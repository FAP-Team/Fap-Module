package services;

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

public class ProcedimientosServiceImpl implements ProcedimientosService {

	private static Logger log = Logger.getLogger(ProcedimientosServiceImpl.class);
	
	private ProcedimientosInterface procedimientos;
	
	private PropertyPlaceholder propertyPlaceholder;

	private TiposDocumentosService tiposDocumentosService;
	
	public ProcedimientosServiceImpl(PropertyPlaceholder propertyPlaceholder, TiposDocumentosService tiposDocumentosService) {
		this.propertyPlaceholder = propertyPlaceholder;
		this.tiposDocumentosService = tiposDocumentosService;
		
		URL wsdlProcedimientosURL = Aed.class.getClassLoader().getResource ("wsdl/procedimientos/procedimientos.wsdl");
		procedimientos = new Procedimientos(wsdlProcedimientosURL).getProcedimientos();
		WSUtils.configureEndPoint(procedimientos, getEndPoint());
		PlatinoProxy.setProxy(procedimientos, propertyPlaceholder);
	}
	
	public String getEndPoint(){
		return propertyPlaceholder.get("fap.aed.procedimientos.url");
	}
	
	public String getVersion() throws Exception {
		Holder<String> h1 = new Holder<String>();
		Holder<String> h2 = new Holder<String>();
		procedimientos.obtenerVersionServicio(h1, h2);
		return h1.value;
	}
	
	@Override
	public boolean hasConnection() {
		boolean hasConnection = false;
		try {
			hasConnection = getVersion() != null;
		}catch(Exception e){
			log.info("El servicio no tiene coneccion con " + getEndPoint());
		}
		return hasConnection;
	}
	
	/* (non-Javadoc)
	 * @see services.ProcedimientosService#obtenerDocumentosEnTramite(java.lang.String, java.lang.String)
	 */
	@Override
	public List<models.TipoDocumento> obtenerDocumentosEnTramite(String uriProcedimiento, String uriTramite) throws TiposDocumentosExcepcion, ProcedimientosExcepcion{
		if(uriProcedimiento == null || uriTramite == null){
			throw new NullPointerException();
		}
		
		List<models.TipoDocumento> result = new ArrayList<models.TipoDocumento>();
		List<TipoDocumentoEnTramite> documentos = procedimientos.consultarTiposDocumentosEnTramite(uriProcedimiento, uriTramite).getTiposDocumentos();
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
	
	public List<models.Tramite> obtenerTramites() throws TiposDocumentosExcepcion, ProcedimientosExcepcion {
		String uriProcedimiento = propertyPlaceholder.get("fap.aed.procedimientos.procedimiento.uri"); 
		return obtenerTramites(uriProcedimiento);
	}
	
	/* (non-Javadoc)
	 * @see services.ProcedimientosService#obtenerTramites(java.lang.String)
	 */
	@Override
	public List<models.Tramite> obtenerTramites(String uriProcedimiento) throws TiposDocumentosExcepcion, ProcedimientosExcepcion {
		if(uriProcedimiento == null)
			throw new NullPointerException();
		
		
		List<models.Tramite> result = new ArrayList<models.Tramite>();
		
		ListaTramites tramites = procedimientos.consultarTramites(uriProcedimiento);
		for (Tramite tramite : tramites.getTramites()) {
			models.Tramite tramitedb = new models.Tramite();
			tramitedb.uri = tramite.getUri();
			tramitedb.nombre = tramite.getNombre();
			
			List<models.TipoDocumento> documentosEnTramite = obtenerDocumentosEnTramite(uriProcedimiento, tramitedb.uri);
			tramitedb.documentos.addAll(documentosEnTramite);
			result.add(tramitedb);
		}
		
		return result;
	}
	

	
	/* (non-Javadoc)
	 * @see services.ProcedimientosService#actualizarTramites()
	 */
	@Override
	public boolean actualizarTramites() {
		String uriProcedimiento = propertyPlaceholder.get("fap.aed.procedimientos.procedimiento.uri"); 
		return actualizarTramites(uriProcedimiento);
	}

	public boolean actualizarTramites(String uriProcedimiento) {
		boolean error = false;
		
		List<models.Tramite> tramites = null;
		try {
			tramites = obtenerTramites(uriProcedimiento);
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
	}
	
	public List<Procedimiento> getProcedimientos(){
		List<Procedimiento> result = new ArrayList<Procedimiento>();
	    try {
	    	ListaProcedimientos serviceList = procedimientos.consultarProcedimientos();
	        if (serviceList != null) {
	        	result = serviceList.getProcedimientos();
	        }
	    } catch (Exception ex) {
	        log.error("Se produjo un error al consultar los tipos de procedimientos.", ex);
	    }
        return result;		
	}
	
}
