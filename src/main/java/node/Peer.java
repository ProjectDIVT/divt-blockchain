package node;

import java.util.Arrays;

public class Peer {
	private final String IP;
	private int blockchainHeight;

	public Peer(String IP, int blockchainHeight) {
		this.blockchainHeight = blockchainHeight;
		this.IP = IP;
	}

	public String getIP() {
		return IP;
	}

	@Override
	public int hashCode() {
		return Arrays.stream(IP.split("\\.")).mapToInt(e -> Integer.parseInt(e)).reduce(1, (a, b) -> a * b);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof Peer) {
			Peer peer = (Peer) obj;
			return this.IP.equals(peer.getIP());
		} else {
			return false;
		}
	}

	public void setBlockchainHeight(int blockchainHeight) {
		this.blockchainHeight = blockchainHeight;
	}

	public int getBlockchainHeight() {
		return blockchainHeight;
	}
}
