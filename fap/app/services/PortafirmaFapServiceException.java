package services;

public class PortafirmaFapServiceException extends Exception {

	public PortafirmaFapServiceException(String msg){
		super(msg);
	}
	
	public PortafirmaFapServiceException(String message, Throwable cause){
        super(message, cause);
    }
	
}
