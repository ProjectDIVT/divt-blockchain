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
		// Sets the main directory depending on the user's OS
		String OS = System.getProperty("os.name");
		if (OS.equals("Linux")) {
			mainPath = Paths.get(System.getProperty("user.home"), ".divt");
		} else if (OS.startsWith("Windows")) {
			mainPath = Paths.get(System.getProperty("user.home") + "\\AppData\\Roaming\\.divt");
		}

		// Creates the main directory if it doesn't exist
		if (!Files.exists(mainPath)) {
			try {
				Files.createDirectory(mainPath);
				blocks.add(Block.getGenesis());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		blocksConfig = Paths.get(mainPath.toString(), "blocksConfig");
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
						this.blockchainDifficulty = getLastBlock().getBlockchainDifficulty();
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

	/**
	 * Returns the height of the blockchain.
	 * 
	 * @return int The blockchain height.
	 */
	public int getBlockchainHeight() {
		return blocks.size();
	}

	/**
	 * Returns all blocks in the blockchain.
	 * 
	 * @return List<Block> All blocks in the blockchain.
	 */
	public List<Block> getAllBlocks() {
		return this.blocks;
	}

	/**
	 * Returns block from given index.
	 * 
	 * @param index
	 *            The index of the block.
	 * @return Block The block with the specified index.
	 */
	public Block getBlockByIndex(int index) {
		return this.blocks.get(index);
	}

	/**
	 * Returns block from given hash of the block.
	 * 
	 * @param hash
	 *            The hash of the block.
	 * @return Block The block with the specified hash.
	 */
	public Block getBlockByHash(String hash) {
		return blocks.stream().parallel().filter(block -> block.getHash().equals(hash)).findAny().orElse(null);
	}

	/**
	 * Returns the last block in the blockchain.
	 * 
	 * @return Block the last block in the blockchain.
	 */
	public Block getLastBlock() {
		return blocks.get(blocks.size() - 1);
	}

	/**
	 * Adds the block to the blockchain and broadcast it to the other peers if the
	 * new block is mined by the user.
	 * 
	 * @param block
	 *            The block to be added to the blockchain.
	 * @param emit
	 *            If true, broadcast the new block to the other peers.
	 */
	synchronized public void addBlock(Block block, boolean emit) {
		Block previousBlock = getLastBlock();
		int fileNumber = previousBlock.getBlockFile();

		// Validate
		// Move to validateBlock method
		if (block.getIndex() <= previousBlock.getIndex()) {
			return; // throw BlockAssertionException
		}

		modifyBlockchainDifficulty(block.getTimestamp(), previousBlock.getTimestamp());

		// If the current blk file is bigger than than 1KB, create a new blk file
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
		block.setBlockchainDifficulty(getBlockchainDifficulty());
		block.setBlockFile(fileNumber);

		// Append the block information to the file
		try {
			Files.write(blkPaths.get(fileNumber), block.toFile().getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (emit == true) {
			System.out.println("Block sended");
			emitter.blockAdded(block);
		}

		blocks.add(block);

		System.out.println(block.getIndex());
		System.out.println(block.getHash());

	}

	/**
	 * Gets the current blockchain difficulty.
	 * 
	 * @return long The blockchain difficulty.
	 */
	public long getBlockchainDifficulty() {
		return blockchainDifficulty;
	}

	/**
	 * Changes the blockchain difficulty based on the timestamps of the new and the
	 * previous blocks.
	 * 
	 * @param newBlockTimestamp
	 *            The timestamp of the new block.
	 * @param previousBlockTimestamp
	 *            The timestamp of the last block.
	 */
	public void modifyBlockchainDifficulty(double newBlockTimestamp, double previousBlockTimestamp) {
		double difficultyMultiplier = (newBlockTimestamp - previousBlockTimestamp) / (double) this.blockTargetTime;

		// Limit the blockchain difficulty change value
		if (difficultyMultiplier > 2) {
			difficultyMultiplier = 2;
		}
		if (difficultyMultiplier < 0.5) {
			difficultyMultiplier = 0.5;
		}

		this.blockchainDifficulty *= difficultyMultiplier;
	}

	/**
	 * Removes all forked blocks from the blockchain.
	 * 
	 * @param index
	 *            The index of the last not forked block.
	 */
	public void removeForkedBlocks(int index) {
		int blockFileIndex = blocks.get(index).getBlockFile() + 1;

		// Remove all blocks after the last not forked one
		while (blocks.size() != index + 1) {
			blocks.remove(index + 1);
		}

		// Remove all blk files after the blk file of the last not forked block
		while (true) {
			Path path = Paths.get(mainPath.toString(), "blk" + blockFileIndex + ".txt");
			if (Files.exists(path)) {
				try {
					Files.delete(path);
					blockFileIndex++;

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				break;
			}
		}

		// Removes all lines after the last not forked blocks in its blk file
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
