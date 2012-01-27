package services;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javassist.NotFoundException;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.apache.log4j.Logger;

import messages.Messages;
import models.ObligatoriedadDocumentos;
import models.Quartz;
import models.Singleton;
import models.TableKeyValue;

import platino.PlatinoProxy;
import play.db.jpa.JPABase;
import play.db.jpa.JPAPlugin;
import play.test.Fixtures;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import tags.StringUtils;
import utils.WSUtils;

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
import es.gobcan.eadmon.procedimientos.ws.dominio.ListaProcedimientos;
import es.gobcan.eadmon.procedimientos.ws.dominio.ListaTiposDocumentosEnTramite;
import es.gobcan.eadmon.procedimientos.ws.dominio.ListaTramites;
import es.gobcan.eadmon.procedimientos.ws.dominio.ObligatoriedadEnum;
import es.gobcan.eadmon.procedimientos.ws.dominio.Procedimiento;
import es.gobcan.eadmon.procedimientos.ws.dominio.TipoDocumentoEnTramite;
import es.gobcan.eadmon.procedimientos.ws.dominio.Tramite;
import es.gobcan.eadmon.procedimientos.ws.servicios.ObtenerTramite;
import es.gobcan.eadmon.verificacion.ws.dominio.ListaDocumentosVerificacion;

/**
 * TiposDocumentosServiceImpl
 * 
 * El servicio esta preparado para inicializarse de forma lazy.
 * Por lo tanto siempre que se vaya a consumir el servicio web
 * se deberia acceder a "tiposPort" en lugar de acceder directamente
 * a la property
 * 
 */
public class TiposDocumentosServiceImpl implements TiposDocumentosService {

	private static Logger log = Logger.getLogger(TiposDocumentosServiceImpl.class);

	private PropertyPlaceholder propertyPlaceholder;
	
	private TiposDocumentosInterface tiposPort;
	
	public TiposDocumentosServiceImpl(PropertyPlaceholder propertyPlaceholder){
		init(propertyPlaceholder, false);
	}
	
	public TiposDocumentosServiceImpl(PropertyPlaceholder propertyPlaceholder, boolean eagerInitialization){
		init(propertyPlaceholder, eagerInitialization);
	}
	
	private void init(PropertyPlaceholder propertyPlaceholder, boolean eagerInitialization){
		this.propertyPlaceholder = propertyPlaceholder;
		if(eagerInitialization){
			getTiposPort();
		}
	}
	
	private TiposDocumentosInterface getTiposPort(){
		if(tiposPort == null){
			URL wsdlTipoURL = Aed.class.getClassLoader().getResource ("wsdl/tipos-documentos/tipos-documentos.wsdl");
			tiposPort = new TiposDocumentos(wsdlTipoURL).getTiposDocumentos();
			WSUtils.configureEndPoint(tiposPort, getEndPoint());
			PlatinoProxy.setProxy(tiposPort, propertyPlaceholder);
		}
		return tiposPort;
	}
	
	public String getEndPoint(){
		return propertyPlaceholder.get("fap.aed.tiposdocumentos.url");
	}
	
	public String getVersion() throws Exception {
		Holder<String> h1 = new Holder<String>();
		Holder<String> h2 = new Holder<String>();
		getTiposPort().obtenerVersionServicio(h1, h2);
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
	
	public TipoDocumento getTipoDocumento(String uri) throws TiposDocumentosExcepcion {
		if(uri == null)
			throw new NullPointerException();
		
		return getTiposPort().obtenerTipoDocumento(uri);
	}

}
