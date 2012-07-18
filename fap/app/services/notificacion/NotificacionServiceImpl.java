package services.notificacion;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.google.inject.Inject;

import enumerado.fap.gen.EstadoNotificacionEnum;
import es.gobcan.eadmon.aed.ws.Aed;
import es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionException;
import es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionPortType;
import es.gobcan.platino.servicios.enotificacion.notificacion.ResultadoBusquedaNotificacionType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ArrayOfArrayResultadoType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ArrayOfInteresadoType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ArrayOfNotificacionEnvioType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ArrayOfNotificacionType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.DocumentoCreateType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.DocumentoNotificacionEnumType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.DocumentoType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.EstadoNotificacionEnumType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.EstadoNotificacionType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.InteresadoType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.MimeTypeEnumType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.NotificacionCreateType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.NotificacionCriteriaType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.NotificacionEnvioType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.NotificacionType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ResultadoType;

import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;

import models.Agente;
import models.Documento;
import models.DocumentoNotificacion;
import models.Interesado;
import models.Notificacion;
import services.NotificacionService;
import utils.NotificacionUtils;
import utils.WSUtils;

@InjectSupport
public class NotificacionServiceImpl implements NotificacionService {
	
	private final PropertyPlaceholder propertyPlaceholder;
	
	protected static es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionService notificacionService;
	private final NotificacionPortType notificacionPort;
	
	private final static String URL_AED = FapProperties.get("fap.aed.url");
	
	protected static Logger log = Logger.getLogger(NotificacionServiceImpl.class);
	
	private final static String COD_ERROR_NOTIFICACION = "INSERCION_CORRECTA";
	private final static String URL_WSDL = FapProperties.get("fap.notificaciones.url");
	private final static String TIPO_DOC_PUESTAADISPOSICION = FapProperties.get("fap.aed.notificacion.tipodocumento.puestaadisposicion");
	private final static String TIPO_DOC_ANULACION = FapProperties.get("fap.aed.notificacion.tipodocumento.anulacion");
	private final static String TIPO_DOC_MARCARARESPONDIDA = FapProperties.get("fap.aed.notificacion.tipodocumento.marcararespondida");
	
	private final static String EXCEPTION_CON_WS = "El gestor %s no pudo conectarse al servicio de notificaciones para realizar %s de la notificación %s. ";
	private final static String EXCEPTION_DESCONOCIDO = "El gestor %s no pudo realizar la operación de %s para la notificación %s al obtenerse un error desconocido. ";
	
	private final static String MSG_CON_WS = "No se pudo conectar con el servicio de notificaciones. ";
	private final static String MSG_DESCONOCIDO = "Error desconocido. ";
	
	private final static String KEY_CONNECTION_TIMEOUT = "fap.notificacion.proxy.connectiontimeout";
    private final static String KEY_RECEIVE_TIMEOUT = "fap.notificacion.proxy.receivetimeout";
	
	@Inject
	public NotificacionServiceImpl (PropertyPlaceholder propertyPlaceholder) {
		this.propertyPlaceholder = propertyPlaceholder;
		URL wsdlLocation = null;
        try {
              wsdlLocation = new URL(URL_WSDL);
        } catch (MalformedURLException e) {
              log.error("No se puede inicializar la wsdl por defecto " + URL_WSDL);
        }
        
        try {
	        notificacionService = new es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionService(wsdlLocation);
        } catch (Exception e) {
        	log.error("No se ha podido injectar el servicio de notificaciones: " + e.getMessage());
        	notificacionPort = null;
        	return;
        }
	        
        notificacionPort = notificacionService.getNotificacionService();
			
		Client client = ClientProxy.getClient(notificacionPort);
	    HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
	        
	    HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
	    httpClientPolicy.setConnectionTimeout(FapProperties.getLong(KEY_CONNECTION_TIMEOUT));
	    httpClientPolicy.setReceiveTimeout(FapProperties.getLong(KEY_RECEIVE_TIMEOUT));
	        
	    httpConduit.setClient(httpClientPolicy);
 
	}
	
    private String getEndPoint() {
        return propertyPlaceholder.get("fap.notificaciones.url");
    }

	@Override
	public void crearDocumentoPuestaADisposición(List<String> urisDocumentos,
			List<Interesado> interesados, String descripcion) {
		//notificacionPort.crearDocumentoPuestaADisposicion(urisDocumentos, interesados, descripcion);
	}

