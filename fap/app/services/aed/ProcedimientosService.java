package services.aed;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Holder;

import messages.Messages;
import models.CodigoRequerimiento;
import models.TableKeyValue;
import models.TipoCodigoExclusion;
import models.TiposCodigoRequerimiento;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;

import platino.PlatinoProxy;
import play.db.jpa.JPAPlugin;
import play.test.Fixtures;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import services.GestorDocumentalServiceException;
import utils.WSUtils;
import es.gobcan.eadmon.aed.ws.Aed;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.TiposDocumentosExcepcion;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;
import es.gobcan.eadmon.procedimientos.ws.Procedimientos;
import es.gobcan.eadmon.procedimientos.ws.ProcedimientosExcepcion;
import es.gobcan.eadmon.procedimientos.ws.ProcedimientosInterface;
import es.gobcan.eadmon.procedimientos.ws.dominio.AportadoPorEnum;
import es.gobcan.eadmon.procedimientos.ws.dominio.CodigoExclusion;
import es.gobcan.eadmon.procedimientos.ws.dominio.ListaCodigosExclusion;
import es.gobcan.eadmon.procedimientos.ws.dominio.ListaCodigosRequerimiento;
import es.gobcan.eadmon.procedimientos.ws.dominio.ListaProcedimientos;
import es.gobcan.eadmon.procedimientos.ws.dominio.ListaTramites;
import es.gobcan.eadmon.procedimientos.ws.dominio.Procedimiento;
import es.gobcan.eadmon.procedimientos.ws.dominio.TipoDocumentoEnTramite;
import es.gobcan.eadmon.procedimientos.ws.dominio.Tramite;

/**
 * ProcedimientosServiceImpl
 * 
 * El servicio esta preparado para inicializarse de forma lazy.
 * Por lo tanto siempre que se vaya a consumir el servicio web
 * se deberia acceder a "getProcedimientosPort" en lugar de acceder directamente
 * a la property
 * 
 */
public class ProcedimientosService {

	private static Logger log = Logger.getLogger(ProcedimientosService.class);
	
	private final ProcedimientosInterface procedimientosPort;
	
	private final PropertyPlaceholder propertyPlaceholder;

	private final TiposDocumentosService tiposDocumentosService;
	
	public ProcedimientosService(PropertyPlaceholder propertyPlaceholder, TiposDocumentosService tiposDocumentosService) {
	    this.propertyPlaceholder = propertyPlaceholder;
        this.tiposDocumentosService = tiposDocumentosService;
        URL wsdlProcedimientosURL = Aed.class.getClassLoader().getResource ("wsdl/procedimientos/procedimientos.wsdl");
        procedimientosPort = new Procedimientos(wsdlProcedimientosURL).getProcedimientos();
        WSUtils.configureEndPoint(procedimientosPort, getEndPoint());
        PlatinoProxy.setProxy(procedimientosPort, propertyPlaceholder);
        
        Client client = ClientProxy.getClient(procedimientosPort);
		HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpClientPolicy.setReceiveTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpConduit.setClient(httpClientPolicy);
	}
	
	private String getEndPoint(){
		return propertyPlaceholder.get("fap.aed.procedimientos.url");
	}
	
	protected ProcedimientosInterface getProcedimientoPort(){
		return this.procedimientosPort;
	}
	
	private List<models.TipoDocumento> getDocumentosEnTramite(String uriProcedimiento, String uriTramite) throws GestorDocumentalServiceException, ProcedimientosExcepcion{
		if(uriProcedimiento == null || uriTramite == null){
			throw new NullPointerException();
		}
		
		List<models.TipoDocumento> result = new ArrayList<models.TipoDocumento>();
		List<TipoDocumentoEnTramite> documentos = procedimientosPort.consultarTiposDocumentosEnTramite(uriProcedimiento, uriTramite).getTiposDocumentos();
		for(TipoDocumentoEnTramite tipoDocumento : documentos){
			models.TipoDocumento tipoDocumentoDb  = new models.TipoDocumento();
			
			tipoDocumentoDb.uri = tipoDocumento.getUri();
			tipoDocumentoDb.aportadoPor = tipoDocumento.getAportadoPor().toString();
			tipoDocumentoDb.obligatoriedad = tipoDocumento.getObligatoriedad().toString();
			tipoDocumentoDb.cardinalidad = tipoDocumento.getCardinalidad().toString();
			tipoDocumentoDb.tramitePertenece = uriTramite;
			
			//Consulta al WS de Tipos de Documentos la descripción
			TipoDocumento td = tiposDocumentosService.getTipoDocumento(tipoDocumento.getUri());
			tipoDocumentoDb.nombre = td.getDescripcion();	
			
			result.add(tipoDocumentoDb);
			setCodigosRequerimientosTipoDocumento(tipoDocumento.getUri(), uriTramite);
		}
		return result;
	}
	
