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
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

import blockchain.Block;
import blockchain.Blockchain;
import blockchain.ValidationBlockException;
import miner.Miner;
import util.Emitter;

public class Node implements Emitter {
	Blockchain blockchain;
	static HashSet<Peer> peers = new HashSet<Peer>();
	Miner miner;

	public Node(Blockchain blockchain, Miner miner) {
		peers.add(new Peer("87.118.159.27", 0));
		peers.add(new Peer("10.77.10.169", 0));
		peers.add(new Peer("89.25.16.151", 0));
		this.blockchain = blockchain;
		this.miner = miner;
		blockchain.setEmitter(this);
		EventListener();
		new Thread(() -> {
			try {
				peers.parallelStream().forEach(this::join);
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
			new EventProcessor(blockchain, peers, miner);
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
			System.out.println("Cannot connect to " + peer.getIP());
		}

	}

	public void syncBlockchain(Peer peer) {
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
			for (int i = blockchain.getBlockchainHeight(); i < peer.getBlockchainHeight(); i++) {
				sendingJSON = new JSONObject();
				sendingJSON.put("command", "getblock");
				sendingJSON.put("index", i);
				stream.println(sendingJSON.toString());
				receivingJSON = new JSONObject(scanner.nextLine());
				Block block = new Block();
				block.fromJSON(receivingJSON.getJSONObject("block"));
				blockchain.addBlock(block, false);
			}
			blockchain.setSynching(false);
			System.out.println("Blockchain is synced. Last block index: " + (blockchain.getBlockchainHeight() - 1));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(ValidationBlockException e) {
			System.out.println(e.getMessage());
		}
	}

	public void leaveNetwork() {
		JSONObject sendingJSON = new JSONObject();
		sendingJSON.put("command", "leave");
		Socket socket = new Socket();

		peers.stream().forEach(e -> {
			try {
				socket.connect(new InetSocketAddress(e.getIP(), 14200), 5000);
				PrintStream stream = new PrintStream(socket.getOutputStream());
				stream.println(sendingJSON.toString());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		});
	}

	public void blockAdded(Block block) {
		peers.parallelStream().forEach(e -> {
			try (Socket socket = new Socket()) {
				System.out.println("Sending the new block to " + e.getIP());
				JSONObject sendingJSON = new JSONObject();
				sendingJSON.put("command", "sendLatestBlock");
				sendingJSON.put("newBlock", block.toJSON());
				try {
					socket.connect(new InetSocketAddress(e.getIP(), 14200), 5000);
					PrintStream stream = new PrintStream(socket.getOutputStream());
					stream.println(sendingJSON.toString());
				} catch (IOException e1) {
					System.out.println("Cannot send to " + e.getIP());
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

	}

}
