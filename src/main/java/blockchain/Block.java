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

	public long getBlockchainDifficulty() {
		return blockchainDifficulty;
	}

	public void setBlockchainDifficulty(long blockchainDifficulty) {
		this.blockchainDifficulty = blockchainDifficulty;
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

	public int getBlockFile() {
		return blockFile;
	}

	public void setBlockFile(int blockFile) {
		this.blockFile = blockFile;
	}

	/**
	 * Hashes the block parameters and returns it.
	 * 
	 * @return String The hash of the block.
	 */
	public String toHash() {
		return CryptoUtil.hash(this.index + this.previousHash + this.timestamp + this.nonce);
	}

	/**
	 * Returns the first 15 characters from the block hash as a number
	 * 
	 * @return long The hash of the block as a number.
	 */
	public long getDifficulty() {
		return Long.parseLong(this.hash.substring(0, 15), 16);
	}

	/**
	 * Returns the first block of the network also known as the genesis block.
	 * 
	 * @return Block The genesis block.
	 */
	static Block getGenesis() {
		Block block = new Block();
		block.index = 0;
		block.previousHash = "0";
		block.hash = "0000000000000000";
		block.timestamp = 0;
		block.nonce = 0;
		block.blockFile = 0;
		block.blockchainDifficulty = (long) 1e15;
		return block;
	}

	/**
	 * Creates a JSON with the block parameters.
	 * 
	 * @return JSONObject The block as a JSON.
	 */
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("index", this.getIndex());
		json.put("hash", this.getHash());
		json.put("timestamp", this.getTimestamp());
		json.put("nonce", this.getNonce());
		json.put("previousHash", this.getPreviousHash());
		json.put("blockchainDifficulty", this.getBlockchainDifficulty());
		json.put("blockFile", this.getBlockFile());
		return json;
	}

	/**
	 * Sets the block parameters from the JSON.
	 * 
	 * @param json
	 *            The JSON to get the parameters from.
	 */
	public void fromJSON(JSONObject json) {
		this.setIndex(json.getInt("index"));
		this.setNonce(json.getLong("nonce"));
		this.setPreviousHash(json.getString("previousHash"));
		this.setHash(json.getString("hash"));
		this.setTimestamp(json.getLong("timestamp"));
		this.setBlockchainDifficulty(json.getLong("blockchainDifficulty"));
		this.setBlockFile(json.getInt("blockFile"));
	}

	/**
	 * Returns the block parameters as a line.
	 * 
	 * @return String The block parameters on a single line.
	 */
	public String toFile() {
		return index + " " + hash + " " + previousHash + " " + timestamp + " " + nonce + " " + blockchainDifficulty
				+ "\n";
	}

	/**
	 * Returns a Block instance from a file line.
	 * 
	 * @param line
	 *            The line from a file with block parameters.
	 * @param blockFile
	 *            The number of the block file.
	 * @return Block The block from the line.
	 */
	public static Block fromFile(String line, int blockFile) {
		String[] params = line.split(" "); // Have to be validate
		Block block = new Block();

		block.index = Integer.parseInt(params[0]);
		block.hash = params[1];
		block.previousHash = params[2];
		block.timestamp = Long.parseLong(params[3]);
		block.nonce = Long.parseLong(params[4]);
		block.blockchainDifficulty = Long.parseLong(params[5]); // Validate
		block.setBlockFile(blockFile);
		return block;
	}
}