	private void setCodigosRequerimientosTipoDocumento (String uriTipoDocumento, String uriTramite) throws GestorDocumentalServiceException, ProcedimientosExcepcion{
		List<CodigoRequerimiento> codigosReq = getCodigosRequerimientos (uriTramite, uriTipoDocumento);
		for (CodigoRequerimiento codigo: codigosReq){
			models.TiposCodigoRequerimiento tipoCodReqdb = new models.TiposCodigoRequerimiento();
			tipoCodReqdb.codigo = codigo.codigo;
			tipoCodReqdb.descripcion = codigo.descripcion;
			tipoCodReqdb.descripcionCorta = codigo.descripcionCorta;
			tipoCodReqdb.uriTipoDocumento = uriTipoDocumento;
			tipoCodReqdb.uriTramite = uriTramite;
			tipoCodReqdb.save();
		}
	}
	
	private List<CodigoRequerimiento> getCodigosRequerimientos (String tramiteUri, String tipoDocumentoUri){
		try {
			ListaCodigosRequerimiento listaCodigos = procedimientosPort.consultarCodigosRequerimiento(propertyPlaceholder.get("fap.aed.procedimientos.procedimiento.uri"), tramiteUri, tipoDocumentoUri);
			return fromListaCodigosRequerimientoWS2List(listaCodigos);
		} catch (ProcedimientosExcepcion e) {
			play.Logger.error("No se han podido obtener los codigos de exclusion asociados al tipo de Documento: "+e.getMessage());
			Messages.error("No se han podido obtener los codigos de exclusion asociados al tipo de Documento");
		}
		return null;
	}
	
	private List<CodigoRequerimiento> fromListaCodigosRequerimientoWS2List(ListaCodigosRequerimiento listCodReq){
        List<CodigoRequerimiento> list = new ArrayList<CodigoRequerimiento>();
        
        if(listCodReq != null){
           for(es.gobcan.eadmon.procedimientos.ws.dominio.CodigoRequerimiento codReq : listCodReq.getCodigosRequerimiento()){
              CodigoRequerimiento nuevo = new CodigoRequerimiento();
              nuevo.codigo = codReq.getCodigo();
              nuevo.descripcionCorta = codReq.getDescripcionCorta();
              nuevo.descripcion = codReq.getDescripcion();
              list.add(nuevo);
           }
        }
        return list;
	}
		
	private List<models.Tramite> getTramites(String uriProcedimiento) throws ProcedimientosExcepcion, GestorDocumentalServiceException {
		if(uriProcedimiento == null)
			throw new NullPointerException();
		
		List<models.Tramite> result = new ArrayList<models.Tramite>();
		ListaTramites tramites = procedimientosPort.consultarTramites(uriProcedimiento);
		for (Tramite tramite : tramites.getTramites()) {
			models.Tramite tramitedb = new models.Tramite();
			tramitedb.uri = tramite.getUri();
			tramitedb.nombre = tramite.getNombre();
			
			List<models.TipoDocumento> documentosEnTramite = getDocumentosEnTramite(uriProcedimiento, tramitedb.uri);
			tramitedb.documentos.addAll(documentosEnTramite);
			result.add(tramitedb);
		}
		return result;
	}
	
	public List<models.Tramite> getTramites() throws GestorDocumentalServiceException {
	    String uriProcedimiento = propertyPlaceholder.get("fap.aed.procedimientos.procedimiento.uri");
	    try {
	        return getTramites(uriProcedimiento);
	    }catch(Exception e){
	        throw new GestorDocumentalServiceException("Error al obtener los trámites", e);
	    }
	}
	
