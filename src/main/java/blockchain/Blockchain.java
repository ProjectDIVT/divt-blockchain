package blockchain;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {

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
		blocks.add(block);
		//Emit to Node Class
	}
	
}