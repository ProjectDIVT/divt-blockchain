package RPC;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.json.JSONObject;

public interface RMIInterface extends Remote {
	String help() throws RemoteException;
	void stop() throws RemoteException;
	String getBestBlockHash() throws RemoteException;
	String getBlock(String hash) throws RemoteException;
	String getblockhash(int index) throws RemoteException;
}
