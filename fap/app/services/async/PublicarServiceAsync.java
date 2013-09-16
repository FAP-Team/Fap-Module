package services.async;

import play.libs.F.Promise;

public interface PublicarServiceAsync {
	
	public Promise<Integer> mostrarInfoInyeccion();
	
	public Promise<Integer> getInfo();
	
}
