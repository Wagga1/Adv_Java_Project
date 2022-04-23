package investments;

public class InvalidDataException extends Exception {
	@Override
	public String getMessage() {
		return "Data must be provided.";
	}
}
