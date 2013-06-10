package services.ticketing;

public class TicketingServiceException extends Exception {

	public TicketingServiceException(String message){
		super(message);
	}
	
	public TicketingServiceException(String message, Throwable cause){
	    super(message, cause);
	}
	
}
