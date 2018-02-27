package RPC;


import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {
	String help() throws RemoteException;
	
	void stop() throws RemoteException, NotBoundException;
	
}
