package blockchain;
public class ValidationBlockException extends Exception {
	String message;
	
	public ValidationBlockException(String message) {
		this.message = message;
	}
	@Override
	public String getMessage() {
		return message;
	}
}

