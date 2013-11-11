package typetodo.exception;

@SuppressWarnings("serial")
public class InvalidCommandException extends Exception {
	public InvalidCommandException() {
		super();
	}

	public InvalidCommandException(String message) {
		super(message);
	}
}
