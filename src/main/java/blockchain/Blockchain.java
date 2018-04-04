package blockchain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import util.Emitter;

public class Blockchain {

	final int blockTargetTime = 60;
	private long blockchainDifficulty = (long) 1e15;
	private Emitter emitter;

	List<Block> blocks = new ArrayList<Block>();
	List<Path> blkPaths = new ArrayList<Path>();
	Path mainPath = null;
	Path blocksConfig = null;
	public Boolean isReadingFiles = true;
	volatile public Boolean isSynching = true;

	public Blockchain() {
		String OS = System.getProperty("os.name");

		if (OS.equals("Linux")) {
			mainPath = Paths.get(System.getProperty("user.home"), ".divt");

		} else if (OS.startsWith("Windows")) {
			mainPath = Paths.get(System.getProperty("user.home") + "\\AppData\\Roaming\\.divt");

		}
		blocksConfig = Paths.get(mainPath.toString(), "blocksConfig");
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
		if (!Files.exists(Paths.get(mainPath.toString(), "blk0.txt"))) {
			try {
				Path firstFile = Paths.get(mainPath.toString(), "blk0.txt");
				Files.createFile(firstFile);
				blocks.add(Block.getGenesis());
				Files.write(firstFile, Block.getGenesis().toFile().getBytes());
				blkPaths.add(firstFile);
				isReadingFiles = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				int index = 0;
				while (true) {
					Path path = Paths.get(mainPath.toString(), "blk" + index + ".txt");
					if (Files.exists(path)) {
						final int fileIndex = index;
						Files.readAllLines(path).stream().forEach(e -> blocks.add(Block.fromFile(e, fileIndex)));
						this.blockchainDifficulty = getLastBlock().getDifficulty();
						index++;
						blkPaths.add(path);
					} else {
						break;
					}
				}
				isReadingFiles = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public int getBlockchainHeight() {
		return blocks.size();
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

	public void addBlock(Block block, boolean emit) {
		// Validat
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
		if (emit == true) {
			emitter.blockAdded(block);
		}
		blocks.add(block);
		System.out.println(block.getIndex());
		System.out.println(block.getHash());
		
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

	public void removeForkedBlocks(int index) {
		int blockIndex = blocks.get(index).getBlockFile() + 1;
		while (blocks.size() != index + 1) {
			blocks.remove(index + 1);
		}
		while (true) {
			Path path = Paths.get(mainPath.toString(), "blk" + blockIndex + ".txt");
			if (Files.exists(path)) {

				try {
					Files.delete(path);
					blockIndex++;

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				break;
			}
		}
		Path path = Paths.get(mainPath.toString(), "blk" + blocks.get(index).getBlockFile() + ".txt");
		try {
			StringBuilder fileContent = new StringBuilder();
			Files.lines(path).filter(e -> {
				int lineIndex = Integer.parseInt(e.split(" ")[0]);
				return lineIndex <= index;
			}).forEach(e -> fileContent.append(e + "\n"));
			Files.delete(path);
			Files.createFile(path);
			Files.write(path, fileContent.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isSynching() {
		return isSynching;
	}

	public void setSynching(boolean isSynching) {
		this.isSynching = isSynching;
	}

	public void setEmitter(Emitter emitter) {
		this.emitter = emitter;
	}
}
