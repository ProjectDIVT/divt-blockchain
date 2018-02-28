package RPC;

import java.io.InputStream;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.json.JSONObject;

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
			printJSon(67);
	}

	@Override
	public String help() throws RemoteException {
		// Return help options
		return null;
	}	
	
	public static void printJSon(int numbers) {
		JSONObject json = new JSONObject();
		json.put("number", numbers);
		System.out.println(json.getInt("number"));
	}
	
}
