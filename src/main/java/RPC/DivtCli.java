package RPC;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DivtCli {

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
				if (args.length == 1) {
					System.out.println("You should specify block hash");
					break;
				}
				String hash = args[1];
				if (hash.length() == 64) {
					String block = stub.getBlock(hash);
					System.out.println((block != null) ? block : "There is no block with such hash");
				} else {
					System.out.println("Invalid hash");
				}
				break;
				case "getblockhash":
				if(args.length == 1) {
				    System.out.println("You should specify block hash");
					break; 
				}
				try { 
					System.out.println(stub.getblockhash(Integer.parseInt(args[1].replaceAll("[\'\"]|[\'\"]","").trim()))); 
				}
				catch(IndexOutOfBoundsException e) { 
					System.out.println("Therer is no block with such index");	
				}
				catch (NumberFormatException e) { 
					System.out.println("You have entered invalid type !");
				}
				break;
			case "getdifficulty":
				System.out.println(stub.getDifficulty());
				break;
			case "getblockchaininfo":
				System.out.println(stub.getBlockchainInfo());
				break;
			case "setmining":
				if (args.length == 1) {
					System.out.println("You should specify 'true' or 'false'");
					break;
				}
				String isMining = args[1].replaceAll("[\'\"]", "").trim();
				if (isMining.equalsIgnoreCase("true") || isMining.equalsIgnoreCase("false")) {
					boolean toMine = Boolean.parseBoolean(isMining);
					stub.setMining(toMine);
				} else {
					System.out.println("Invalid parameter. You should specify 'true' or 'false'");
				}
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
		builder.append("getmininginfo\n");
		return builder.toString();
	}
}
