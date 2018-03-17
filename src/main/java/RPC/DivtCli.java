package RPC;

import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JOptionPane;


public class DivtCli implements Serializable{

	public static void main(String[] args) throws Exception {

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
				JOptionPane.showMessageDialog(null, "You have stopped the mining process");
				break;
			case "getbestblockhash":
				System.out.println(stub.getBestBlockHash());
				break;
			case "getblock":
				String hash = args[1].replaceAll("[\'\"]", "").trim();
				if (hash.length() == 64) {
					String block = stub.getBlock(hash);
					if (block == null) {
						System.out.println("There is no block with such hash");
					} else {
						System.out.println(stub.getBlock(hash));
					}
				} else {
					System.out.println("Invalid hash");
				}
				break;
			case "getblockhash":
				if(args.length == 1) {
				    System.out.println("You should specify block hash");
					break; 
				}
				try { System.out.println(stub.getblockhash(Integer.parseInt(args[1].replaceAll("[\'\"]|[\'\"]","").trim()))); 
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
				//validate
				boolean toMine = Boolean.parseBoolean(args[1]);
				stub.setMining(toMine);
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
