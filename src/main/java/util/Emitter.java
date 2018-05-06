package util;

import blockchain.Block;

public interface Emitter {
	public void blockAdded(Block block);
}
