package services.async;

import java.util.List;

import play.libs.F.Promise;

import models.Agente;
import models.Documento;
import models.Interesado;
import models.Notificacion;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.DocumentoNotificacionEnumType;
import es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionException;

public interface NotificacionServiceAsync {
		/// 3.1 RF01 - Poner a disposición una notificación
		public Promise<Integer> crearDocumentoPuestaADisposicion (Notificacion dbNotificacion, List<String> urisDocumentos, List<Interesado> interesados, String descripcion);
		
		public Promise<Integer> enviarNotificaciones (Notificacion notificacion, Agente gestor) throws NotificacionException;
		
		/// 3.2 RF02 - Acusar recibo de una notificación
		public Promise<Integer> crearDocumentoAcuseRecibo (Notificacion dbNotificacion, String dniInteresado);
		
		public Promise<Integer> enviarAcuseRecibo (Notificacion dbNotificacion, String dniInteresado, String firma);
		
		public Promise<List<Notificacion> > getNotificaciones (String uriProcedimiento);
		
		public Promise<List<Notificacion> > getNotificaciones ();
		
		public Promise<String> estadoNotificacion (String uriNotificacion);

		public Promise<Notificacion> obtenerNotificacion (String uriNotificacion);

		public Promise<Documento> obtenerDocumentoNotificacion (String idUsuario, String uriNotificacion, DocumentoNotificacionEnumType tipoDocumento);
		
		/// 3.4 RF04 - Anular una notificación
		public Promise<Integer> crearDocumentacionAnulacion (Notificacion dbNotificacion);

		public Promise<Integer> anularNotificacion (Notificacion dbNotificacion, String firma);
		
		public Promise<Integer> crearDocumentoMarcarComoRespondida (Notificacion dbNotificacion);
		
		public Promise<Integer> marcarNotificacionComoRespondida (Notificacion dbNotificacion, String firma);
		
		public Promise<String> getUriProcedimiento() ;
		
		public Promise<String> getUriBackOffice() ;

		public Promise<Boolean> isConfigured();
		
		public Promise<Integer> mostrarInfoInyeccion();
		
		public Promise<String> obtenerUriDocumentoNotificacion(String idUsuario, String uriNotificacion, DocumentoNotificacionEnumType tipoDocumento);
}
