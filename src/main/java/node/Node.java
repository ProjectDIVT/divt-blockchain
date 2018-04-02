package node;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import blockchain.Block;
import blockchain.Blockchain;

public class Node {
	Blockchain blockchain;
	static HashSet<Peer> peers = new HashSet<Peer>();

	// join -->
	// Peer --> IP --> Node --> listener -->
	public Node(Blockchain blockchain) {
		peers.add(new Peer("78.130.133.28", 0));
		peers.add(new Peer("87.118.159.27", 0));
		this.blockchain = blockchain;
		EventListener();
		new Thread(() -> {
			try {
				peers.stream().forEach(this::join);
			} catch (ConcurrentModificationException ex) {
			}
			Comparator<Peer> comparator = Comparator.comparing(Peer::getBlockchainHeight);
			Peer longestChainPeer = peers.stream().max(comparator).get();
			syncBlockchain(longestChainPeer);
			blockchain.setSynching(false);
		}).start();
	}

	public void EventListener() {
		new Thread(() -> {
			try (ServerSocket serverSocket = new ServerSocket(14200)) {
				while (true) {
					Socket socket = serverSocket.accept();
					new Thread(() -> {
						System.out.println("Received from: " + socket.getInetAddress().getHostAddress());
						Scanner scanner;
						PrintStream printStream;
						try {
							scanner = new Scanner(socket.getInputStream());
							printStream = new PrintStream(socket.getOutputStream());
							while (scanner.hasNext()) {
								JSONObject receivingJSON = new JSONObject(scanner.nextLine());
								JSONObject sendingJSON;
								switch (receivingJSON.getString("command")) {
								case "join":
									sendingJSON = new JSONObject();
									JSONArray array = new JSONArray();
									peers.add(new Peer(socket.getInetAddress().getHostAddress(),
											receivingJSON.getInt("height")));
									peers.forEach(e -> {
										if (e.getIP().equals(socket.getInetAddress().getHostAddress())) {
											e.setBlockchainHeight(receivingJSON.getInt("height"));
										}
									});
									sendingJSON.put("height", blockchain.getBlockchainHeight());
									peers.stream().forEach(e -> {
										JSONObject peer = new JSONObject();
										peer.put("host", e.getIP());
										peer.put("height", e.getBlockchainHeight());
										array.put(peer);
									});
									sendingJSON.put("peers", array);
									printStream.println(sendingJSON.toString()); // send to peers in the Hashset in form
																					// of json
									break;
								case "getblockhash":
									sendingJSON = new JSONObject();
									sendingJSON.put("blockhash",
											blockchain.getBlockByIndex(receivingJSON.getInt("index")).getHash());
									printStream.println(sendingJSON.toString());
									break;
								case "b": //

									break;

								default:
									break;
								}
							}
							scanner.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					});
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}).start();
	}

	public void join(Peer peer) {
		Scanner scanner = null;
		try {
			JSONObject sendingJSON = new JSONObject();
			System.out.println("Sended to: " + peer.getIP());
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(peer.getIP(), 14200), 5000);
			scanner = new Scanner(socket.getInputStream());
			PrintStream stream = new PrintStream(socket.getOutputStream());
			sendingJSON.put("command", "join");
			sendingJSON.put("height", blockchain.getBlockchainHeight());
			stream.println(sendingJSON.toString());
			JSONObject json = new JSONObject(scanner.nextLine());
			peer.setBlockchainHeight(json.getInt("height"));
			json.getJSONArray("peers").forEach(e -> {
				JSONObject jsonPeer = (JSONObject) e;
				peers.add(new Peer(jsonPeer.getString("host"), jsonPeer.getInt("height")));
			});
			scanner.close();
		} catch (SocketTimeoutException | NoRouteToHostException e) {
			System.out.println("Cannot reach the peer: " + peer.getIP());
			return;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void syncBlockchain(Peer peer) {
		System.out.println(peer.getIP() + " " + peer.getBlockchainHeight());
		Socket socket = new Socket();
		JSONObject json = new JSONObject();
		JSONObject sendingJSON;
		try {
			socket.connect(new InetSocketAddress(peer.getIP(), 14200), 5000);
			Scanner scanner = new Scanner(socket.getInputStream());
			PrintStream stream = new PrintStream(socket.getOutputStream());
			json.put("command", "getblockhash");
			json.put("index", blockchain.getBlockchainHeight() - 1);
			stream.println(json.toString());
			JSONObject receivingJSON = new JSONObject(scanner.nextLine());
			String hash = receivingJSON.getString("blockhash");
			System.out.println(hash);
			if (!hash.equals(blockchain.getLastBlock().getHash())) {
				int index = blockchain.getLastBlock().getIndex();
				int difference = 1;
				while (true) {
					if (index < 0) {
						difference /= 2;
						index += difference;
						continue;
					}
					sendingJSON = new JSONObject();
					sendingJSON.put("command", "getblockhash");
					sendingJSON.put("index", index);
					stream.println(sendingJSON.toString());
					receivingJSON = new JSONObject(scanner.nextLine());
					hash = receivingJSON.getString("blockhash");
					if (blockchain.getBlockByIndex(index).getHash().equals(hash)) {
						difference /= 2;
						index += difference;
					} else {
						difference *= 2;
						index -= difference;
					}
					if (difference == 1) {
						sendingJSON = new JSONObject();
						sendingJSON.put("command", "getblockhash");
						sendingJSON.put("index", index);
						stream.println(sendingJSON.toString());
						receivingJSON = new JSONObject(scanner.nextLine());
						hash = receivingJSON.getString("blockhash");
						if (!blockchain.getBlockByIndex(index).getHash().equals(hash)) {
							System.out.println(hash + "\n" + blockchain.getBlockByIndex(index).getHash());
							index -= 1;
						}
						break;
					}
				}
				System.out.println("You're forked on block with index : " + index);
				blockchain.removeForkedBlocks(index);
			}
			blockchain.setSynching(false);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
