package services.filesystem;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import models.Agente;
import models.Documento;
import models.DocumentoNotificacion;
import models.FasesRegistro;
import models.Firmantes;
import models.InformacionRegistro;
import models.Interesado;
import models.Notificacion;
import models.Persona;
import models.PersonaFisica;
import models.Registro;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import services.NotificacionService;
import services.notificacion.NotificacionServiceImpl;
import utils.NotificacionUtils;

import com.google.inject.Inject;

import enumerado.fap.gen.EstadoNotificacionEnum;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ArrayOfArrayResultadoType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ArrayOfInteresadoType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ArrayOfNotificacionEnvioType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.DocumentoNotificacionEnumType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.InteresadoType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.NotificacionCreateType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.NotificacionCriteriaType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.NotificacionEnvioType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.NotificacionType;
import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ResultadoType;
import es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionException;
import es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionPortType;
import es.gobcan.platino.servicios.enotificacion.notificacion.ResultadoBusquedaNotificacionType;


@InjectSupport
public class FileSystemNotificacionServiceImpl implements NotificacionService {
	
	protected static es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionService notificacionService;	
	protected static Logger log = Logger.getLogger(NotificacionServiceImpl.class);
	private final static String TIPO_DOC_PUESTAADISPOSICION = FapProperties.get("fap.aed.notificacion.tipodocumento.puestaadisposicion");

	
	
    private String getEndPoint() { 
    	return "fs//endPoint";
    }
    
    @Override
	public void crearDocumentoPuestaADisposicion(List<String> urisDocumentos,
			List<Interesado> interesados, String descripcion) {
		
	}
    
    public void enviarNotificaciones(Notificacion notificacion, Agente gestor) throws NotificacionException {
		log.info(String.format("La notificación pasa al estado de puesta a disposición"));
			// Asignamos el gestor a la notificación
			notificacion.agente = gestor;
			notificacion.save();
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
		List<Notificacion> notificaciones = new ArrayList<Notificacion>();
		if ((uriProcedimiento == null) || (uriProcedimiento.trim().isEmpty())){
			play.Logger.info("La uri del procedimiento no puede ser vacía");
			return notificaciones;
		}

		notificaciones.add(crearNotificacion(1));
		notificaciones.add(crearNotificacion(2));
		notificaciones.add(crearNotificacion(3));
		return notificaciones;
	}
	
	@Override
	public List<Notificacion> getNotificaciones() {
		List<Notificacion> notificaciones = new ArrayList<Notificacion>();
		notificaciones.add(crearNotificacion(1));
		notificaciones.add(crearNotificacion(2));
		notificaciones.add(crearNotificacion(3));
		notificaciones.add(crearNotificacion(4));
		notificaciones.add(crearNotificacion(5));
		return notificaciones;
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
	public void crearDocumentacionAnulacion() {
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

	public List<Interesado> crearInteresado(int num){
		List<Interesado> lista = new ArrayList<Interesado>();
		Interesado i = new Interesado();
		i.persona = new Persona();
		i.movil = "678"+num;
		i.uriTerceros= "fs//uriTerceros"+num;
		i.email = "a"+num+"@email.es";
		lista.add(i);
		return lista;
	}
	
	public Notificacion crearNotificacion(int num){
		List<DocumentoNotificacion> documentosANotificar = new ArrayList<DocumentoNotificacion>();
		documentosANotificar.add(new DocumentoNotificacion("fs://docNotificacion"+num+".1"));
		documentosANotificar.add(new DocumentoNotificacion("fs://docNotificacion"+num+".2"));
		String idExpediente = "FAP2012"+num;
		
		
		Notificacion n = new Notificacion(documentosANotificar, crearInteresado(num), idExpediente); 
		n.uri= "fs://notificacion"+num;
		n.uriProcedimiento = "fs://procedimiento"+num;
		n.descripcion = "Descripcion notificacion de prueba "+num;
		if (num % 2 == 0){
			n.estado = EstadoNotificacionEnum.creada.name();
		}else{
			n.estado = EstadoNotificacionEnum.puestaadisposicion.name();
		}
		n.asunto = "Asunto de prueba "+num;
		n.fechaPuestaADisposicion = new DateTime();
		n.fechaAcceso = new DateTime();
		
		n.documentosAnexos.add(new DocumentoNotificacion("fs://docAnexo"+num+".1"));
		n.documentosAuditoria.add(new Documento());
		n.documentoPuestaADisposicion = new Documento();
		n.registro = crearRegistro(num);
		return n;
	}

	public Registro crearRegistro(int num){
		Registro r = new Registro();
		r.borrador = new Documento();
		r.oficial = new Documento();
		r.justificante = new Documento();
		r.autorizacionFuncionario = new Documento();
		r.informacionRegistro.fechaRegistro = new DateTime();
		r.fasesRegistro = new FasesRegistro();
		r.firmantes = new Firmantes(); //Comprobar si es necesario rellenarlo
		return r;
	}
	
}
