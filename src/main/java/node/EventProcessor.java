package node;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import blockchain.Block;
import blockchain.Blockchain;
import blockchain.ValidationBlockException;
import miner.Miner;

public class EventProcessor {
	public EventProcessor(Blockchain blockchain, HashSet<Peer> peers, Miner miner) {
		try (ServerSocket serverSocket = new ServerSocket(14200)) {
			while (true) {
				Socket socket = serverSocket.accept();
				new Thread(() -> {
					System.out.println("Received from: " + socket.getInetAddress().getHostAddress());
					PrintStream printStream;
					try (Scanner scanner = new Scanner(socket.getInputStream())) {
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
								printStream.println(sendingJSON.toString());

								break;
							case "getblockhash":
								sendingJSON = new JSONObject();
								sendingJSON.put("blockhash",
										blockchain.getBlockByIndex(receivingJSON.getInt("index")).getHash());
								printStream.println(sendingJSON.toString());
								break;
							case "getblock":
								sendingJSON = new JSONObject();
								sendingJSON.put("block",
										blockchain.getBlockByIndex(receivingJSON.getInt("index")).toJSON());
								printStream.println(sendingJSON.toString());
								break;
							case "leave":
								peers.remove(new Peer(socket.getInetAddress().getHostAddress(), 0));
								break;
							case "sendLatestBlock":
								new Thread(() -> {
									Block block = new Block();
									block.fromJSON(receivingJSON.getJSONObject("newBlock"));
									miner.stopMining();
									miner.shutDownExecutor();
									try {
										blockchain.addBlock(block, false);
										Thread.sleep(500);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (ValidationBlockException e) {
										System.out.println(e.getMessage());
									} finally {
										miner.setMining(true);
										miner.mine();
									}
								}).start();
								break;

							default:
								break;
							}
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
