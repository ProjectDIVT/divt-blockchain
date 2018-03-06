package blockchain;

import org.json.JSONObject;

import util.CryptoUtil;

public class Block {
	private long index;
	private String previousHash;
	private String hash;
	private long timestamp;
	private long nonce;
	
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
		return this.hash;
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

	public long getNonce() {
		return nonce;
	}

	public void setNonce(long nonce) {
		this.nonce = nonce;
	}

	public String toHash(){
		return CryptoUtil.hash(this.index + this.previousHash + this.timestamp + this.nonce);
	}
	
	public long getDifficulty(){
		return Long.parseLong(this.hash.substring(0, 15),16);
	}
	
	static Block getGenesis(){
		Block block = new Block();
		block.index = 0;
		block.previousHash = "0";
		block.hash = "0";
		block.timestamp = 0;
		block.nonce = 0;
		return block;
	}
	
	public JSONObject toJSON(){
		JSONObject json = new JSONObject();
		json.put("index", this.getIndex());
		json.put("hash", this.getHash());
		json.put("timestamp", this.getTimestamp());
		json.put("nonce", this.getNonce());
		json.put("previousHash", this.getPreviousHash());
		return json;
	}
	public static Block fromJSON(JSONObject json) {
		Block block = new Block();
		block.setIndex(json.getLong("index"));
		block.setNonce(json.getLong("nonce"));
		block.setPreviousHash(json.getString("previousHash"));
		block.setHash(json.getString("hasg"));
		block.setTimestamp(json.getLong("timestamp"));
		return block;
	}
}
