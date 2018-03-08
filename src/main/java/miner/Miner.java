package miner;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import blockchain.Block;
import blockchain.Blockchain;

public class Miner {

	Blockchain blockchain;
	private ExecutorService executor;
	private volatile boolean isMining = true;
	private byte threads = 2;

	public Miner(Blockchain blockchain) {
		this.blockchain = blockchain;
	}

	public void mine() {
		while (isMining) {
			Block block = generateNextBlock(blockchain);
			if (block.getIndex() != 0) {
				blockchain.addBlock(block);
			}
		}

	}

	private Block generateNextBlock(Blockchain blockchain) {

		Block previousBlock = blockchain.getLastBlock();
		int index = previousBlock.getIndex() + 1;
		String previousHash = previousBlock.getHash();
		Block newBlock = new Block();
		threads = 2;

		executor = Executors.newFixedThreadPool(threads); //
		for (int i = 0; i < threads; i++) {
			final long num = i;
			executor.execute(() -> {
				Block block = new Block();
				block.setIndex(index);
				block.setPreviousHash(previousHash);
				long blockDifficulty = 0;
				long j = num; // variable's name have to be changed
				while (true) {
					block.setTimestamp(Instant.now().getEpochSecond());
					block.setNonce(j);
					block.setHash(block.toHash());
					blockDifficulty = block.getDifficulty();
					j += threads;
					if (blockDifficulty < blockchain.getBlockchainDifficulty()) {
						newBlock.setIndex(block.getIndex());
						newBlock.setHash(block.getHash());
						newBlock.setNonce(block.getNonce());
						newBlock.setPreviousHash(block.getPreviousHash());
						newBlock.setTimestamp(block.getTimestamp());
						executor.shutdownNow();
					}
					if (Thread.currentThread().isInterrupted()) {
						break;
					}
				}
			});
		}
		try {
			executor.awaitTermination(1000000, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newBlock;

	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public byte getThreads() {
		return threads;
	}

	public void setThreads(byte threads) {
		this.threads = threads;
	}

	public boolean isMining() {
		return isMining;
	}

	public void setMining(boolean isMining) {
		this.isMining = isMining;
	}

	public void stopMining() {
		isMining = false;
	}

	public void shutDownExecutor() {
		executor.shutdownNow();
	}
}
