package services;

import java.util.List;

import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.DocumentoNotificacionEnumType;
import es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionException;

import models.Agente;
import models.Documento;
import models.Interesado;
import models.Notificacion;

public interface NotificacionService {
	
	/// 3.1 RF01 - Poner a disposición una notificación
	public void crearDocumentoPuestaADisposicion (Notificacion dbNotificacion, List<String> urisDocumentos, List<Interesado> interesados, String descripcion);
	
	public void enviarNotificaciones (Notificacion notificacion, Agente gestor) throws NotificacionException;
	
	/// 3.2 RF02 - Acusar recibo de una notificación
	public void crearDocumentoAcuseRecibo (Notificacion dbNotificacion, String dniInteresado);
	
	public void enviarAcuseRecibo (Notificacion dbNotificacion, String dniInteresado, String firma);
	
	public List<Notificacion> getNotificaciones (String uriProcedimiento);
	
	public List<Notificacion> getNotificaciones ();
	
	public String estadoNotificacion (String uriNotificacion);

	public Notificacion obtenerNotificacion (String uriNotificacion);

	public Documento obtenerDocumentoNotificacion (String idUsuario, String uriNotificacion, DocumentoNotificacionEnumType tipoDocumento);
	
	/// 3.4 RF04 - Anular una notificación
	public void crearDocumentacionAnulacion (Notificacion dbNotificacion);

	public void anularNotificacion (Notificacion dbNotificacion, String firma);
	
	public void crearDocumentoMarcarComoRespondida (Notificacion dbNotificacion);
	
	public void marcarNotificacionComoRespondida (Notificacion dbNotificacion, String firma);
	
	public String getUriProcedimiento() ;
	
	public String getUriBackOffice() ;

	public boolean isConfigured();
	
	public void mostrarInfoInyeccion();
	
	public String obtenerUriDocumentoNotificacion(String idUsuario, String uriNotificacion, DocumentoNotificacionEnumType tipoDocumento); 
}