package RPC;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import org.json.JSONObject;

import blockchain.Block;
import blockchain.Blockchain;
import miner.Miner;

public class DivtDaemon implements RMIInterface {
	static Blockchain blockchain;
	static Miner miner;
	
	public static void main(String args[]) {
		Registry registry;
		try {
			registry = LocateRegistry.createRegistry(1099);
			DivtDaemon object = new DivtDaemon();
			RMIInterface stub;
			stub = (RMIInterface) UnicastRemoteObject.exportObject(object, 0);
			registry.bind("divt", stub);
			System.out.println("Running");
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
		blockchain = new Blockchain();
		while(blockchain.isReadingFiles) {
		}
		miner = new Miner(blockchain);
		miner.mine();
	}

	@Override
	public void stop() throws RemoteException {
		try {
			UnicastRemoteObject.unexportObject(this, true);
			miner.stopMining();
			miner.shutDownExecutor();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getBestBlockHash() throws RemoteException {
		String hash = blockchain.getLastBlock().getHash();
		return hash;
	}

	@Override
	public String getBlock(String hash) throws RemoteException {
		try {
			JSONObject json = blockchain.getBlockByHash(hash).toJSON();
			return json.toString(4);
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public String getDifficulty() throws RemoteException {
		String difficulty = String.valueOf(blockchain.getBlockchainDifficulty());
		return difficulty;
	}

	@Override
	public String getblockhash(int index) throws RemoteException {
		Block block = blockchain.getBlockByIndex(index);
		String hash = block.getHash();
		return hash;
	}

	@Override
	public String getBlockchainInfo() throws RemoteException {
		String hash = blockchain.getLastBlock().getHash();
		long blockHeight = blockchain.getBlockByHash(hash).getIndex();
		long difficulty = blockchain.getBlockchainDifficulty();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("blocks", blockHeight);
		jsonObject.put("bestblockhash", hash);
		jsonObject.put("difficulty", difficulty);
		return jsonObject.toString(4);
	}

	@Override
	public void setMining(boolean toMine) throws RemoteException {
		if (toMine && !miner.isMining()) {
			miner.setMining(true);
			new Thread(() -> {
				miner.mine();
			}).start();
		} else if (!toMine && miner.isMining()) {
			miner.stopMining();
			miner.shutDownExecutor();
		}
	}
	@Override
	public String getMiningInfo() throws RemoteException {
		int threads = miner.getThreads();
		boolean isMining = miner.isMining();
		JSONObject jsonObject = new JSONObject();
		if(isMining == true){
			jsonObject.put("status", isMining);
			jsonObject.put("threads", threads);
		}else{
			jsonObject.put("status", isMining);
		}
		return jsonObject.toString(4);
	}
}