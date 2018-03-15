package blockchain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Blockchain {
	
	final int blockTargetTime = 60; 
	private long blockchainDifficulty = (long) 1e15;
	
	Block blocks[] = new Block[6];
	
	public Blockchain () {
		String OS = System.getProperty("os.name");
		Path mainDir = null;
		Path blocksFile = null;
		if(OS.equals("Linux")){
			mainDir = Paths.get(System.getProperty("user.home"), ".divt");
		}else if(OS.startsWith("Windows")){
			mainDir = Paths.get(System.getProperty("user.home") + "\\AppData\\Roaming\\.divt");
		}
		blocksFile = Paths.get(mainDir.toString() + "blocks.dat");
		if(!Files.exists(mainDir)){
			try {
				Files.createDirectory(mainDir);
				Files.createFile(blocksFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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