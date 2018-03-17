package blockchain;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Blockchain {

	final int blockTargetTime = 60;
	private long blockchainDifficulty = (long) 1e15;

	List<Block> blocks = new ArrayList<Block>();
	List<Path> blkPaths = new ArrayList<Path>();
	Path mainPath = null;
	Path blocksConfig = null;
	public Boolean isReadingFiles = true;

	public Blockchain() {
		String OS = System.getProperty("os.name");

		if (OS.equals("Linux")) {
			mainPath = Paths.get(System.getProperty("user.home"), ".divt");

		} else if (OS.startsWith("Windows")) {
			mainPath = Paths.get(System.getProperty("user.home") + "\\AppData\\Roaming\\.divt");

		}
		blocksConfig = Paths.get(mainPath.toString(), "blocksConfig");
		blkPaths.add(Paths.get(mainPath.toString(), "blk0.txt"));
		if (!Files.exists(mainPath)) {
			try {
				Files.createDirectory(mainPath);
				blocks.add(Block.getGenesis());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!Files.exists(blocksConfig)) {
			try {
				// BlocksConfig
				Files.createFile(blocksConfig);
				JSONObject json = new JSONObject();
				JSONArray array = new JSONArray();

				JSONObject ob = new JSONObject();
				ob.put("index", 0);
				ob.put("blocksFrom", 0);
				ob.put("blocksTo", -1);
				array.put(ob);

				json.put("files", array);
				Files.write(blocksConfig, json.toString().getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!Files.exists(blkPaths.get(0))) {
			try {
				Files.createFile(blkPaths.get(0));
				blocks.add(Block.getGenesis());
				Files.write(blkPaths.get(0), Block.getGenesis().toFile().getBytes());
				isReadingFiles = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				int index = 0;
				while (true) {
					Path path = Paths.get(mainPath.toString(),"blk" + index + ".txt");
					if (Files.exists(path)) {
						Files.readAllLines(path).stream().forEach(e -> blocks.add(Block.fromFile(e)));
						this.blockchainDifficulty = getLastBlock().getDifficulty();
						index++;
						blkPaths.add(path);
					}else {
						break;
					}
				}
				isReadingFiles = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

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
		return blocks.get(blocks.size() - 1);
	}

	public void addBlock(Block block) {
		// Validate
		Block previousBlock = getLastBlock();
		int fileNumber = previousBlock.getBlockFile();
		modifyBlockchainDifficulty((double) ((block.getTimestamp() - previousBlock.getTimestamp()) / blockTargetTime));
		if (blkPaths.get(blkPaths.size() - 1).toFile().length() > 1024) { // The size has to be changed 
			Path newPath = Paths.get(mainPath.toString(), "blk" + (fileNumber + 1) + ".txt");
			blkPaths.add(Paths.get(newPath.toString()));
			try {
				Files.createFile(newPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileNumber++;
		}
		block.setBlockFile(fileNumber);
		try {
			Files.write(blkPaths.get(fileNumber), block.toFile().getBytes(), StandardOpenOption.APPEND);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		blocks.add(block);
		System.out.println(block.getIndex());
		System.out.println(block.getHash());
		// Emit to Node Class
	}

	public long getBlockchainDifficulty() {
		return blockchainDifficulty;
	}

	public void modifyBlockchainDifficulty(double difficultyMultiplier) {
		if (difficultyMultiplier > 2) {
			difficultyMultiplier = 2;
		}
		if (difficultyMultiplier < 0.5) {
			difficultyMultiplier = 0.5;
		}

		this.blockchainDifficulty *= difficultyMultiplier;
	}
}