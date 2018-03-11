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
				System.out.println(printHelp());
				return;
			}
			switch (args[0]) {
			case "help":
				System.out.println(printHelp());
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
			case "getblockhash":
				//validate
				System.out.println(stub.getblockhash(Integer.parseInt(args[1])));
				break;
			case "getdifficulty":
				System.out.println(stub.getDifficulty());
				break;
			case "getblockchaininfo":
				System.out.println(stub.getBlockchainInfo());
 				break;
			case "setmining":
				//validate
				boolean toMine = Boolean.parseBoolean(args[1]);
				stub.setMining(toMine);
 				break;
			case "getmininginfo":
				System.out.println(stub.getMiningInfo());
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

	private static String printHelp() {
		StringBuilder builder = new StringBuilder();
		builder.append("== Blockchain ==\n");
		builder.append("getbestblockhash\n");
		builder.append("getblock \"hash\"\n");
		builder.append("getblockchaininfo\n");
		builder.append("getblockhash\n");
		builder.append("getdifficulty\n");
		builder.append("\n");
		builder.append("== Control ==\n");
		builder.append("getinfo\n");
		builder.append("stop\n");
		builder.append("\n");
		builder.append("== Mining ==\n");
		builder.append("setmining\n");
		return builder.toString();
	}
}
