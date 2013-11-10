package typetodo.exception;

@SuppressWarnings("serial")
public class InvalidFieldNameException extends Exception {
	public InvalidFieldNameException() {
		super();
	}

	public InvalidFieldNameException(String message) {
		super(message);
	}
}