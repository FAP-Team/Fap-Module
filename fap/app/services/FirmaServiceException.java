package services;

public class FirmaServiceException extends Exception {

	public FirmaServiceException(String msg){
		super(msg);
	}
	
	public FirmaServiceException(String message, Throwable cause){
        super(message, cause);
    }
}
