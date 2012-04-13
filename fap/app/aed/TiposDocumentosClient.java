package aed;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javassist.NotFoundException;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.apache.log4j.Logger;

import messages.Messages;
import models.CodigoRequerimiento;
import models.Quartz;
import models.Singleton;
import models.TableKeyValue;

import platino.PlatinoProxy;
import play.db.jpa.JPABase;
import play.db.jpa.JPAPlugin;
import play.test.Fixtures;
import properties.FapProperties;
import tags.StringUtils;
import utils.ObligatoriedadDocumentosFap;

import es.gobcan.eadmon.aed.ws.Aed;
import es.gobcan.eadmon.aed.ws.AedPortType;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.TiposDocumentos;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.TiposDocumentosExcepcion;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.TiposDocumentosInterface;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.Grupo;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.GrupoTipoDocumento;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.ListaGrupoTipoDocumento;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;
import es.gobcan.eadmon.procedimientos.ws.Procedimientos;
import es.gobcan.eadmon.procedimientos.ws.ProcedimientosExcepcion;
import es.gobcan.eadmon.procedimientos.ws.ProcedimientosInterface;
import es.gobcan.eadmon.procedimientos.ws.dominio.AportadoPorEnum;
import es.gobcan.eadmon.procedimientos.ws.dominio.ListaCodigosExclusion;
import es.gobcan.eadmon.procedimientos.ws.dominio.ListaCodigosRequerimiento;
import es.gobcan.eadmon.procedimientos.ws.dominio.ListaTiposDocumentosEnTramite;
import es.gobcan.eadmon.procedimientos.ws.dominio.ListaTramites;
import es.gobcan.eadmon.procedimientos.ws.dominio.ObligatoriedadEnum;
import es.gobcan.eadmon.procedimientos.ws.dominio.TipoDocumentoEnTramite;
import es.gobcan.eadmon.procedimientos.ws.dominio.Tramite;
import es.gobcan.eadmon.procedimientos.ws.servicios.ObtenerTramite;
import es.gobcan.eadmon.verificacion.ws.dominio.ListaDocumentosVerificacion;

public class TiposDocumentosClient {

	protected static TiposDocumentosInterface tipos;
	protected static ProcedimientosInterface procedimientos;
	protected static Logger log = Logger.getLogger(TiposDocumentosClient.class);
	static {		
		URL wsdlTipoURL = Aed.class.getClassLoader().getResource ("wsdl/tipos-documentos/tipos-documentos.wsdl");
		tipos = new TiposDocumentos(wsdlTipoURL).getTiposDocumentos();
		
		BindingProvider bpTipo = (BindingProvider) tipos;
		bpTipo.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, FapProperties.get("fap.aed.tiposdocumentos.url"));
		
		PlatinoProxy.setProxy(tipos);
		
		//procedimientos
		URL wsdlProcedimientosURL = Aed.class.getClassLoader().getResource ("wsdl/procedimientos/procedimientos.wsdl");
		procedimientos = new Procedimientos(wsdlProcedimientosURL).getProcedimientos();
		
