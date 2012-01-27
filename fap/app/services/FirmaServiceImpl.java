package services;

import java.io.IOException;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import messages.Messages;
import models.Documento;
import models.Firmante;
import models.RepresentantePersonaFisica;
import models.RepresentantePersonaJuridica;
import models.Solicitante;
import net.java.dev.jaxb.array.StringArray;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import platino.Firma;
import platino.FirmanteCertificado;
import platino.InfoCert;
import platino.KeystoreCallbackHandler;
import platino.PlatinoCXFSecurityHeaders;
import platino.PlatinoProxy;
import play.libs.Codec;
import properties.PropertyPlaceholder;
import sun.security.pkcs.PKCS7;
import utils.WSUtils;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.platino.servicios.sfst.FirmaService;
import es.gobcan.platino.servicios.sfst.PlatinoSignatureServerBean;
import es.gobcan.platino.servicios.sfst.SignatureServiceException_Exception;
import es.gobcan.platino.servicios.sfst.ValidateCertResult;

/**
 * FirmaServiceImpl
 * 
 * El servicio esta preparado para inicializarse de forma lazy.
 * Por lo tanto siempre que se vaya a consumir el servicio web
 * se deberia acceder a "getFirmaPort" en lugar de acceder directamente
 * a la property
 * 
 */
public class FirmaServiceImpl implements services.FirmaService {

	private static Logger log = Logger.getLogger(FirmaServiceImpl.class);
	
	private PlatinoSignatureServerBean firmaPort;
	private final PropertyPlaceholder propertyPlaceholder;
	private final AedService aedService;
	
	private static final int CERT_OK = 6;
	private static final int CERT_NO_VALIDO = 2;
	private static final int CERT_NO_CONFIANZA = 3;
	private static final int CERT_REVOCADO = 4;
	private static final int CERT_NO_VERIFICADO = 5;
	private static final int CADENA_CERT_NO_VALIDA = 25;
	
	public FirmaServiceImpl(PropertyPlaceholder propertyPlaceholder, AedService aedService){
		this.propertyPlaceholder = propertyPlaceholder;
		this.aedService = aedService;
	}

	public FirmaServiceImpl(PropertyPlaceholder propertyPlaceholder, AedService aedService, boolean eagerInitialization){
		this.propertyPlaceholder = propertyPlaceholder;
		this.aedService = aedService;
		if(eagerInitialization)
			getFirmaPort();
	}
	
	private PlatinoSignatureServerBean getFirmaPort(){
		if(firmaPort == null){
			URL wsdlURL = FirmaServiceImpl.class.getClassLoader().getResource("wsdl/firma-pre.wsdl");
			firmaPort = new FirmaService(wsdlURL).getFirmaService();
			
			WSUtils.configureEndPoint(firmaPort, getEndPoint());
			WSUtils.configureSecurityHeaders(firmaPort, propertyPlaceholder);

			PlatinoProxy.setProxy(firmaPort, propertyPlaceholder);			
		}
		return firmaPort;
	}

	@Override
	public boolean hasConnection() {
		boolean hasConnection = false;
		try {
			hasConnection = getVersion() != null;
			log.info("El servicio tiene conexion con " + getEndPoint() + "? :"+hasConnection);
		}catch(Exception e){
			log.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}


	@Override
	public String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.firma.url");
	}


	@Override
	public String getVersion() {
		try {
			return getFirmaPort().getVersion();
		} catch (Exception e) {
			play.Logger.error("firmaPlatino not version: "+e.getMessage());
		}
		return null;
	}
	

	@Override
	public boolean verificarPKCS7(String texto, String firma){
		boolean result = false;
		try {
			String invokingApp = propertyPlaceholder.get("fap.platino.firma.invokingApp");
			result = getFirmaPort().verifyPKCS7Signature(texto.getBytes(), firma.getBytes(), invokingApp);
		} catch (Exception e) {
			log.error("Error verificando la firma", e);
		}
		return result;
	}
	

	@Override
	public boolean verificarContentSignature(byte[] content, byte[] signature) {
		boolean result = false;
		try {
			String invokingApp = propertyPlaceholder.get("fap.platino.firma.invokingApp");
			result = getFirmaPort().verifyContentSignature(content, signature, invokingApp);
		} catch (SignatureServiceException_Exception e) {
			log.error("Error verificando el contenido de la firma", e);
		}
		return result;
	}
	

	@Override
	public String firmarPKCS7(String texto){
		return firmarPKCS7(texto.getBytes());
	}
	

