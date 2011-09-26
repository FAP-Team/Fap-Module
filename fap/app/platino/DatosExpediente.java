package platino;

import javax.xml.datatype.XMLGregorianCalendar;

public class DatosExpediente {
	private boolean creadoPlatino;
	private String numero;
	private String uri;
	private XMLGregorianCalendar fechaApertura; //XMLGregorianCalendar gregCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(2010,07,12));
	private String ruta;
	
	public boolean isCreadoPlatino() {
		return creadoPlatino;
	}
	public void setCreadoPlatino(boolean creadoPlatino) {
		this.creadoPlatino = creadoPlatino;
	}
	public String getNumero() {
		return numero;
	}
	public void setNumero(String numero) {
		this.numero = numero;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public XMLGregorianCalendar getFechaApertura() {
		return fechaApertura;
	}
	public void setFechaApertura(XMLGregorianCalendar fechaApertura) {
		this.fechaApertura = fechaApertura;
	}
	
	public String getRuta() {
		return ruta;
	}
	public void setRuta(String ruta) {
		this.ruta = ruta;
	}
	
	
}
