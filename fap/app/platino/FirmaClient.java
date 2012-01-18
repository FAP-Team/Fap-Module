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

/**
 * @deprecated Utilizar FirmaService con la nueva forma de inyectar servicios
 */

@Deprecated
public class FirmaClient {
	private static Logger log = Logger.getLogger(FirmaClient.class);
	private static PlatinoSignatureServerBean firmaPlatino;
	
	public static final int CERT_OK = 6;
	public static final int CERT_NO_VALIDO = 2;
	public static final int CERT_NO_CONFIANZA = 3;
	public static final int CERT_REVOCADO = 4;
	public static final int CERT_NO_VERIFICADO = 5;
	public static final int CADENA_CERT_NO_VALIDA = 25;

	static {
		URL wsdlURL = FirmaClient.class.getClassLoader().getResource(
				"wsdl/firma-pre.wsdl");
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
			return extraerInformacionPersonal(extraerCertificado(firma));
		} catch (ParserConfigurationException e) {
			log.error("Error al parsear al extraer el certificado "+e);
		} catch (SAXException e) {
			log.error("Error al parsear al extraer el certificado. REINSTALAR EL APPLET O ACTIVEX "+e);
		} catch (IOException e) {
			log.error("Error en extraerInfoFromFirma "+e);
		}
		return null;
	}
	
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

			String certificado = extraerCertificado(firma);
			ValidateCertResult result = validarCertificadoVR(certificado);
			
			if (result.getCode() == CERT_OK) {
				//Valida la firma
				if (verificarContentSignature(contenidoDoc, firma.getBytes())) {
					
					//Firma válida, extrae la informacion del certificado
					HashMap<String,String> certData = extraerInfoFromFirma(firma);
					Firmante firmante = null;
					
					
					//El certificado es de un NIF o NIE
					if (certData != null && certData.containsKey("NIF")) {
						play.Logger.debug("El certificado es un NIF o un CIE");
						
						firmante = new Firmante();
						firmante.idtipo = "nif";
						firmante.idvalor = certData.get("NIF");
						
						if (certData.containsKey("NOMBRECOMPLETO")){ 
							firmante.nombre = certData.get("NOMBRECOMPLETO");
						}else if (certData.containsKey("APELLIDOS")){ 
							firmante.nombre = certData.get("NOMBRE") + " " + certData.get("APELLIDOS");
						}else if (certData.containsKey("APELLIDO1")) {
							String nombre = certData.get("NOMBRE") + " " + certData.get("APELLIDO1");
							if (certData.containsKey("APELLIDO2"))
								nombre = nombre + " " + certData.get("APELLIDO2"); 
							firmante.nombre = nombre;
						}
					}
					else if (certData != null && certData.containsKey("CIF")) {
						play.Logger.debug("El certificado es un CIF");
						
						firmante = new Firmante();
						firmante.idtipo = "cif";
						firmante.idvalor = certData.get("CIF");
						
						if (certData.containsKey("NOMBRECOMPLETO")){ 
							firmante.nombre = certData.get("NOMBRECOMPLETO");
						}else if (certData.containsKey("APELLIDOS")){ 
							firmante.nombre = certData.get("NOMBRE") + " " + certData.get("APELLIDOS");
						}else if (certData.containsKey("APELLIDO1")) {
							String nombre = certData.get("NOMBRE") + " " + certData.get("APELLIDO1");
							if (certData.containsKey("APELLIDO2"))
								nombre = nombre + " " + certData.get("APELLIDO2"); 
							firmante.nombre = nombre;
						}
					}
					return firmante;
				}
				return null;
			} else {
				switch (result.getCode()) {
					case CERT_NO_VALIDO: log.error("certificadoNoValido"); break;
					case CERT_NO_CONFIANZA: log.error("certificadoNoConfianza"); break;
					case CERT_NO_VERIFICADO: log.error("certificadoNoVerificado"); break;
					case CERT_REVOCADO: log.error("certificadoRevocado"); break;
					case CADENA_CERT_NO_VALIDA: log.error("cadenaNoValida"); break;
				}
			}
		} catch (ParserConfigurationException e) {
			log.error("Error al parsear al extraer el certificado "+e);
			//throw new ValidaFirmaException();
		} catch (SAXException e) {
			log.error("Error al parsear al extraer el certificado. REINSTALAR EL APPLET O ACTIVEX "+e);
			//throw new ValidaFirmaException();
		} catch (IOException e) {
			log.error("Error al parsear al extraer el certificado "+e);
			//throw new ValidaFirmaException();
		}
		catch (Exception e) {
			log.error("Error en validateXMLSignature "+e);
			Messages.error("Error al validar la firma");
//			throw new ValidaFirmaException();
		}
		return null;
	}
	
	public static String extraerCertificado(String firma) throws ParserConfigurationException, SAXException, IOException {
		//"Extrayendo el certificado de la firma 
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db  = dbf.newDocumentBuilder();
        org.w3c.dom.Document doc = db.parse(new InputSource(new StringReader(firma)));
        //Pillamos certificado
        Element x509Certificate = (Element) doc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "X509Certificate").item(0);
        return x509Certificate.getTextContent();
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