	@Override
	public String firmarPKCS7(byte[] bytes){
		String firma = null;
		try {
			String invokingApp = propertyPlaceholder.get("fap.platino.firma.invokingApp");
			String alias = propertyPlaceholder.get("fap.platino.firma.alias");
			firma = getFirmaPort().signPKCS7(bytes, invokingApp, alias);
		} catch (SignatureServiceException_Exception e) {
			log.error("Error al hacer la firma pkcs7", e);
		} 
		return firma;		
	}
	
	
	public String firmarContentSignature(byte[] content) {
		String firma = null;
		try {
			String invokingApp = propertyPlaceholder.get("fap.platino.firma.invokingApp");
			String alias = propertyPlaceholder.get("fap.platino.firma.alias");
			firma = getFirmaPort().signContent(content, invokingApp, alias);
		} catch (SignatureServiceException_Exception e) {
			log.error("Error al firmar contenido", e);
		}
		return firma;
	}

	@Override
	public String extraerCertificadoDeFirma(String firma){
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
	

	@Override
	public boolean validarCertificado(String certificado){
		String invokingApp = propertyPlaceholder.get("fap.platino.firma.invokingApp");
		try {
			ValidateCertResult result = getFirmaPort().validateCert(certificado, invokingApp);
			return result.getCode() == 6; //Codigo 6 Certificado OK
		} catch (SignatureServiceException_Exception e) {
			log.error("Error validando certificado", e);
		}
		return false;
	}
	
	private ValidateCertResult validarCertificadoVR(String certificado) {
		String invokingApp = propertyPlaceholder.get("fap.platino.firma.invokingApp");
		ValidateCertResult result = null;
		try {
			result = getFirmaPort().validateCert(certificado, invokingApp);
		} catch (SignatureServiceException_Exception e) {
			log.error("Error al recuperar la información del certificado", e);
		}
		return result;
	}
	
	@Override
	public InfoCert extraerInformacion(String certificado){
		try {
			String invokingApp = propertyPlaceholder.get("fap.platino.firma.invokingApp");
			List<StringArray> certInfo = getFirmaPort().getCertInfo(certificado, invokingApp);
			InfoCert infoCert = new InfoCert(certInfo);
			return infoCert;
		} catch (Exception e) {
			log.error("Error al recuperar la información del certificado", e);
		}
		return null;
	}
	

	@Override
	public HashMap<String,String> extraerInfoFromFirma(String firma) {
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
	
	private HashMap<String,String> extraerInformacionPersonal(String certificado) {
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
	

	@Override
	public List<StringArray> getCertInfo(String certificado) throws Exception {
		try {
			String invokingApp = propertyPlaceholder.get("fap.platino.firma.invokingApp");
			return getFirmaPort().getCertInfo(certificado, invokingApp);
		} catch (Exception e) {
			log.error("Error al recuperar la información del certificado"+e);
		}
		return null;
	}
	

	@Override
	public Firmante validateXMLSignature(byte[] contenidoDoc, String firma) {
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
		} catch (SAXException e) {
			log.error("Error al parsear al extraer el certificado. REINSTALAR EL APPLET O ACTIVEX "+e);
		} catch (IOException e) {
			log.error("Error al parsear al extraer el certificado "+e);
		}catch (Exception e) {
			log.error("Error en validateXMLSignature "+e);
			Messages.error("Error al validar la firma");
		}
		return null;
	}
	
	private String extraerCertificado(String firma) throws ParserConfigurationException, SAXException, IOException {
		//Extrayendo el certificado de la firma 
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db  = dbf.newDocumentBuilder();
        org.w3c.dom.Document doc = db.parse(new InputSource(new StringReader(firma)));
        //Pillamos certificado
        Element x509Certificate = (Element) doc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "X509Certificate").item(0);
        return x509Certificate.getTextContent();
	}
	
	private boolean isFirmanteCertificate(String tipoDoc, String docId, FirmanteCertificado firmante) {
		//Por alguna razon los certificados no distingeun entre NIE Y NIF y se ponen lso dos en el mismo campo como NIF
		String newTipoDoc = tipoDoc;
		if (tipoDoc.equalsIgnoreCase("NIE"))
			newTipoDoc = "NIF";
		//Si son el mismo tipo de documento el del ususario y el del certificado
		if (firmante != null && firmante.getTipoDocumento().equalsIgnoreCase(newTipoDoc)) 
			return docId.equals(firmante.getDocumento());
		return false;
	}
	
	@Override
	public void firmar(Documento documento, List<Firmante> firmantes, Firma firma){	
		firmar(documento, firmantes, firma, null);
	}
	
	private Firmante getFirmante(String firma, Documento documento){
		if(firma == null || firma.isEmpty()){
			Messages.error("La firma llegó vacía");
			return null;
		}	
		Firmante firmante = null;
		try {
			byte[] contenido = aedService.obtenerDocBytes(documento.uri);
			firmante = validateXMLSignature(contenido, firma);
			if(firmante == null){
				Messages.error("Error validando la firma");
			}
		} catch (Exception e) {
			play.Logger.error("Error obteniendo el documento del AED para verificar la firma. Uri = " + documento.uri);
			Messages.error("Error validando la firma");
		}
		return firmante;
	}
	

	@Override
	public void firmar(Documento documento, List<Firmante> firmantes, Firma firma, String valorDocumentofirmanteSolicitado){		
		Firmante firmanteCertificado = getFirmante(firma.firma, documento);
		
		if(firmanteCertificado != null){
			log.info("Firmante validado");
			
			int index = firmantes.indexOf(firmanteCertificado);
			Firmante firmante = null;
			if(index == -1){
				Messages.error("El certificado no se corresponde con uno que debe firmar la solicitud.");
			}else{
				firmante = firmantes.get(index);
				if(firmante.fechaFirma != null){
					Messages.error("Ya ha firmado la solicitud");
				}
				
				log.info("Firmante encontrado " + firmante.idvalor );
				log.info("Esperado " + valorDocumentofirmanteSolicitado);
				if(valorDocumentofirmanteSolicitado != null && !firmante.idvalor.equalsIgnoreCase(valorDocumentofirmanteSolicitado)){
					Messages.error("Se esperaba la firma de " + valorDocumentofirmanteSolicitado);
				}
			}
			
			if(!Messages.hasErrors()){
				// Guarda la firma en el AED
				try {
					log.info("Guardando firma en el aed");
					firmante.fechaFirma = new DateTime();
					aedService.agregarFirma(documento.uri, firmante, firma.firma);
					firmante.save();
					
					log.info("Firma del documento " + documento.uri + " guardada en el AED");
				}catch(AedExcepcion e){
					log.error("Error guardando la firma en el aed");
					Messages.error("Error al guardar la firma");
				}				
			}
		}else{
			log.error("firmanteCertificado == null????");
		}
	}

	@Override
	public void firmarFH(Documento documento, Firma firma){		
		Firmante firmante = getFirmante(firma.firma, documento);
		
		if((firmante != null)&&(firmante.esFuncionarioHabilitado())){
			log.info("Funcionario habilitado validado");
			log.info("Firmante encontrado " + firmante.idvalor );
			
			if(!Messages.hasErrors()){
				// Guarda la firma en el AED
				try {
					log.info("Guardando firma en el aed");
					firmante.fechaFirma = new DateTime();
					aedService.agregarFirma(documento.uri, firmante, firma.firma);
					firmante.save();
					
					log.info("Firma del documento " + documento.uri + " guardada en el AED");
				}catch(AedExcepcion e){
					log.error("Error guardando la firma en el aed");
					Messages.error("Error al guardar la firma");
				}				
			}
		}else{
			Messages.error("El certificado no se corresponde con uno que debe firmar la solicitud.");
		}
	}

	public boolean hanFirmadoTodos(List<Firmante> firmantes){
		boolean multiple = true;
		for(Firmante f : firmantes){
			//Firmante único que ya ha firmado
			if(f.cardinalidad.equals("unico") && f.fechaFirma != null)
				return true;
			
			//Uno de los firmantes multiples no ha firmado
			if(f.cardinalidad.equals("multiple") && f.fechaFirma == null)
				multiple = false;
		}
		
		//En el caso de que no haya firmado ningún único
		//Se devuelve true si todos los múltiples han firmado
		return multiple;
	}
	
	/**
	 * Borra una lista de firmantes, borrando cada uno de los firmantes y vaciando la lista
	 * @param firmantes
	 */
	public void borrarFirmantes(List<Firmante> firmantes){
		List<Firmante> firmantesBack = new ArrayList<Firmante>(firmantes);
		firmantes.clear();
		
		for(Firmante f : firmantesBack)
			f.delete();
	}
	
	public void calcularFirmantes(Solicitante solicitante, List<Firmante> firmantes){
		if(solicitante == null) throw new NullPointerException();
		if(firmantes == null) throw new NullPointerException();
		
		//Solicitante de la solicitud
		Firmante firmanteSolicitante = new Firmante(solicitante, "unico");
		firmantes.add(firmanteSolicitante);
		
		//Comprueba los representantes
		if(solicitante.isPersonaFisica() && solicitante.representado){
			// Representante de persona física
			Firmante representante = new Firmante(solicitante.representante, "representante", "unico");
			firmantes.add(representante);
		}else if(solicitante.isPersonaJuridica()){
			//Representantes de la persona jurídica
			for(RepresentantePersonaJuridica r : solicitante.representantes){
				String cardinalidad = null;
				if(r.tipoRepresentacion.equals("mancomunado")){
					cardinalidad = "multiple";
				}else if((r.tipoRepresentacion.equals("solidario")) || (r.tipoRepresentacion.equals("administradorUnico"))){
					cardinalidad = "unico";
				}
				Firmante firmante = new Firmante(r, "representante", cardinalidad);
				firmantes.add(firmante);
			}
		}
	}	
	
}
