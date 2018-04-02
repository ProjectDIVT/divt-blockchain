package blockchain;

import org.json.JSONObject;

import util.CryptoUtil;

public class Block {
	private int blockFile;
	private int index;
	private String previousHash;
	private String hash;
	private long timestamp;
	private long nonce;
	private long blockchainDifficulty;
	
	public void setBlockchainDifficulty(long blockchainDifficulty) {
		this.blockchainDifficulty = blockchainDifficulty;
	}
	
	public long getBlockchainDifficulty() {
		return blockchainDifficulty;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
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
	public int getBlockFile() {
		return blockFile;
	}

	public void setBlockFile(int blockFile) {
		this.blockFile = blockFile;
	}
	
	static Block getGenesis(){
		Block block = new Block();
		block.index = 0;
		block.previousHash = "0";
		block.hash = "0";
		block.timestamp = 0;
		block.nonce = 0;
		block.blockFile = 0;
		return block;
	}
	
	public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        json.put("index", this.getIndex());
        json.put("hash", this.getHash());
        json.put("timestamp", this.getTimestamp());
        json.put("nonce", this.getNonce());
        json.put("previousHash", this.getPreviousHash());
        json.put("blockchainDifficulty", this.getBlockchainDifficulty());
        return json;
    }
    public void fromJSON(JSONObject json) {
        this.setIndex(json.getInt("index"));
        this.setNonce(json.getLong("nonce"));
        this.setPreviousHash(json.getString("previousHash"));
        this.setHash(json.getString("hash"));
        this.setTimestamp(json.getLong("timestamp"));
        this.setBlockchainDifficulty(json.getLong("blockchainDifficulty"));
    }

	public String toFile() {
		return index + " " + hash + " " + previousHash + " " + timestamp + " " + nonce + " " + blockchainDifficulty +"\n";
	}
	public static Block fromFile(String line, int blockFile) {
		String [] params = line.split(" ");  	//Have to be validate 
		Block block = new Block();
		
		block.index = Integer.parseInt(params[0]);
		block.hash = params[1];
		block.previousHash = params[2];
		block.timestamp = Long.parseLong(params[3]);
		block.nonce = Long.parseLong(params[4]);
		block.blockchainDifficulty = Long.parseLong(params[5]); //Validate
		block.setBlockFile(blockFile);
		return block;
	}
}
