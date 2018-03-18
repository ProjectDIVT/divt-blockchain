package node;

import java.util.Arrays;

public class Peer {
	private final String IP;

	public Peer(String IP) {
		this.IP = IP;
	}
	
	public String getIP() {
		return IP;
	}
	
	@Override
    public int hashCode() {
        return Arrays.stream(IP.split("\\.")).mapToInt(e -> Integer.parseInt(e)).reduce(1, (a,b) -> a*b);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof Peer) {
            Peer peer = (Peer)obj;
            return this.IP.equals(peer.getIP());      
        }else { return false; }
    }
}
