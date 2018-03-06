package RPC;

import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class DivtCli implements Serializable{

	public static void main(String[] args) {

		try {
			Registry registry = LocateRegistry.getRegistry(1099);

			RMIInterface stub = (RMIInterface) registry.lookup("divt");

			if (args.length < 1) {
				// Print the help call
				return;
			}
			switch (args[0]) {
			// Add your call here :D
			case "help":
				// Print the help call
				break;
			case "asd":
				break;
			case "stop":
				stub.stop();
				break;
			case "getbestblockhash":
				System.out.println(stub.getBestBlockHash());
				break;
			case "getblock":
				// validate 
				System.out.println(stub.getBlock(args[1]));
				break;
			case "getblockchaininfo":
				System.out.println(stub.getBlockchainInfo());
				break;
			default:
				System.out.println("No such command");
				break;
			}
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
