package services;

public class RegistroServiceException extends Exception {
	public RegistroServiceException(String message) {
		super(message);
	}
	
	public RegistroServiceException(String message, Throwable cause){
	    super(message, cause);
	}
}
