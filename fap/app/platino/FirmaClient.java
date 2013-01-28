package platino;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;

import messages.Messages;
import models.Firmante;
import net.java.dev.jaxb.array.StringArray;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import play.libs.Codec;
import properties.FapProperties;

import sun.security.pkcs.PKCS7;
import sun.security.pkcs.ParsingException;

import es.gobcan.eadmon.aed.ws.Aed;
import es.gobcan.eadmon.aed.ws.AedPortType;
import es.gobcan.platino.servicios.sfst.FirmaService;
import es.gobcan.platino.servicios.sfst.PlatinoSignatureServerBean;
import es.gobcan.platino.servicios.sfst.SignatureServiceException_Exception;
import es.gobcan.platino.servicios.sfst.ValidateCertResult;

public class FirmaClient {
	private static Logger log = Logger.getLogger(FirmaClient.class);
	private static PlatinoSignatureServerBean firmaPlatino;	
	
	public static final int CERT_OK = 6;
	public static final int CERT_NO_VALIDO = 2;
	public static final int CERT_NO_CONFIANZA = 3;
	public static final int CERT_REVOCADO = 4;
	public static final int CERT_NO_VERIFICADO = 5;
	public static final int CADENA_CERT_NO_VALIDA = 25;
	private final static String INVOKING_APP;
	
	static {
		URL wsdlURL = FirmaClient.class.getClassLoader().getResource(
				"wsdl/firma.wsdl");
		firmaPlatino = new FirmaService(wsdlURL).getFirmaService();

		BindingProvider bp = (BindingProvider) firmaPlatino;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				FapProperties.get("fap.platino.firma.url"));

		PlatinoCXFSecurityHeaders.addSoapWSSHeader(
				firmaPlatino,
				PlatinoCXFSecurityHeaders.SOAP_11,
				FapProperties.get(
						"fap.platino.security.backoffice.uri"),
						FapProperties.get(
						"fap.platino.security.certificado.alias"),
				KeystoreCallbackHandler.class.getName(), null);

		
		PlatinoProxy.setProxy(firmaPlatino);
		
		Client client = ClientProxy.getClient(firmaPlatino);
		HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpClientPolicy.setReceiveTimeout(FapProperties.getLong("fap.servicios.httpTimeout"));
		httpConduit.setClient(httpClientPolicy);
		
