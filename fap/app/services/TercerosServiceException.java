package services;

public class TercerosServiceException extends Exception {

	public TercerosServiceException(String msg){
		super(msg);
	}
	
	public TercerosServiceException(String message, Throwable cause){
        super(message, cause);
    }
}
