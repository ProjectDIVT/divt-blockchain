package blockchain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import util.Emitter;

public class Blockchain {

	public final int blockTargetTime = 60;
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
						blkPaths.add(path);
						Files.readAllLines(path).stream().forEach(e -> {
							try {
								Block block = Block.fromFile(e, fileIndex);
								validateBlock(block);
								blocks.add(block);
							} catch (ValidationBlockException ex) {
								System.out.println(ex.getMessage());
								return;
							}
						});
						this.blockchainDifficulty = getLastBlock().getBlockchainDifficulty();
						index++;
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
	 * @throws ValidationBlockException
	 */
	synchronized public void addBlock(Block block, boolean emit) throws ValidationBlockException {
		System.out.println("getIndex " + block.getIndex());
		Block previousBlock = getLastBlock();
		int fileNumber = previousBlock.getBlockFile();
		validateBlock(block);

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

	/**
	 * Checks if the new block is legitimate.
	 * 
	 * @param block
	 *            The new block
	 * @param previousBlock
	 *            The last block in the blockchain
	 * @throws ValidationBlockException
	 */
	public void validateBlock(Block block) throws ValidationBlockException {
		if (block.getIndex() == 0) {
			if (!block.getHash().equals("0000000000000000") || !block.getPreviousHash().equals("0")
					|| block.getBlockchainDifficulty() != 1e15 || block.getDifficulty() != 0
					|| block.getTimestamp() != 0 || block.getNonce() != 0 || block.getBlockFile() != 0) {
				throw new ValidationBlockException("Invalid genesis block");
			} else {
				blocks.add(block);
				return;
			}
		}
		Block previousBlock = getLastBlock();
		// Check if the block is the last one
		if (block.getIndex() != previousBlock.getIndex() + 1) {
			throw new ValidationBlockException(
					"Invalid index: expected " + (previousBlock.getIndex() + 1) + " got " + block.getIndex());
		}
		// Check if the hash is correct
		if (!block.toHash().equals(block.getHash())) {
			throw new ValidationBlockException("Invalid hash: expected " + block.toHash() + " got " + block.getHash());
		}
		// Check if the previous block is correct
		if (!previousBlock.getHash().equals(block.getPreviousHash())) {
			throw new ValidationBlockException(
					"Invalid previoushash: expected " + previousBlock.getHash() + " got " + block.getPreviousHash());			
		}
		
		if (block.getTimestamp() < previousBlock.getTimestamp() || block.getTimestamp() > Instant.now().getEpochSecond()) {
			throw new ValidationBlockException("Invalid timestamp");
		}

		double multiplier = (block.getTimestamp() - previousBlock.getTimestamp()) / (double) blockTargetTime;
		if (multiplier > 2) {
			multiplier = 2;
		}
		if (multiplier < 0.5) {
			multiplier = 0.5;
		}
		// Check if the blockchain difficulty of the new block is correct
		if ((long) (previousBlock.getBlockchainDifficulty() * multiplier) != block.getBlockchainDifficulty()) {
			throw new ValidationBlockException(
					"Invalid blockchainDifficulty: expected " + (long) (previousBlock.getBlockchainDifficulty() * multiplier)
							+ " got " + block.getBlockchainDifficulty());
		}
	}
}
