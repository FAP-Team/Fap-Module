package services.aed;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Holder;

import models.TableKeyValue;

import org.apache.log4j.Logger;

import platino.PlatinoProxy;
import play.db.jpa.JPAPlugin;
import play.test.Fixtures;
import properties.PropertyPlaceholder;
import services.GestorDocumentalServiceException;
import utils.WSUtils;
import es.gobcan.eadmon.aed.ws.Aed;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.TiposDocumentosExcepcion;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;
import es.gobcan.eadmon.procedimientos.ws.Procedimientos;
import es.gobcan.eadmon.procedimientos.ws.ProcedimientosExcepcion;
import es.gobcan.eadmon.procedimientos.ws.ProcedimientosInterface;
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
	}
	
	private String getEndPoint(){
		return propertyPlaceholder.get("fap.aed.procedimientos.url");
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
			
			//Consulta al WS de Tipos de Documentos la descripción
			TipoDocumento td = tiposDocumentosService.getTipoDocumento(tipoDocumento.getUri());
			tipoDocumentoDb.nombre = td.getDescripcion();	
			
			result.add(tipoDocumentoDb);
		}
		return result;
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
