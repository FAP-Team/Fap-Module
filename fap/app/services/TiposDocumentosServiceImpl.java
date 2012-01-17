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

public class TiposDocumentosServiceImpl implements TiposDocumentosService {

	private static Logger log = Logger.getLogger(TiposDocumentosServiceImpl.class);

	private PropertyPlaceholder propertyPlaceholder;
	
	private TiposDocumentosInterface tipos;
	
	public TiposDocumentosServiceImpl(PropertyPlaceholder propertyPlaceholder){
		this.propertyPlaceholder = propertyPlaceholder;
		
		URL wsdlTipoURL = Aed.class.getClassLoader().getResource ("wsdl/tipos-documentos/tipos-documentos.wsdl");
		tipos = new TiposDocumentos(wsdlTipoURL).getTiposDocumentos();
		WSUtils.configureEndPoint(tipos, getEndPoint());
		PlatinoProxy.setProxy(tipos, propertyPlaceholder);
	}
	
	public String getEndPoint(){
		return propertyPlaceholder.get("fap.aed.tiposdocumentos.url");
	}
	
	public String getVersion() throws Exception {
		Holder<String> h1 = new Holder<String>();
		Holder<String> h2 = new Holder<String>();
		tipos.obtenerVersionServicio(h1, h2);
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
		
		return tipos.obtenerTipoDocumento(uri);
	}

}
