package services.platino;

public class ProcedimientosServiceException extends Exception {

	public ProcedimientosServiceException(String message){
		super(message);
	}
	
	public ProcedimientosServiceException (String message, Throwable cause){
	    super(message, cause);
	}
}
