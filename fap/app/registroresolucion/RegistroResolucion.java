package registroresolucion;

import org.joda.time.DateTime;

public class RegistroResolucion {
	
	public int numero;
	public int primerFolio;
	public int ultimoFolio;
	public DateTime fecha;
	
	public RegistroResolucion(int numero, int primerFolio, int ultimoFolio, DateTime fecha) {
		this.numero = numero;
		this.primerFolio = primerFolio;
		this.ultimoFolio = ultimoFolio;
		this.fecha = fecha;
	}
}
