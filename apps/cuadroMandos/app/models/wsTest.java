package models;

import javax.persistence.Entity;

@Entity
public class wsTest extends FapModel {
	// Código de los atributos

	public String nombre = "WS A";
	public String ruta = "http://pppp.com";
	
	public void init() {
		postInit();
	}
	
	@Override
	public String toString() {
	   return "Info [nombre=" + nombre + ", ruta=" + ruta + "]";
	}

}