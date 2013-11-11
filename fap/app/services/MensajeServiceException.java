package services;

public class MensajeServiceException extends Exception {
	public MensajeServiceException(String msg){
		super(msg);
	}
	
	public MensajeServiceException(String message, Throwable cause){
        super(message, cause);
    }
}
