package blockchain;

import util.CryptoUtil;

public class Block {
	long index;
	String previousHash;
	String hash;
	long timestamp;
	int nonce;
	
	String toHash(){
		return CryptoUtil.hash(this.index + this.previousHash + this.timestamp + this.nonce);
	}
	
	Long getDificulty(){
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
