package typetodo.exception;

@SuppressWarnings("serial")
public class ReservedCharacterException extends Exception {
	public ReservedCharacterException() {
		super();
	}

	public ReservedCharacterException(String message) {
		super(message);
	}
}