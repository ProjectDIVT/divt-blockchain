package blockchain;

import util.CryptoUtil;

public class Block {
	private long index;
	private String previousHash;
	private String hash;
	private long timestamp;
	private int nonce;
	
	
	public long getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getNonce() {
		return nonce;
	}

	public void setNonce(int nonce) {
		this.nonce = nonce;
	}

	public String toHash(){
		return CryptoUtil.hash(this.index + this.previousHash + this.timestamp + this.nonce);
	}
	
	public long getDificulty(){
		return Long.parseLong(this.hash.substring(0, 15),16);
	}
	
	static Block getGenesis(){
		Block block = new Block();
		block.index=0;
		block.previousHash=null;
		block.hash=null;
		block.timestamp=0;
		block.nonce=0;
		return block;
	}
	
	//ToDo add From/ToJSON
	
}