		INVOKING_APP = FapProperties.get("fap.platino.firma.invokingApp");
	}

	public static String getVersion() {
		return firmaPlatino.getVersion();
	}

	public static Boolean verificarPKCS7(String texto, String firma){
		try {
			String invokingApp = FapProperties.get("fap.platino.firma.invokingApp");
			play.Logger.info("invokingApp: "+invokingApp);
			play.Logger.info("texto: "+texto);
			play.Logger.info("firma: "+firma);
			return firmaPlatino.verifyPKCS7Signature(texto.getBytes(), firma.getBytes(), invokingApp);
		} catch (Exception e) {
			log.error("Error verificando la firma", e);
			return false;
		}
	}
	
	public static Boolean verificarContentSignature(byte[] content, byte[] signature) {
		try {
			String invokingApp = FapProperties.get("fap.platino.firma.invokingApp");
			return firmaPlatino.verifyContentSignature(content, signature, invokingApp);
		} catch (SignatureServiceException_Exception e) {
			log.error("Error verificando el contenido de la firma", e);
			return false;
		}
	}
	
	public static String firmarPKCS7(String texto){
		return firmarPKCS7(texto.getBytes());
	}
	
	public static String firmarPKCS7(byte[] bytes){
		String firma = null;
		try {
			String invokingApp = FapProperties.get("fap.platino.firma.invokingApp");
			String alias = FapProperties.get("fap.platino.firma.alias");
			firma = firmaPlatino.signPKCS7(bytes, invokingApp, alias);
		} catch (SignatureServiceException_Exception e) {
			log.error("Error al hacer la firma pkcs7", e);
		} 
		return firma;		
	}
	
	public static String extraerCertificadoDeFirma(String firma){
		String certificado = null;
		try {
			PKCS7 pkcs7 = new PKCS7(Codec.decodeBASE64(firma));
			X509Certificate certificate = pkcs7.getCertificates()[0];
			byte[] certificadoEncoded = certificate.getEncoded();
			certificado = Codec.encodeBASE64(certificadoEncoded);
		} catch (Exception e) {
			log.error("Error al extraer la información del certificado");
		}
		return certificado;
	}
	
	public static Boolean validarCertificado(String certificado){
		String invokingApp = FapProperties.get("fap.platino.firma.invokingApp");
		try {
			ValidateCertResult result = firmaPlatino.validateCert(certificado, invokingApp);
			return result.getCode() == 6; //Codigo 6 Certificado OK
		} catch (SignatureServiceException_Exception e) {
			log.error("Error validando certificado", e);
		}
		return false;
	}
	
	public static ValidateCertResult validarCertificadoVR(String certificado) {
		String invokingApp = FapProperties.get("fap.platino.firma.invokingApp");
		ValidateCertResult result = null;
		try {
			result = firmaPlatino.validateCert(certificado, invokingApp);
		} catch (SignatureServiceException_Exception e) {
			log.error("Error al recuperar la información del certificado", e);
			e.printStackTrace();
		}
		return result;
	}
	
	public static InfoCert extraerInformacion(String certificado){
		try {
			String invokingApp = FapProperties.get("fap.platino.firma.invokingApp");
			List<StringArray> certInfo = firmaPlatino.getCertInfo(certificado, invokingApp);
			InfoCert infoCert = new InfoCert(certInfo);
			return infoCert;
		} catch (Exception e) {
			log.error("Error al recuperar la información del certificado", e);
		}
		return null;
	}
	
	public static HashMap<String,String> extraerInfoFromFirma(String firma) {
		try {
			return Info2HashMap(extraerCertificado(firma));
		} catch (ParserConfigurationException e) {
			log.error("Error al parsear al extraer el certificado "+e);
		} catch (SAXException e) {
			log.error("Error al parsear al extraer el certificado. REINSTALAR EL APPLET O ACTIVEX "+e);
		} catch (IOException e) {
			log.error("Error en extraerInfoFromFirma "+e);
		}
		return null;
	}
	
	private static HashMap<String, String> Info2HashMap(InfoCert infocert) {
		HashMap<String, String> result = new HashMap<String, String>();
		if (!infocert.nombrecompleto.isEmpty())
			result.put("nombrecompleto", infocert.nombrecompleto);
		if (!infocert.nombre.isEmpty())
			result.put("nombre", infocert.nombre);
		if (!infocert.fullname.isEmpty())
			result.put("fullname", infocert.fullname);
		if (!infocert.entidad.isEmpty())
			result.put("entidad", infocert.entidad);
		if (!infocert.apellido1.isEmpty())
			result.put("apellido1", infocert.apellido1);
		if (!infocert.apellido2.isEmpty())
			result.put("apellido2", infocert.apellido2);
		if (!infocert.apellidos.isEmpty())
			result.put("apellidos", infocert.apellidos);
		if (!infocert.nif.isEmpty())
			result.put("nif", infocert.nif);
		if (!infocert.cif.isEmpty())
			result.put("cif", infocert.cif);
		if (!infocert.tipo.isEmpty())
			result.put("tipo", infocert.tipo);
		if (!infocert.email.isEmpty())
			result.put("email", infocert.email);
		if (!infocert.cargo.isEmpty())
			result.put("cargo", infocert.cargo);
		if (!infocert.departamento.isEmpty())
			result.put("departamento", infocert.departamento);
		if (!infocert.finalidad.isEmpty())
			result.put("finalidad", infocert.finalidad);
		if (!infocert.organizacion.isEmpty())
			result.put("organizacion", infocert.organizacion);
		if (!infocert.serialNumber.isEmpty())
			result.put("serialnumber", infocert.serialNumber);
		if (!infocert.issuer.isEmpty())
			result.put("issuer", infocert.issuer);
		if (!infocert.subject.isEmpty())
			result.put("subject", infocert.subject);
		if (!infocert.notBefore.isEmpty())
			result.put("notBefore", infocert.notBefore);
		if (!infocert.notAfter.isEmpty())
			result.put("notAfter", infocert.notAfter);

		return result;
	}

	@Deprecated
	private static HashMap<String,String> extraerInformacionPersonal(String certificado) {
		try {
			List<StringArray> certificadoInfo = getCertInfo(certificado);
			HashMap<String,String>values = new HashMap<String, String>();
			if (certificadoInfo != null) {
				for (StringArray array : certificadoInfo) {
					String key = array.getItem().get(0);
					String value = array.getItem().get(1);
					if (key != null) {
						values.put(key.toUpperCase(), value);
					}
				}
			}
			return values;

		} catch (Exception e) {
			return null;
		}
	}
	
	public static List<StringArray> getCertInfo(String certificado) throws Exception {
		try {
			String invokingApp = FapProperties.get("fap.platino.firma.invokingApp");
			return firmaPlatino.getCertInfo(certificado, invokingApp);
		} catch (Exception e) {
			log.error("Error al recuperar la información del certificado"+e);
		}
		return null;
	}
	
	/**
	 * Valida la firma y extrae la informacion del firmante
	 * @param contenidoDoc Contenido del documento firmado
	 * @param firma Firma
	 * @return Informacion del firmante
	 */
	public static Firmante validateXMLSignature(byte[] contenidoDoc, String firma) {// throws ValidaFirmaException {
		try {
			//Valida la firma
			if (verificarContentSignature(contenidoDoc, firma.getBytes())) {

				//Firma válida, extrae la informacion del certificado
				InfoCert certData = extraerCertificado(firma);
				Firmante firmante = new Firmante();
				firmante.idtipo = certData.getIdTipo();
				firmante.idvalor = certData.getId();
				firmante.nombre = certData.getNombreCompleto();

				return firmante;
			}
			return null;
		}catch (Exception e) {
			play.Logger.error("Error en validateXMLSignature "+e);
			Messages.error("Error al validar la firma");
		}
		play.Logger.error("Error en validateXMLSignature, la firma no es válida.");
		Messages.error("La firma no es válida.");
		
		return null;
	}

	
	public static InfoCert extraerCertificado(String firma) throws ParserConfigurationException, SAXException, IOException {
		String certificado;
		try {
			 certificado = extraerCertificadoDeFirma(firma);
			 boolean certificadoValido = isValidCertificado(certificado);
		     if(!certificadoValido)
		         throw new Exception("El certificado no es válido");
		     return extraerInformacion(certificado);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
       
	}
	
	private static boolean isValidCertificado(String certificado) throws Exception{
		try {
			ValidateCertResult result = firmaPlatino.validateCert(certificado, INVOKING_APP);
			return result.getCode() == 6; //Codigo 6 Certificado OK
		} catch (Exception e) {
		    throw new Exception("Error validando el certificado", e);
		}
	}
	
	public static boolean isFirmanteCertificate(String tipoDoc, String docId, FirmanteCertificado firmante) {
		//Por alguna razon los certificados no distingeun entre NIE Y NIF y se ponen lso dos en el mismo campo como NIF
		String newTipoDoc = tipoDoc;
		if (tipoDoc.equalsIgnoreCase("NIE"))
			newTipoDoc = "NIF";
		//Si son el mismo tipo de documento el del ususario y el del certificado
		if (firmante != null && firmante.getTipoDocumento().equalsIgnoreCase(newTipoDoc)) 
			return docId.equals(firmante.getDocumento());
		return false;
	}
	
}