	@Override
	public void enviarNotificaciones(Notificacion notificacion, Agente gestor) throws NotificacionException {
		String uriNotificacion = notificacion.uri;
		String idGestor = gestor.username; // Se refiere al dni
		
		try {
			
			// Conversor de solicitud a objetos del servicio web
			NotificacionCreateType notificacionCreateType = new NotificacionCreateType();
			notificacionCreateType.setAsunto(notificacion.asunto);
			notificacionCreateType.setCuerpo(notificacion.descripcion);
			notificacionCreateType.setNumeroExpediente(notificacion.idExpedienteAed);
			notificacionCreateType.setPlazoAcceso(notificacion.plazoAcceso);
			notificacionCreateType.setPlazoRespuesta(notificacion.plazoRespuesta);
			notificacionCreateType.setRecordatorioAcceso(notificacion.frecuenciaRecordatorioAcceso);
			notificacionCreateType.setRecordatorioRespuesta(notificacion.frecuenciaRecordatorioRespuesta);
			notificacionCreateType.setUriBackoffice(this.getUriBackOffice()); // Especifica la aplicación
			notificacionCreateType.setUriProcedimiento(this.getUriProcedimiento());
			notificacionCreateType.setVersionProcedimiento("");
			
			// Se añaden los documentos a notificar
			for (DocumentoNotificacion doc : notificacion.documentosANotificar) {
				notificacionCreateType.getUrisDocumentosANotificar().add(doc.uri);
			}
			
			// Se añaden los documentos adjuntos
			for (DocumentoNotificacion doc : notificacion.documentosAnexos) {
				notificacionCreateType.getUrisDocumentosAdjuntos().add(doc.uri);
			}
			
			NotificacionEnvioType notificacionEnvioType = new NotificacionEnvioType();
			
			// Se añaden los datos de la notificación
			notificacionEnvioType.setDatosNotificacion(notificacionCreateType);
			
			// Se añade el documento de envío de notificación
			notificacionEnvioType.setDocumento(null);
			
			ArrayOfInteresadoType interesados = new ArrayOfInteresadoType();
			for (Interesado in : notificacion.interesados) {
				if (in.notificar) { // Debe notificarse?
					InteresadoType interesadoType = NotificacionUtils.convertInteresadoToInteresadoType(in);
					if (interesadoType != null)
						interesados.getInteresados().add(interesadoType);
					else
						play.Logger.error("Hubo un problema al convertir el interesado: "+in.getId()+" a tipo InteresadoType");
				}
			}
			
			// Se añade los interesados
			notificacionEnvioType.setInteresados(interesados);
			
			notificacionEnvioType.setUriGestorDocumental(URL_AED);
			
			ArrayOfNotificacionEnvioType notificaciones = new ArrayOfNotificacionEnvioType();
			notificaciones.getNotificacion().add(notificacionEnvioType);

			String mailGestor = gestor.email;
			
			// Se envía la notificación
			play.Logger.info("Se envía la notificación por el gestor: "+idGestor+ " cuyo email es: "+mailGestor);
			ArrayOfArrayResultadoType enviarNotificacionesResponse = notificacionPort.enviarNotificaciones(notificaciones, idGestor, mailGestor);
			
			uriNotificacion = enviarNotificacionesResponse.getArrayResultado().get(0).getResultado().get(0).getUriNotificacion();
			ResultadoType result0 = enviarNotificacionesResponse.getArrayResultado().get(0).getResultado().get(0);
			
			if (!result0.getCodigoError().equalsIgnoreCase(COD_ERROR_NOTIFICACION)) {
				StringBuilder sb = new StringBuilder();
				sb.append("No se pudo realizar la notificación debido a los siguientes errores: ");
				for (ResultadoType result : enviarNotificacionesResponse.getArrayResultado().get(0).getResultado()) {
					sb.append(String.format("Codigo (%s). ", result.getCodigoError()) + result.getDescripcionError());
				}
				log.info(String.format("No se pudo realizar la notificación del gestor (%s) debido a los siguientes errores: ", idGestor) + sb.toString());
				throw new NotificacionException("No se pudo realizar la notificación debido a los siguientes errores: " + sb.toString());
			}

			log.info(String.format("Notificación (%s) realizada por el gestor (%s).", uriNotificacion, idGestor));
			
			// Se obtiene la uri del documento de notificación
			String uriDoc = null;
			try {
				uriDoc = notificacionPort.obtenerURIDocumentoNotificacion("", uriNotificacion, DocumentoNotificacionEnumType.PUESTA_A_DISPOSICION);
				log.info(String.format("Documento de puesta a disposición (%s) para la notificación (%s)", uriDoc, uriNotificacion));
			} catch (Exception e){
				log.error("Fallo al intentar recuperar la URI del Documento Notificacion. Error: "+e.getMessage());
				log.error("Ojo, la URI del documento puesta a disposicion de la notificacion "+uriNotificacion+" se seteará a NULL en la BBDD local de la aplicación");
			}
			
			// Cumplimentar los campos del documento
			Documento docPuestaADisposicion = new Documento();
			docPuestaADisposicion.clasificado = true;
			docPuestaADisposicion.descripcion = "Documento \"Puesta a disposición\" creado por el servicio de notificaciones";
			docPuestaADisposicion.fechaRegistro = DateTime.now();
			docPuestaADisposicion.fechaSubida = DateTime.now();
			docPuestaADisposicion.tipo = this.getTipoDocPuestaADisposicion();
			docPuestaADisposicion.uri = uriDoc;
			docPuestaADisposicion.estadoDocumento = EstadoNotificacionEnum.puestaadisposicion.name();
		
			docPuestaADisposicion.save();
			
			Documento dbDoc = notificacion.documentoPuestaADisposicion;
			notificacion.documentoPuestaADisposicion = null;
			notificacion.save();
			dbDoc.delete();
			dbDoc = null;

			notificacion.uri = uriNotificacion;
			notificacion.documentoPuestaADisposicion = docPuestaADisposicion;
			notificacion.documentosAuditoria.add(docPuestaADisposicion); // Añadir el documento a la collección de documentos de auditoría
			notificacion.estado = EstadoNotificacionEnum.puestaadisposicion.name(); // Se cambia el estado de la notificación
			
			log.info(String.format("La notificación (%s) pasa al estado de puesta a disposición", uriNotificacion));
			
			// Asignamos el gestor a la notificación
			notificacion.agente = gestor;
			
			notificacion.save();
		}
		catch (es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionException ex1) {
			log.error(String.format(EXCEPTION_CON_WS, idGestor, "puesta a disposición", uriNotificacion), ex1);
			throw new NotificacionException(MSG_CON_WS + ex1.getMessage(), ex1);
		} catch (Exception ex4) {
			log.error(String.format(EXCEPTION_DESCONOCIDO + ex4.getMessage(), idGestor, "puesta a dispoción", uriNotificacion));
			throw new NotificacionException(MSG_DESCONOCIDO + ex4.getMessage(), ex4);
		}
		
	}

