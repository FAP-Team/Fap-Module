package platino;

import java.io.InputStream;

import javax.activation.DataHandler;
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
	private String asunto = null;
	private String unidadOrganica = null;

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
		InputStream inputStream = dataSource.getInputStream();
		byte[] arrayByte = PlatinoSecurityUtils.obtenerHash(inputStream);
		// URN
		documento.setNombre(uriDocumentoPlatino);
		// Hash
		dataSource = new javax.mail.util.ByteArrayDataSource(arrayByte, "xml");
		DataHandler dataHandler = new DataHandler(dataSource);
		documento.setHash(dataHandler);
		return documento;
	}
	
	public void setUnidadOrganica(String unidadOrganica) {
		this.unidadOrganica = unidadOrganica;
	}

	public String getUnidadOrganica() {
		return this.unidadOrganica;
	}

	public String getAsunto() {
		return this.asunto;
	}

	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}
}
