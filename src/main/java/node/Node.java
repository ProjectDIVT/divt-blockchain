package node;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import blockchain.Blockchain;

public class Node {
	Blockchain blockchain;
	static HashSet<Peer> peers = new HashSet<Peer>();

	// Peer --> IP --> Node --> listener -->
	public Node(Blockchain blockchain) {
		peers.add(new Peer("87.118.159.27", 0));
		this.blockchain = blockchain;
		EventListener();
		new Thread(() -> {
			try {
				peers.stream().forEach(this::join);
			} catch (ConcurrentModificationException ex) {
			}
		}).start();
	}

	public void EventListener() {
		new Thread(() -> {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(14200);
				while (true) {
					Socket socket = serverSocket.accept();
					Scanner scanner = new Scanner(socket.getInputStream());
					PrintStream printStream = new PrintStream(socket.getOutputStream());
					JSONObject receivingJSON = new JSONObject(scanner.nextLine());
					System.out.println("Received from: " + socket.getInetAddress().getHostAddress());
					switch (receivingJSON.getString("command")) {
					case "join":
						JSONObject sendingJSON = new JSONObject();
						JSONArray array = new JSONArray();
						peers.add(new Peer(socket.getInetAddress().getHostAddress(), receivingJSON.getInt("height")));
						peers.forEach(e -> {
						    if(e.getIP().equals(socket.getInetAddress().getHostAddress())) {
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
						printStream.println(sendingJSON.toString()); // send to peers in the Hashset in form of json
						break;
					case "": // peer and height block

						break;
					case "b": //

						break;

					default:
						break;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
	}

	public void join(Peer peer) {
		Socket socket;
		try {
			JSONObject sendingJSON = new JSONObject();
			System.out.println("Sended to: " + peer.getIP());
			socket = new Socket();
			socket.connect(new InetSocketAddress(peer.getIP(), 14200), 10000);
			Scanner scanner = new Scanner(socket.getInputStream());
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

		} catch (SocketTimeoutException e) {
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

}
