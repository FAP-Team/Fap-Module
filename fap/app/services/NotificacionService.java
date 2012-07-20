package services;

import java.util.List;

import es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionException;

import models.Agente;
import models.Interesado;
import models.Notificacion;

public interface NotificacionService {
	
	/// 3.1 RF01 - Poner a disposición una notificación
	public void crearDocumentoPuestaADisposicion (List<String> urisDocumentos, List<Interesado> interesados, String descripcion);
	
	public void enviarNotificaciones (Notificacion notificacion, Agente gestor) throws NotificacionException;
	
	/// 3.2 RF02 - Acusar recibo de una notificación
	public void crearDocumentoAcuseRecibo ();
	
	public void recibirAcuseRecibo ();
	
	public List<Notificacion> getNotificaciones (String uriProcedimiento);
	
	public List<Notificacion> getNotificaciones ();
	
	public void estadoNotificacion ();

	public void obtenerNotificacion ();

	public void obtenerDocumentoNotificacion ();
	
	/// 3.4 RF04 - Anular una notificación
	public void crearDocumentacionAnulacion ();

	public void anularNotificacion ();
	
	public void crearDocumentoMarcarComoRespondida ();
	
	public void marcarNotificacionComoRespondida ();
	
	public String getUriProcedimiento() ;
	
	public String getUriBackOffice() ;
}