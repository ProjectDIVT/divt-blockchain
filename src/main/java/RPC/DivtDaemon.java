package RPC;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class DivtDaemon implements RMIInterface {

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
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

}
