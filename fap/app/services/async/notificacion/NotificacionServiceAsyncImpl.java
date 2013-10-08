package services.async.notificacion;

import java.util.List;

import config.InjectorConfig;

import models.Agente;
import models.Documento;
import models.Interesado;
import models.Notificacion;
import models.ResolucionFAP;
import play.libs.F.Promise;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.DocumentoNotificacionEnumType;
import es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionException;
import services.NotificacionService;
import services.async.GenericServiceAsyncImpl;
import services.async.NotificacionServiceAsync;

public class NotificacionServiceAsyncImpl extends GenericServiceAsyncImpl implements NotificacionServiceAsync {
	
	NotificacionService notificacionService = InjectorConfig.getInjector().getInstance(NotificacionService.class);

	@Override
	public Promise<Integer> crearDocumentoPuestaADisposicion(
			Notificacion dbNotificacion, List<String> urisDocumentos,
			List<Interesado> interesados, String descripcion) {
		// TODO Auto-generated method stub
    	Object[] params = {dbNotificacion, urisDocumentos, interesados, descripcion};
		Class[] types = {Notificacion.class, List.class, List.class, String.class};
		return (Promise<Integer>) execute(notificacionService, "crearDocumentoPuestaADisposicion", params, types);
	}

	@Override
	public Promise<Integer> enviarNotificaciones(Notificacion notificacion, Agente gestor) throws NotificacionException {
		// TODO Auto-generated method stub
    	Object[] params = {notificacion, gestor};
		Class[] types = {Notificacion.class, Agente.class};
		return (Promise<Integer>) execute(notificacionService, "enviarNotificaciones", params, types);
	}

	@Override
	public Promise<Integer> crearDocumentoAcuseRecibo(Notificacion dbNotificacion, String dniInteresado) {
		// TODO Auto-generated method stub
    	Object[] params = {dbNotificacion, dniInteresado};
		Class[] types = {Notificacion.class, String.class};
		return (Promise<Integer>) execute(notificacionService, "crearDocumentoAcuseRecibo", params, types);
	}

	@Override
	public Promise<Integer> enviarAcuseRecibo(Notificacion dbNotificacion, String dniInteresado, String firma) {
		// TODO Auto-generated method stub
    	Object[] params = {dbNotificacion, dniInteresado, firma};
		Class[] types = {Notificacion.class, String.class, String.class};
		return (Promise<Integer>) execute(notificacionService, "enviarAcuseRecibo", params, types);
	}

	@Override
	public Promise<List<Notificacion>> getNotificaciones(String uriProcedimiento) {
		// TODO Auto-generated method stub
    	Object[] params = {uriProcedimiento};
		Class[] types = {String.class};
		return (Promise<List<Notificacion>>) execute(notificacionService, "getNotificaciones", params, types);
	}

	@Override
	public Promise<List<Notificacion>> getNotificaciones() {
		return (Promise<List<Notificacion>>) execute(notificacionService, "getNotificaciones");
	}

	@Override
	public Promise<String> estadoNotificacion(String uriNotificacion) {
    	Object[] params = {uriNotificacion};
		Class[] types = {String.class};
		return (Promise<String>) execute(notificacionService, "estadoNotificacion", params, types);
	}

	@Override
	public Promise<Notificacion> obtenerNotificacion(String uriNotificacion) {
    	Object[] params = {uriNotificacion};
		Class[] types = {String.class};
		return (Promise<Notificacion>) execute(notificacionService, "obtenerNotificacion", params, types);
	}

	@Override
	public Promise<Documento> obtenerDocumentoNotificacion(String idUsuario, String uriNotificacion, DocumentoNotificacionEnumType tipoDocumento) {
		Object[] params = {idUsuario, uriNotificacion, tipoDocumento};
		Class[] types = {String.class, String.class, DocumentoNotificacionEnumType.class};
		return (Promise<Documento>) execute(notificacionService, "obtenerDocumentoNotificacion", params, types);
	}

	@Override
	public Promise<Integer> crearDocumentacionAnulacion(Notificacion dbNotificacion) {
    	Object[] params = {dbNotificacion};
		Class[] types = {Notificacion.class};
		return (Promise<Integer>) execute(notificacionService, "crearDocumentacionAnulacion", params, types);
	}

	@Override
	public Promise<Integer> anularNotificacion(Notificacion dbNotificacion, String firma) {
    	Object[] params = {dbNotificacion, firma};
		Class[] types = {Notificacion.class, String.class};
		return (Promise<Integer>) execute(notificacionService, "anularNotificacion", params, types);
	}

	@Override
	public Promise<Integer> crearDocumentoMarcarComoRespondida(Notificacion dbNotificacion) {
    	Object[] params = {dbNotificacion};
		Class[] types = {Notificacion.class};
		return (Promise<Integer>) execute(notificacionService, "crearDocumentoMarcarComoRespondida", params, types);
	}

	@Override
	public Promise<Integer> marcarNotificacionComoRespondida(Notificacion dbNotificacion, String firma) {
    	Object[] params = {dbNotificacion, firma};
		Class[] types = {Notificacion.class, String.class};
		return (Promise<Integer>) execute(notificacionService, "marcarNotificacionComoRespondida", params, types);
	}

	@Override
	public Promise<String> getUriProcedimiento() {
		return (Promise<String>) execute(notificacionService, "getUriProcedimiento");
	}

	@Override
	public Promise<String> getUriBackOffice() {
		return (Promise<String>) execute(notificacionService, "getUriBackOffice");
	}

	@Override
	public Promise<Boolean> isConfigured() {
		return (Promise<Boolean>) execute(notificacionService, "isConfigured");
	}

	@Override
	public Promise<Integer> mostrarInfoInyeccion() {
		return (Promise<Integer>) execute(notificacionService, "mostrarInfoInyeccion");
	}

	@Override
	public Promise<String> obtenerUriDocumentoNotificacion(String idUsuario, String uriNotificacion, DocumentoNotificacionEnumType tipoDocumento) {
		Object[] params = {idUsuario, uriNotificacion, tipoDocumento};
		Class[] types = {String.class, String.class, DocumentoNotificacionEnumType.class};
		return (Promise<String>) execute(notificacionService, "obtenerUriDocumentoNotificacion", params, types);
	}

}
