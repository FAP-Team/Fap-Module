package services.filesystem;

import java.io.IOException;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import messages.Messages;
import models.Agente;
import models.Solicitante;

import net.java.dev.jaxb.array.StringArray;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;

import org.joda.time.DateTime;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import enumerado.fap.gen.SexoEnum;

import platino.KeystoreCallbackHandler;
import platino.PlatinoCXFSecurityHeaders;
import platino.PlatinoProxy;
import play.libs.Codec;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import services.TercerosServiceException;
import sun.security.pkcs.PKCS7;
import utils.BinaryResponse;
import utils.TercerosUtils;
import utils.WSUtils;
import utils.XMLGregorianCalendarConverter;

/**
 * TercerosServiceImpl
 */
@InjectSupport
public class FileSystemTercerosServiceImpl implements services.TercerosService {

	private static Logger log = Logger.getLogger(FileSystemTercerosServiceImpl.class);
	
	public boolean isConfigured(){
	    return true;
	}
	
    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Terceros ha sido inyectado con FileSystem y está operativo.");
		else
			play.Logger.info("El servicio de Terceros ha sido inyectado con FileSystem y NO está operativo.");
    }
	
	private TercerosServiceException newTercerosServiceException(String msg, Exception cause){
	    return new TercerosServiceException(msg, cause);
	}
	
	public Solicitante buscarTercerosDetalladosByNumeroIdentificacion(String numeroIdentificacion, String tipoIdentificacion) throws TercerosServiceException{
		Solicitante solicitante = new Solicitante();
		if ((numeroIdentificacion==null) || (tipoIdentificacion==null))
			throw newTercerosServiceException("Parametros incorrectos, alguno de los dos ha sido null", null);
		if (tipoIdentificacion.equals("cif")){
			if (!numeroIdentificacion.toUpperCase().equals("A99999997"))
				return null;
			solicitante.tipo="juridica";
			solicitante.juridica.cif=numeroIdentificacion;
			solicitante.juridica.entidad="FAP Company S.A.";
		} else {
			if (!numeroIdentificacion.toUpperCase().equals("11111111H"))
				return null;
			solicitante.tipo="fisica";
			solicitante.fisica.nip.tipo=tipoIdentificacion;
			solicitante.fisica.nip.valor=numeroIdentificacion;
			solicitante.fisica.nombre="Luke";
			solicitante.fisica.primerApellido="Sky";
			solicitante.fisica.segundoApellido="Walker";
			solicitante.fisica.sexo=SexoEnum.hombre.name();
			solicitante.fisica.fechaNacimiento=new DateTime();
		}
		solicitante.domicilio.calle="Paseo de la Administración Electrónica";
		solicitante.domicilio.numero="19";
		solicitante.domicilio.codigoPostal="38390";
		solicitante.domicilio.otros="Bajo Derecha";
		solicitante.domicilio.tipo="nacional";
		solicitante.domicilio.municipio="_380393";
		solicitante.domicilio.localidad="Cuesta la Villa";
		solicitante.domicilio.isla="_384";
		solicitante.domicilio.comunidad="_05";
		solicitante.domicilio.provincia="_38";
		solicitante.domicilio.pais="_724";
		solicitante.email="miCorreo@miDireccion.com";
		solicitante.telefonoContacto="92230000";
		solicitante.uriTerceros="fs://tercero001/v01";
		return solicitante;
	}
	
	public String crearTerceroMinimal(Solicitante solicitante) throws TercerosServiceException{
		return "fs://tercero001/v01";
	}

	@Override
	public Agente buscarTercerosAgenteByNumeroIdentificacion (String numeroIdentificacion, String tipoIdentificacion)
			throws TercerosServiceException {
		Agente agente = Agente.find("select agente from Agente agente where agente.username = ?", numeroIdentificacion).first();
		return (agente == null) ? null : agente;
	}

}
