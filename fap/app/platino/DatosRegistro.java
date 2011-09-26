package platino;

import java.io.InputStream;

import javax.activation.DataSource;
import javax.xml.datatype.XMLGregorianCalendar;

import es.gobcan.platino.servicios.registro.Asunto;
import es.gobcan.platino.servicios.registro.Documento;
import es.gobcan.platino.servicios.registro.Documentos;

public class DatosRegistro {
	private DatosExpediente expediente;
	private String nombreRemitente;
	private String tipoTransporte; // opcional
	private String tipoDocumento; // opcional N,E,C por defecto NIF
	private String numeroDocumento; // NIF(N), o NIE(E) o CIF(C) documento
	private XMLGregorianCalendar fecha;
	private DatosDocumento documento;

	public DatosExpediente getExpediente() {
		return expediente;
	}

	public void setExpediente(DatosExpediente expediente) {
		this.expediente = expediente;
	}

	public String getNombreRemitente() {
		return nombreRemitente;
	}

	public void setNombreRemitente(String nombreRemitente) {
		this.nombreRemitente = nombreRemitente;
	}

	public String getTipoTransporte() {
		return tipoTransporte;
	}

	public void setTipoTransporte(String tipoTransporte) {
		this.tipoTransporte = tipoTransporte;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public XMLGregorianCalendar getFecha() {
		return fecha;
	}

	public void setFecha(XMLGregorianCalendar fecha) {
		this.fecha = fecha;
	}

	public DatosDocumento getDocumento() {
		return documento;
	}

	public void setDocumento(DatosDocumento documento) {
		this.documento = documento;
	}

	public static Documento documentoSGRDEToRegistro(DataSource dataSource,
			String uriDocumentoPlatino) throws Exception {
		Documento documento = new Documento();
		// URN
		documento.setNombre(uriDocumentoPlatino);
		// Hash
		InputStream inputStream = dataSource.getInputStream();
		documento.setHash(PlatinoSecurityUtils.obtenerHash(inputStream));
		// documentosGestorDocumental.getDocumento().add(documento);
		return documento;
	}
}