	public List<TipoDocumentoEnTramite> getTiposDocumentosAportadosCiudadano (models.Tramite tramite) {
		String uriProcedimiento = propertyPlaceholder.get("fap.aed.procedimientos.procedimiento.uri");
		
		play.Logger.info("Obteniendo tipos de documento aportados por el ciudadano en el trámite "+tramite.uri);
		List<TipoDocumentoEnTramite> listaTodos = new ArrayList<TipoDocumentoEnTramite>();
		List<TipoDocumentoEnTramite> listaCiudadanos = new ArrayList<TipoDocumentoEnTramite>();
		try {
			listaTodos = procedimientosPort.consultarTiposDocumentosEnTramite(uriProcedimiento, tramite.uri).getTiposDocumentos();
		} catch (ProcedimientosExcepcion e) {
			play.Logger.error("No se han podido consultar los tipos de documentos aportados por el ciudadano: "+e.getMessage());
			Messages.error("No se han podido consultar los tipos de documentos aportados por el ciudadano");
		}
		
		for (TipoDocumentoEnTramite tipoDoc : listaTodos) {
			if (tipoDoc.getAportadoPor() == AportadoPorEnum.CIUDADANO) {
				listaCiudadanos.add(tipoDoc);
			}			
		}
		
		return listaCiudadanos;
	}
	
	public List<TipoDocumento> getListTiposDocumentosAportadosCiudadano (models.Tramite tramite) {
		List<TipoDocumento> tiposDocumentos = new ArrayList<TipoDocumento>();
		List<TipoDocumentoEnTramite> listaCiudadanos = getTiposDocumentosAportadosCiudadano(tramite);
		for (TipoDocumentoEnTramite tipoDoc : listaCiudadanos) {
			try {
				TipoDocumento tipoDocumento = tiposDocumentosService.getTipoDocumento(tipoDoc.getUri());
				tiposDocumentos.add(tipoDocumento);
			} catch (GestorDocumentalServiceException e) {
				play.Logger.error("No se han podido obtener el tipo de Documento a partir de su uri: "+e.getMessage());
				Messages.error("No se han podido obtener el tipo de Documento a partir de su uri");
			}
		}
		return tiposDocumentos;
	}
	
	public boolean actualizarCodigosExclusion() {
		String uriProcedimiento = propertyPlaceholder.get("fap.aed.procedimientos.procedimiento.uri"); 

		try {
			TipoCodigoExclusion.deleteAll();
			
			ListaCodigosExclusion codigosExclusionWS = procedimientosPort.consultarCodigosExclusion(uriProcedimiento);
			List<TipoCodigoExclusion> listaCodExc = utils.ConvertWSUtils.codigosExclusionWS2List(codigosExclusionWS);
			for (TipoCodigoExclusion tipo : listaCodExc) {
				tipo.save();
			}
		} catch (Exception e) {
			play.Logger.error("No se han podido cargar los codigo de exclusión "+e.getMessage());
			return false;
		}
		return true;
	}
	
	/*
	protected boolean actualizarTramites() {
		String uriProcedimiento = propertyPlaceholder.get("fap.aed.procedimientos.procedimiento.uri"); 
		return actualizarTramites(uriProcedimiento);
	}

	protected boolean actualizarTramites(String uriProcedimiento) {
		boolean error = false;
		
		List<models.Tramite> tramites = null;
		try {
			tramites = getTramites(uriProcedimiento);
		}catch (ProcedimientosExcepcion e) {
			WSUtils.aedError("Se produjo un error actualizando los trámites del procedimiento " + uriProcedimiento, e);
			error = true;
		} catch (TiposDocumentosExcepcion e) {
			WSUtils.aedError("Se produjo un error actualizando los trámites del procedimiento " + uriProcedimiento, e);
			error = true;
		}
		
		if(tramites != null){			
			//Borra los trámites y los tipos de documentos antiguos
			Fixtures.delete(models.Tramite.class, models.TipoDocumento.class);
			
			//Guarda los trámites nuevo
			for(models.Tramite tramite : tramites){
				tramite.save();
			}
			
			//Añade el tipo y la descripción a la tabla de tablas
			List<models.TipoDocumento> tiposDocumentos = models.TipoDocumento.findAll();
			String table = "tiposDocumentos";
			TableKeyValue.deleteTable(table);
			for(models.TipoDocumento tipo : tiposDocumentos){
				TableKeyValue.setValue(table, tipo.uri, tipo.nombre, false);
			}
			TableKeyValue.renewCache(table); //Renueva la cache una única vez
		}
		
		return !error;
	}*/
	
}
