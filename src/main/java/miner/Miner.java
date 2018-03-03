package miner;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import blockchain.Block;
import blockchain.Blockchain;


public class Miner {

	Blockchain blockchain;
	boolean isMining = true;

	public Miner(Blockchain blockchain) {
		this.blockchain = blockchain;
		mine();
	}

	public void mine() {
		if (isMining) {
			generateNextBlock(blockchain);
		}
	}

	private void generateNextBlock(Blockchain blockchain) {

		Block previousBlock = blockchain.getLastBlock();
		long index = previousBlock.getIndex() + 1;
		String previousHash = previousBlock.getHash();
		long timestamp = new Date().getTime() / 1000;
		Block block = new Block();
		block.setIndex(index);
		block.setTimestamp(timestamp);
		block.setPreviousHash(previousHash);
		proveWorkFor(block);

	}

	private void proveWorkFor(Block block) {
		long blockDifficulty = 0;
		long unixTimestamp = Instant.now().getEpochSecond();

		do {
			if (isMining) {
				block.setNonce(block.getNonce() + 1);
				block.setHash(block.toHash());
				blockDifficulty = block.getDifficulty();
			} else { break; }
		} while (blockDifficulty >= blockchain.getBlockchainDifficulty());
		blockchain.addBlock(block);
		mine();
	}
}
