package node;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import blockchain.Blockchain;

public class Node {
	Blockchain blockchain;
	HashSet<Peer> peers = new HashSet<Peer>();

	
	// Peer --> IP --> Node --> listener --> 
	public Node(Blockchain blockchain) {
		peers.add(new Peer("87.118.159.27"));
		EventListener();
		this.blockchain = blockchain;
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
					switch (receivingJSON.getString("command")) {
					case "join":
						JSONObject sendingJSON = new JSONObject();
						JSONArray array = new JSONArray();
						peers.add(new Peer(socket.getInetAddress().getHostAddress()));
						sendingJSON.put("height", blockchain.getBlockchainHeight());
						peers.stream().forEach(e -> array.put(e.getIP()));
						sendingJSON.put("peers", array);
						printStream.println(sendingJSON.toString()); // send to peers in the Hashset in form of json
						break;
					case "":  //peer and height block 

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
		});
	}

	public void join(Peer peer) {
		Socket socket;
		try {
			JSONObject sendingJSON = new JSONObject();
			socket = new Socket(peer.getIP(), 4444);
			Scanner scanner = new Scanner(socket.getInputStream());
			PrintStream stream = new PrintStream(socket.getOutputStream());
			sendingJSON.put("command", "join");
			stream.println(sendingJSON.toString());
			JSONObject json = new JSONObject(scanner.nextLine());
			json.getJSONArray("peers").toList().stream().forEach(e -> peers.add(new Peer(e.toString())));
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