	@Override
	public void crearDocumentoAcuseRecibo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recibirAcuseRecibo() {
		// TODO Auto-generated method stub
		
	}

	/* Funcion que consulta el WS de Notificaciones para conocer las notificaciones a raiz de un patrón de búsqueda
	 * 
	 * uriProcedimiento: La uri del procedimiento que queremos saber sus notificaciones
	 */
	@Override
	public List<Notificacion> getNotificaciones(String uriProcedimiento) {
		List<Notificacion> notificacionesWS = new ArrayList<Notificacion>();
		if ((uriProcedimiento == null) || (uriProcedimiento.trim().isEmpty())){
			play.Logger.info("La uri del procedimiento no puede ser vacía");
			return notificacionesWS;
		}
		try {
			NotificacionCriteriaType criterioBusqueda = new NotificacionCriteriaType();
			criterioBusqueda.setUriProcedimiento(uriProcedimiento);
			ResultadoBusquedaNotificacionType resultadoBusqueda = notificacionPort.buscarNotificaciones(criterioBusqueda);
			List<NotificacionType> notificacionesType = resultadoBusqueda.getNotificaciones().getNotificacion();
			for (NotificacionType notificacionType: notificacionesType)
				notificacionesWS.add(NotificacionUtils.convertNotificacionTypeToNotificacion(notificacionType));
			return notificacionesWS;
		} catch (NotificacionException e) {
			play.Logger.error("Error en la llamada al método del Servicio Web de búsqueda de notificaciones: "+e.getMessage());
			return notificacionesWS;
		} catch (Exception e){
			play.Logger.error("Error al intentar obtener las notificaciones del servicio web "+e.getMessage());
			return null;
		}
	}
	
	@Override
	public List<Notificacion> getNotificaciones() {
		List<Notificacion> notificacionesWS = new ArrayList<Notificacion>();
		try {
			NotificacionCriteriaType criterioBusqueda = new NotificacionCriteriaType();
			ResultadoBusquedaNotificacionType resultadoBusqueda = notificacionPort.buscarNotificaciones(criterioBusqueda);
			List<NotificacionType> notificacionesType = resultadoBusqueda.getNotificaciones().getNotificacion();
			for (NotificacionType notificacionType: notificacionesType)
				notificacionesWS.add(NotificacionUtils.convertNotificacionTypeToNotificacion(notificacionType));
			return notificacionesWS;
		} catch (NotificacionException e) {
			play.Logger.info("Error en la llamada al método del Servicio Web de búsqueda de notificaciones: "+e.getMessage());
			return notificacionesWS;
		}
	}

	@Override
	public void estadoNotificacion() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void obtenerNotificacion() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void obtenerDocumentoNotificacion() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void crearDocumentaciónAnulacion() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void anularNotificacion() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void crearDocumentoMarcarComoRespondida() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void marcarNotificacionComoRespondida() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public final String getUriProcedimiento() {
		return FapProperties.get("fap.notificacion.procedimiento");
	}
	
	@Override
	public final String getUriBackOffice() {
		return FapProperties.get("fap.notificacion.backoffice");
	}
	
	protected String getTipoDocPuestaADisposicion() {
		return TIPO_DOC_PUESTAADISPOSICION;
	}

}