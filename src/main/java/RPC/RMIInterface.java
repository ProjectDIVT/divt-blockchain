package RPC;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.json.JSONObject;

public interface RMIInterface extends Remote {
	void stop() throws RemoteException;
	String getBlockchainInfo() throws RemoteException;
	String getBestBlockHash() throws RemoteException;
	String getBlock(String hash) throws RemoteException;
	String getDifficulty() throws RemoteException;
    String getblockhash(int index) throws RemoteException;
    String getMiningInfo() throws RemoteException;
    void setMining(boolean toMine) throws RemoteException;
}
