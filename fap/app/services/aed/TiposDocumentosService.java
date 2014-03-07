package services.aed;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javassist.NotFoundException;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
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
import services.GestorDocumentalServiceException;
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
public class TiposDocumentosService {

	private final PropertyPlaceholder propertyPlaceholder;
	
	private final TiposDocumentosInterface tiposPort;
	
	public TiposDocumentosService(PropertyPlaceholder propertyPlaceholder){
	    this.propertyPlaceholder = propertyPlaceholder;
        URL wsdlTipoURL = Aed.class.getClassLoader().getResource ("wsdl/tipos-documentos/tipos-documentos.wsdl");
        tiposPort = new TiposDocumentos(wsdlTipoURL).getTiposDocumentos();
        WSUtils.configureEndPoint(tiposPort, getEndPoint());
        PlatinoProxy.setProxy(tiposPort, propertyPlaceholder);  
	}
		
	public String getEndPoint(){
		return propertyPlaceholder.get("fap.aed.tiposdocumentos.url");
	}
	
	protected TiposDocumentosInterface getTiposPort(){
		return this.tiposPort;
	}
		
	public TipoDocumento getTipoDocumento(String uri) throws GestorDocumentalServiceException {
		if(uri == null)
			throw new NullPointerException();
		try {
		    return tiposPort.obtenerTipoDocumento(uri);
		}catch(TiposDocumentosExcepcion e){
		    throw new GestorDocumentalServiceException("Error recuperando el tipo de documento : " + e.getFaultInfo().getDescripcion(), e);
		}
	}

}