		BindingProvider bpProcedimientos = (BindingProvider) procedimientos;
		bpProcedimientos.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, FapProperties.get("fap.aed.procedimientos.url"));
		
		PlatinoProxy.setProxy(procedimientos);
		
	}

	public static String getVersion() throws Exception {
		Holder<String> h1 = new Holder<String>();
		Holder<String> h2 = new Holder<String>();
		tipos.obtenerVersionServicio(h1, h2);
		procedimientos.obtenerVersionServicio(h1, h2);
		return h1.value;
	}
	
	
	public static boolean actualizarTramites() {
		String uriProcedimiento = FapProperties.get("fap.aed.procedimientos.procedimiento.uri"); 
		JPAPlugin.startTx(false);

		try {
			//Borra los trámites antiguos
			Fixtures.delete(models.Tramite.class);
			Fixtures.delete(models.TipoDocumento.class);
			
			//Recupera los trámites y los tipos de documentos asociados
			ListaTramites tramites = procedimientos.consultarTramites(uriProcedimiento);
			for (Tramite tramite : tramites.getTramites()) {				
				models.Tramite tramitedb = new models.Tramite();
				tramitedb.uri = tramite.getUri();
				tramitedb.nombre = tramite.getNombre();
				
				List<TipoDocumentoEnTramite> documentos = procedimientos.
								consultarTiposDocumentosEnTramite(uriProcedimiento, tramite.getUri()).getTiposDocumentos();
				
				for(TipoDocumentoEnTramite tipoDocumento : documentos){
					models.TipoDocumento tipoDocumentoDb  = new models.TipoDocumento();
					
					tipoDocumentoDb.uri = tipoDocumento.getUri();
					tipoDocumentoDb.aportadoPor = tipoDocumento.getAportadoPor().toString();
					tipoDocumentoDb.obligatoriedad = tipoDocumento.getObligatoriedad().toString();
					tipoDocumentoDb.tramitePertenece = tramite.getUri();
					tipoDocumentoDb.cardinalidad = tipoDocumento.getCardinalidad().toString();
					
					//Consulta al WS de Tipos de Documentos la descripción
					TipoDocumento td = tipos.obtenerTipoDocumento(tipoDocumento.getUri());
					tipoDocumentoDb.nombre = td.getDescripcion();	
					
					tramitedb.documentos.add(tipoDocumentoDb);
					
					List<CodigoRequerimiento> codigosReq = getCodigosRequerimientos (tramite.getUri(), tipoDocumento.getUri());
					for (CodigoRequerimiento codigo: codigosReq){
						models.TiposCodigoRequerimiento tipoCodReqdb = new models.TiposCodigoRequerimiento();
						tipoCodReqdb.codigo = codigo.codigo;
						tipoCodReqdb.descripcion = codigo.descripcion;
						tipoCodReqdb.descripcionCorta = codigo.descripcionCorta;
						tipoCodReqdb.uriTipoDocumento = tipoDocumento.getUri();
						tipoCodReqdb.uriTramite = tramite.getUri();
						tipoCodReqdb.save();
					}
				}
				
				tramitedb.save();
			}
			
			//Añade el tipo y la descripción a la tabla de tablas
			List<models.TipoDocumento> tiposDocumentos = models.TipoDocumento.findAll();
			String table = "tiposDocumentos";
			TableKeyValue.deleteTable(table);
			for(models.TipoDocumento tipo : tiposDocumentos){
				TableKeyValue.setValue(table, tipo.uri, tipo.nombre, false);
			}
			TableKeyValue.renewCache(table); //Renueva la cache una única vez
			
		} catch (ProcedimientosExcepcion e) {
			aedError("Se produjo un error en el servicio web de Procedimientos"+ uriProcedimiento, e);
			JPAPlugin.closeTx(true);
			return false;
		} catch(TiposDocumentosExcepcion e){
			aedError("Se produjo un error en el servicio web de TiposDocumenetos"+ uriProcedimiento, e);
			JPAPlugin.closeTx(true);
			return false;
		}
		JPAPlugin.closeTx(false);
		return true;
	}
	
	protected static void aedError(String error, ProcedimientosExcepcion e){
		aedError(error, e.getFaultInfo().getDescripcion());
	}
	
	protected static void aedError(String error, TiposDocumentosExcepcion e){
		aedError(error, e.getFaultInfo().getDescripcion());
	}
	
	protected static void aedError(String error, String descripcion){
		log.error(error + " - descripcion: "+ descripcion);
		Messages.error(error);		
	}
	
	
	/**
	 * Se actualizará la lista de tipos de documentos para cada trámite.
	 * 
	 * Ejemplo: Para el tramite "solicitud", se actualizarán las listas "tipoDocumentosCiudadanosSolicitud"
	 * 
	 * @return
	 */
	public static boolean actualizarTiposDocumentoDB() {
		play.Logger.info("Actualizando los Tipos de Documentos en lA BBDD");
		//String uriTramite = FapProperties.get("fap.aed.procedimientos.tramite.uri");
		String uriProcedimiento = FapProperties.get("fap.aed.procedimientos.procedimiento.uri");
		
		//Recupera los trámites y los tipos de documentos asociados
		ListaTramites tramites = null;
		try {
			tramites = procedimientos.consultarTramites(uriProcedimiento);
		} catch (ProcedimientosExcepcion e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		boolean result = true;
		for (Tramite tramite : tramites.getTramites()) {
			play.Logger.info("Trámite: "+tramite.getNombre());
				
			List<TipoDocumentoEnTramite> listaTodos = new ArrayList<TipoDocumentoEnTramite>();
			List<TipoDocumentoEnTramite> listaCiudadanos = new ArrayList<TipoDocumentoEnTramite>();
			List<TipoDocumentoEnTramite> listaOrganismos = new ArrayList<TipoDocumentoEnTramite>();
			List<TipoDocumentoEnTramite> listaOtrasEntidades = new ArrayList<TipoDocumentoEnTramite>();

			try {
				listaTodos = procedimientos.consultarTiposDocumentosEnTramite(uriProcedimiento, tramite.getUri()).getTiposDocumentos();
			}catch(Exception e){
				log.error("Se produjo un error al consultar los tipos de Documentos", e);
				return false;
			}
		
			for (TipoDocumentoEnTramite tipoDoc : listaTodos) {
				if (tipoDoc.getAportadoPor() == AportadoPorEnum.CIUDADANO) {
					listaCiudadanos.add(tipoDoc);
				}else if (tipoDoc.getAportadoPor() == AportadoPorEnum.ORGANISMO) {
					listaOrganismos.add(tipoDoc);				
				}else if (tipoDoc.getAportadoPor() == AportadoPorEnum.OTRAS_ENTIDADES) {
					listaOtrasEntidades.add(tipoDoc);				
				}				
			}
		
			boolean todos = actualizarDocumentosDB(listaTodos, "tipoDocumentos");
			boolean ciudadano = actualizarDocumentosDB(listaCiudadanos, "tipoDocumentosCiudadanos"+StringUtils.firstUpper(tramite.getNombre()));
			boolean organismo = actualizarDocumentosDB(listaOrganismos, "tipoDocumentosOrganismos"+StringUtils.firstUpper(tramite.getNombre()));
			boolean otrasEntidades = actualizarDocumentosDB(listaOtrasEntidades, "tipoDocumentosOtrasEntidades"+StringUtils.firstUpper(tramite.getNombre()));
		
			boolean obligatoriedad = actualizarObligatoriedadDocumentos(tramite.getNombre());
		
			result = result && todos && ciudadano && organismo && otrasEntidades && obligatoriedad;
		}
		return result;
	}
	
	/**
	 * 
	 * @param lista
	 * @param table
	 * @return
	 */
	public static boolean actualizarDocumentosDB(List<TipoDocumentoEnTramite> lista, String table) {
		JPAPlugin.startTx(false);
		TableKeyValue.deleteTable(table);		
		try {
			for(TipoDocumentoEnTramite tipoDoc : lista){
				String uriTipoDocumento = tipoDoc.getUri();
				TipoDocumento tipoDocumento = tipos.obtenerTipoDocumento(uriTipoDocumento);
				TableKeyValue.setValue(table, uriTipoDocumento, tipoDocumento.getDescripcion(), false);
			}
		}catch(Exception e){
			log.error("Se produjo un error actualizando la tabla " + table + ". Rollback", e);
			JPAPlugin.closeTx(true);
			return false;
		}
		TableKeyValue.renewCache(table); //Renueva la cache
		JPAPlugin.closeTx(false);
		log.debug("lista de documentos de la tabla " + table + " actualizada");
		return true;
	}

	
	public static boolean actualizarObligatoriedadDocumentos(String tramite) {
		JPAPlugin.startTx(false);
		long idTramite = models.Tramite.find("select id from Tramite where nombre=?", tramite).first();
		ObligatoriedadDocumentosFap docObli = (ObligatoriedadDocumentosFap)ObligatoriedadDocumentosFap.find("select docObli from ObligatoriedadDocumentosFap docObli join docObli.tramite tramite where tramite.id=?", idTramite).first();
    	if (docObli != null)
    		docObli.delete();
		ObligatoriedadDocumentosFap docObliNew = new ObligatoriedadDocumentosFap(tramite);
		docObliNew.save();
		JPAPlugin.closeTx(false);
		return true;
	}
	
	public static List<TipoDocumentoEnTramite> getTiposDocumentosAportadosCiudadano (models.Tramite tramite) {
		String uriProcedimiento = FapProperties.get("fap.aed.procedimientos.procedimiento.uri");
		
		play.Logger.info("Obteniendo tipos de documento aportados por el ciudadano en el trámite "+tramite.uri);
		List<TipoDocumentoEnTramite> listaTodos = new ArrayList<TipoDocumentoEnTramite>();
		List<TipoDocumentoEnTramite> listaCiudadanos = new ArrayList<TipoDocumentoEnTramite>();
		try {
			listaTodos = procedimientos.consultarTiposDocumentosEnTramite(uriProcedimiento, tramite.uri).getTiposDocumentos();
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
	
	public static List<TipoDocumento> getListTiposDocumentosAportadosCiudadano (models.Tramite tramite) {
		List<TipoDocumento> tiposDocumentos = new ArrayList<TipoDocumento>();
		List<TipoDocumentoEnTramite> listaCiudadanos = getTiposDocumentosAportadosCiudadano(tramite);
		for (TipoDocumentoEnTramite tipoDoc : listaCiudadanos) {
			try {
				TipoDocumento tipoDocumento = tipos.obtenerTipoDocumento(tipoDoc.getUri());
				tiposDocumentos.add(tipoDocumento);
			} catch (TiposDocumentosExcepcion e) {
				play.Logger.error("No se han podido obtener el tipo de Documento a partir de su uri: "+e.getMessage());
				Messages.error("No se han podido obtener el tipo de Documento a partir de su uri");
			}
		}
		return tiposDocumentos;
	}

	public static List<CodigoRequerimiento> getCodigosRequerimientos (String tramiteUri, String tipoDocumentoUri){
		try {
			ListaCodigosRequerimiento listaCodigos = procedimientos.consultarCodigosRequerimiento(FapProperties.get("fap.aed.procedimientos.procedimiento.uri"), tramiteUri, tipoDocumentoUri);
			return fromListaCodigosRequerimientoWS2List(listaCodigos);
		} catch (ProcedimientosExcepcion e) {
			play.Logger.error("No se han podido obtener los codigos de exclusion asociados al tipo de Documento: "+e.getMessage());
			Messages.error("No se han podido obtener los codigos de exclusion asociados al tipo de Documento");
		}
		return null;
	}
	
	public static List<CodigoRequerimiento> fromListaCodigosRequerimientoWS2List(ListaCodigosRequerimiento listCodReq){
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
	
}
