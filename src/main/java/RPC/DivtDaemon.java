package RPC;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import org.json.JSONObject;
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
		miner = new Miner(blockchain);

	}

	@Override
	public String help() throws RemoteException {
		// Return help options
		return null;
	}

	@Override
	public void stop() throws RemoteException {
		try {
			UnicastRemoteObject.unexportObject(this, true);
			Miner.stopMining();   //changed by me
			Miner.shutDownExecutor(); //changed by me
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
		JSONObject json = blockchain.getBlockByHash(hash).toJSON();
		return json.toString(4);
	}

	@Override
	public String getDifficulty() throws RemoteException {
		String difficulty = String.valueOf(blockchain.getBlockchainDifficulty());
		return difficulty;
	}

}
