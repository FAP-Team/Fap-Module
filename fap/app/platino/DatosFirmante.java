package platino;

import javax.xml.datatype.XMLGregorianCalendar;


public class DatosFirmante {
	private String idFirmante;
	private String descFirmante;
	private XMLGregorianCalendar fechaFirma;
	private String uriFirmante;
	private String cargoFirmante;
	
	public XMLGregorianCalendar getFechaFirma() {
		return fechaFirma;
	}
	public void setFechaFirma(XMLGregorianCalendar fechaFirma) {
		this.fechaFirma = fechaFirma;
	}
	public String getUriFirmante() {
		return uriFirmante;
	}
	public void setUriFirmante(String uriFirmante) {
		this.uriFirmante = uriFirmante;
	}
	public String getCargoFirmante() {
		return cargoFirmante;
	}
	public void setCargoFirmante(String cargoFirmante) {
		this.cargoFirmante = cargoFirmante;
	}
	public String getDescFirmante() {
		return descFirmante;
	}
	public void setDescFirmante(String descFirmante) {
		this.descFirmante = descFirmante;
	}
	public String getIdFirmante() {
		return idFirmante;
	}
	public void setIdFirmante(String idFirmante) {
		this.idFirmante = idFirmante;
	}
}
