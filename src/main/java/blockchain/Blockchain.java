package blockchain;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {
	
	final int blockTargetTime = 60; 
	private long blockchainDifficulty = (long) 1e15;
	
	List<Block> blocks = new ArrayList<Block>();
	
	public Blockchain () {
		
		//Check if the blocks list is empty when blocksDb is added
		blocks.add(Block.getGenesis());
	}
	
	public List<Block> getAllBlocks() {
		return this.blocks;
	}
	
	public Block getBlockByIndex(int index) {
		return this.blocks.get(index);
	}
	
	public Block getBlockByHash(String hash) {
		return blocks.stream().parallel().filter(block -> block.getHash().equals(hash)).findAny().orElse(null);
	}
	
	public Block getLastBlock() {
		return blocks.get(blocks.size() -1);
	}
	
	public void addBlock(Block block) {
		//Validate
		Block previousBlock = getLastBlock();
		modifyBlockchainDifficulty((double)(( block.getTimestamp() - previousBlock.getTimestamp()) / blockTargetTime));
		blocks.add(block);
		System.out.println(block.getIndex());
		System.out.println(block.getHash());
		//Emit to Node Class
	}

	public long getBlockchainDifficulty() {
		return blockchainDifficulty;
	}
	
	public void modifyBlockchainDifficulty(double difficultyMultiplier) {
		if(difficultyMultiplier > 2) {
			difficultyMultiplier = 2;
		}
		if(difficultyMultiplier < 0.5) {
			difficultyMultiplier = 0.5;
		}
	
		this.blockchainDifficulty *= difficultyMultiplier;
	}
}