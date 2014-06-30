package services.platino;

import java.net.URL;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.soap.MTOMFeature;

import models.ExpedientePlatino;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;

import platino.DatosDocumento;
import platino.DatosFirmante;
import platino.DatosRegistro;
import platino.PlatinoProxy;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import services.GestorDocumentalServiceException;
import services.TercerosServiceException;
import utils.WSUtils;
import es.gobcan.platino.servicios.localizaciones.IslaItem;
import es.gobcan.platino.servicios.localizaciones.LocalizacionesServiceBean;
import es.gobcan.platino.servicios.localizaciones.MunicipioItem;
import es.gobcan.platino.servicios.localizaciones.PaisItem;
import es.gobcan.platino.servicios.localizaciones.ProvinciaItem;
import es.gobcan.platino.servicios.registro.Documento;
import es.gobcan.platino.servicios.registro.Documentos;
import es.gobcan.platino.servicios.sgrde.DocumentoBase;
import es.gobcan.platino.servicios.sgrde.DocumentoExpediente;
import es.gobcan.platino.servicios.sgrde.ErrorInternoException;
import es.gobcan.platino.servicios.sgrde.Expediente;
import es.gobcan.platino.servicios.sgrde.FirmasElectronicas;
import es.gobcan.platino.servicios.sgrde.InformacionFirmaElectronica;
import es.gobcan.platino.servicios.sgrde.MetaInformacionException;
import es.gobcan.platino.servicios.sgrde.SGRDEServicePortType;
import es.gobcan.platino.servicios.sgrde.SGRDEServiceProxy;

/**
 * GestorDocumentalServiceImpl
 */
public class PlatinoLocalizacionesService {
    private static Logger log = Logger.getLogger(PlatinoLocalizacionesService.class);

    private final PropertyPlaceholder propertyPlaceholder;
    private volatile LocalizacionesServiceBean localizacionesPort;

    public PlatinoLocalizacionesService(PropertyPlaceholder propertyPlaceholder) {
        this.propertyPlaceholder = propertyPlaceholder;

        URL wsdlURL = PlatinoLocalizacionesService.class.getClassLoader().getResource("wsdl/localizaciones.wsdl");
        localizacionesPort = new es.gobcan.platino.servicios.localizaciones.LocalizacionesService(wsdlURL).getLocalizacionesService();

        WSUtils.configureEndPoint(localizacionesPort, getEndPoint());
        WSUtils.configureSecurityHeaders(localizacionesPort, propertyPlaceholder);
        PlatinoProxy.setProxy(localizacionesPort, propertyPlaceholder);
    }

    public boolean hasConnection() {
        boolean result = false;
        try {
            result = getVersion() != null;
        } catch (Exception e) {
            log.error("El servicio web de Localizaciones no tiene conexion con " + getEndPoint());
        }
        return result;
    }

    private String getEndPoint() {
        return propertyPlaceholder.get("fap.platino.localizaciones.url");
    }

    private String getVersion() {
        return localizacionesPort.getVersion();
    }
    
    protected LocalizacionesServiceBean getLocalizacionesPort(){
    	return this.localizacionesPort;
    }
    
    private TercerosServiceException newTercerosServiceException(String msg, Exception cause){
	    return new TercerosServiceException(msg, cause);
	}
    
    public ProvinciaItem recuperarProvincia(Long idProvincia) throws TercerosServiceException{
		if (idProvincia == null)
			throw newTercerosServiceException("Fallo al recuperar Provincia por id null", null);
		try {
			return localizacionesPort.recuperarProvincia(idProvincia);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al recuperar Provincia por id: "+idProvincia, e);
		}
	}

	public PaisItem recuperarPais(Long idPais) throws TercerosServiceException{
		if (idPais == null)
			throw newTercerosServiceException("Fallo al recuperar Pais por id null", null);
		try {
			return localizacionesPort.recuperarPais(idPais);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al recuperar Pais por id: "+idPais, e);
		}
	}	

	public MunicipioItem recuperarMunicipio(Long idProvincia, Long idMunicipio) throws TercerosServiceException{
		if ((idMunicipio == null) || (idProvincia == null))
			throw newTercerosServiceException("Fallo al recuperar Municipio por id null de Provincia o Municipio", null);
		try {
			return localizacionesPort.recuperarMunicipio(idProvincia, idMunicipio);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al recuperar Municipio por idMunicipio: "+idMunicipio+" e idProvincia "+idProvincia, e);
		}
	}	
	
	public IslaItem recuperarIsla(Long idIsla) throws TercerosServiceException{
		if (idIsla == null)
			throw newTercerosServiceException("Fallo al recuperar Isla por id null", null);
		try {
			return localizacionesPort.recuperarIsla(idIsla);
		} catch (Exception e) {
			throw newTercerosServiceException("Fallo al recuperar Isla por idIsla: "+idIsla, e);
		}
	}

}
