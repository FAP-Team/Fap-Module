package aed;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.apache.log4j.Logger;

import models.TableKeyValue;

import platino.PlatinoProxy;
import play.db.jpa.JPAPlugin;
import properties.FapProperties;

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
import es.gobcan.eadmon.procedimientos.ws.dominio.ListaTiposDocumentosEnTramite;
import es.gobcan.eadmon.procedimientos.ws.dominio.TipoDocumentoEnTramite;
import es.gobcan.eadmon.procedimientos.ws.dominio.Tramite;
import es.gobcan.eadmon.procedimientos.ws.servicios.ObtenerTramite;
import es.gobcan.eadmon.verificacion.ws.dominio.ListaDocumentosVerificacion;

public class TiposDocumentosClient {

	private static TiposDocumentosInterface tipos;
	private static ProcedimientosInterface procedimientos;
	private static Logger log = Logger.getLogger(TiposDocumentosClient.class);
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
	
	public static boolean actualizarTiposDocumentoDB() {
		String uriTramite = FapProperties.get("fap.aed.procedimientos.tramite.uri");
		String uriProcedimiento = FapProperties.get("fap.aed.procedimientos.procedimiento.uri");
				
		List<TipoDocumentoEnTramite> listaTodos = new ArrayList<TipoDocumentoEnTramite>();
		List<TipoDocumentoEnTramite> listaCiudadanos = new ArrayList<TipoDocumentoEnTramite>();
		List<TipoDocumentoEnTramite> listaOrganismos = new ArrayList<TipoDocumentoEnTramite>();
		List<TipoDocumentoEnTramite> listaOtrasEntidades = new ArrayList<TipoDocumentoEnTramite>();

		try {
			listaTodos = procedimientos.consultarTiposDocumentosEnTramite(uriProcedimiento, uriTramite).getTiposDocumentos();
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
		
		boolean todos = actualizarDocumentosDB(listaTodos, "tipoDocumentosTodos");
		boolean ciudadano = actualizarDocumentosDB(listaCiudadanos, "tipoDocumentosCiudadanos");
		boolean organismo = actualizarDocumentosDB(listaOrganismos, "tipoDocumentosOrganismos");
		boolean otrasEntidades = actualizarDocumentosDB(listaOtrasEntidades, "tipoDocumentosOtrasEntidades");
		
		return todos && ciudadano && organismo && otrasEntidades;
	}
	
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
	
}
