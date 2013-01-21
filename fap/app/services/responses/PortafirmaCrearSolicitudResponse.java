package services.responses;

/**
 * La solicitud de firma del portafirma debería devolver un objeto de este tipo.
 *
 */
public class PortafirmaCrearSolicitudResponse {

	private String comentarios;
	private String idSolicitud;
	
	public String getComentarios() {
		return comentarios;
	}
	public void setComentarios(String comentarios) {
		this.comentarios = comentarios;
	}
	public String getIdSolicitud() {
		return idSolicitud;
	}
	public void setIdSolicitud(String idSolicitud) {
		this.idSolicitud = idSolicitud;
	}
	
}
