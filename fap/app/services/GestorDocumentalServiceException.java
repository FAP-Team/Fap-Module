package services;

public class GestorDocumentalServiceException extends Exception {

	public GestorDocumentalServiceException(String message){
		super(message);
	}
	
	public GestorDocumentalServiceException(String message, Throwable cause){
	    super(message, cause);
	}
	
}
