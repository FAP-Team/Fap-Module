package services.platino;

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
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import controllers.fap.FirmaController;


import platino.Firma;
import platino.FirmanteCertificado;
import platino.InfoCert;
import platino.KeystoreCallbackHandler;
import platino.PlatinoCXFSecurityHeaders;
import platino.PlatinoProxy;
import play.libs.Codec;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import services.FirmaServiceException;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import sun.security.pkcs.PKCS7;
import utils.BinaryResponse;
import utils.WSUtils;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.AedPortType;
import es.gobcan.platino.servicios.sfst.*;

/**
 * FirmaServiceImpl
 */
@InjectSupport
public class PlatinoFirmaServiceImpl implements services.FirmaService {

    public static final String NOMBRE_IMPLEMENTACION = "PlatinoFirmaServiceImpl";
    public static final String APPLET_FIRMA = "cliente_afirma/mini/js/";
	private static Logger log = Logger.getLogger(PlatinoFirmaServiceImpl.class);
	
	@Inject
    protected static GestorDocumentalService gestorDocumentalService;
	
	private PlatinoSignatureServerBean firmaPort;
	private PropertyPlaceholder propertyPlaceholder;
	
	private final String INVOKING_APP;
	private final String ALIAS;
	private final String JS_ENTORNO;
	
	@Inject
	public PlatinoFirmaServiceImpl(PropertyPlaceholder propertyPlaceholder){
        this(propertyPlaceholder, null);

    }

	public PlatinoFirmaServiceImpl(PropertyPlaceholder propertyPlaceholder, PlatinoSignatureServerBean signPort){
		this.propertyPlaceholder = propertyPlaceholder;

        if (signPort == null) {
            URL wsdlURL = PlatinoFirmaServiceImpl.class.getClassLoader().getResource("wsdl/firma.wsdl");
            firmaPort = new FirmaService(wsdlURL).getFirmaService();
            WSUtils.configureEndPoint(firmaPort, getEndPoint());
            WSUtils.configureSecurityHeaders(firmaPort, propertyPlaceholder);
            PlatinoProxy.setProxy(firmaPort, propertyPlaceholder);
        } else {
            firmaPort = signPort;
        }



        //Properties
        INVOKING_APP = propertyPlaceholder.get("fap.platino.firma.invokingApp");
        ALIAS = propertyPlaceholder.get("fap.platino.firma.alias");
        JS_ENTORNO = properties.FapProperties.get("fap.platino.firma.js");
	}
	
	public boolean isConfigured(){
	    return hasConnection();
	}
	
	@Override
    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Firma ha sido inyectado con Platino y está operativo.");
		else
			play.Logger.info("El servicio de Firma ha sido inyectado con Platino y NO está operativo.");
    }

    @Override
    public String getInfoInyeccion() {
        return NOMBRE_IMPLEMENTACION;
    }
	
	private boolean hasConnection() {
		boolean hasConnection = false;
		try {
			hasConnection = getVersion() != null;
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "? :"+hasConnection);
		}catch(Exception e){
			play.Logger.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}

	private String getVersion() throws FirmaServiceException {
	    try {
	        String version = firmaPort.getVersion();
	        return version;
	    }catch(Exception e){
	        throw newFirmaServiceException("Error al hacer getVersion", e);
	    }
	}
	
	protected PlatinoSignatureServerBean getFirmaPort(){
    	return this.firmaPort;
    }
	
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.firma.url");
	}

	private FirmaServiceException newFirmaServiceException(String msg, Exception cause){
	    if(cause instanceof SignatureServiceException_Exception){
	        SignatureServiceException_Exception signatureException = (SignatureServiceException_Exception) cause;
	        return new FirmaServiceException(msg + " - " + signatureException.getFaultInfo().getMessage(), cause);
	    }else{
	        return new FirmaServiceException("Error al realizar firma pkcs7", cause);
	    }
	}
	
	@Override
    public List<String> getFirmaEnClienteJS() {
        List<String> jsclient = new ArrayList<String>();
        String host;
        if("pre".equals(JS_ENTORNO.toLowerCase())){
        	host = "https://www-pre.gobiernodecanarias.org/platino/" + APPLET_FIRMA;
        }else{
            String baseUrl = FapProperties.get("application.baseUrl");
            if (baseUrl.contains("gobiernodecanarias.org")) {
                host = "https://www.gobiernodecanarias.org/platino/" + APPLET_FIRMA;
            } else if (baseUrl.contains("sede.gobcan.es")) {
                host = "https://sede.gobcan.es/platino/" + APPLET_FIRMA;
            } else {
                host = "https://www.gobiernodecanarias.org/platino/" + APPLET_FIRMA;
            }
        }

        jsclient.add(host + "common-js/deployJava.js");
        jsclient.add(host + "miniapplet.js");
        jsclient.add(host + "sfest.base.js");

        jsclient.add("/public/javascripts/firma/firma.js");
        jsclient.add("/public/javascripts/firma/firma-platino.js");
        jsclient.add("/public/javascripts/msg.js");
        return jsclient;
    }
	
    public String firmarTexto(byte[] texto) throws FirmaServiceException {
    	log.info("[firmarTexto] Iniciando firmarTexto -> Datos de Registro"); 
        String firma = null;
        try {
            firma = firmaPort.signPKCS7(texto, INVOKING_APP, ALIAS);
            log.info("[firmarTexto] Texto firmado correctamente"); 
        } catch (NullPointerException e) {
            log.error("NullPointerException. Posiblemente recibido null como texto a firmar");
            throw e;
        }catch (Exception e) {
        	log.error("[firmarDatosRegistro] Error en firmaPort.signPKCS7()");
            throw newFirmaServiceException("Error al realizar firma pkcs7", e);
        }
        return firma;
    }
	
    public boolean validarFirmaTexto(byte[] texto, String firma) throws FirmaServiceException{
        boolean result = false;
        try {
            result = firmaPort.verifyContentSignature(texto, firma.getBytes(), INVOKING_APP);
        } catch (NullPointerException e) {
            throw e;
        } catch (Exception e) {
            throw newFirmaServiceException("Error al validar la firma en el texto", e);
        }
        return result;  
    }

    public String firmarDocumento(byte[] contenidoDocumento) throws FirmaServiceException {
        String firma = null;
        try {
            firma = firmaPort.signContent(contenidoDocumento, INVOKING_APP, ALIAS);
        } catch (Exception e) {
            throw newFirmaServiceException("Error al firmar XMLSignature ", e);
        }
        return firma;
    }
    
    public boolean validarFirmaDocumento(byte[] contenidoDocumento, String firma) throws FirmaServiceException {
        boolean result = false;
        try {
            result = firmaPort.verifyContentSignature(contenidoDocumento, firma.getBytes(), INVOKING_APP);
        } catch (SignatureServiceException_Exception e) {
            play.Logger.error("Error verificando el contenido de la firma", e);
        }
        return result;        
    }

    public InfoCert extraerCertificado(String firma) throws FirmaServiceException {
        String certificado = extraerCertificadoDeFirma(firma);
        boolean certificadoValido = isValidCertificado(certificado);
        if(!certificadoValido)
            throw new FirmaServiceException("El certificado no es válido");
        return getInformacion(certificado);
    }

    public InfoCert extraerCertificadoLogin(String firma) throws FirmaServiceException{
		String certificado = null;
		try {
			PKCS7 pkcs7 = new PKCS7(Codec.decodeBASE64(firma));
			X509Certificate certificate = pkcs7.getCertificates()[1];
			byte[] certificadoEncoded = certificate.getEncoded();
			certificado = Codec.encodeBASE64(certificadoEncoded);
		} catch (Exception e) {
			play.Logger.error("Error al extraer la información del certificado", e);
		}
		boolean certificadoValido = isValidCertificado(certificado);
        if(!certificadoValido)
            throw new FirmaServiceException("El certificado no es válido");
		return getInformacion(certificado);
	}
    
	protected String extraerCertificadoDeFirma(String firma) throws FirmaServiceException {
		try {
			//"Extrayendo el certificado de la firma 
            FirmaInfoResult firmaInfoResult = firmaPort.getSignInfo(firma.getBytes());
            List<NodoInfoResult> nodosFirma = firmaInfoResult.getNodosFirma().getNodoFirma();
            return nodosFirma.get(nodosFirma.size()-1).getCertificado().get(0);
		} catch (Exception e) {
			System.out.println("Exception extraer: "+e.getMessage());
			throw new FirmaServiceException("Error al extraer el certificado de la firma", e);
		}
	}
	
	private boolean isValidCertificado(String certificado) throws FirmaServiceException{
		try {
			ValidateCertResult result = firmaPort.validateCert(certificado, INVOKING_APP);
			return result.getCode() == 6; //Codigo 6 Certificado OK
		} catch (Exception e) {
		    throw new FirmaServiceException("Error validando el certificado", e);
		}
	}
		
	protected InfoCert getInformacion(String certificado) throws FirmaServiceException{
		try {
			List<StringArray> certInfo = firmaPort.getCertInfo(certificado, INVOKING_APP);
			InfoCert infoCert = new InfoCert(certInfo);
			return infoCert;
		} catch (Exception e) {
			throw newFirmaServiceException("Error al extraer la información del certificado", e);
		}
	}
	
	@Override
	public Firmante getFirmante(String firma, Documento documento){
		if(firma == null || firma.isEmpty()){
			Messages.error("La firma llegó vacía");
			return null;
		}	
		Firmante firmante = null;
		try {
		    BinaryResponse response = gestorDocumentalService.getDocumento(documento);
			byte[] contenido = response.getBytes();
			firmante = validateXMLSignature(contenido, firma);
			if(firmante == null){
				Messages.error("Error validando la firma");
			}
		} catch (Exception e) {
			play.Logger.error("Error obteniendo el documento del AED para verificar la firma. Uri = " + documento.uri, e);
			Messages.error("Error validando la firma");
		}
		return firmante;
	}

    @Override
    public List<Firmante> getFirmantes(String firma) {
        List<Firmante> firmantes = new ArrayList<Firmante>();
        long inicio = System.nanoTime();
        try {
            FirmaInfoResult signInfo = firmaPort.getSignInfo(firma.getBytes());
            List<NodoInfoResult> nodosFirma = signInfo.getNodosFirma().getNodoFirma();

            for (NodoInfoResult nodoInfoResult : nodosFirma) {
                try {
                    InfoCert certData = getInformacion(nodoInfoResult.getCertificado().get(0));
                    firmantes.add(firmanteFromCertInfo(certData));
                } catch (FirmaServiceException e) {
                    e.printStackTrace();
                }
            }
        } catch (SignatureServiceException_Exception e) {
            e.printStackTrace();
        }
        return firmantes;
    }
	
	@Override
	public Firmante getFirmante(String firma, Documento documento, List<Firmante> todosFirmantes){
		if(firma == null || firma.isEmpty()){
			Messages.error("La firma llegó vacía");
			return null;
		}	
		Firmante firmante = null;
		try {
		    BinaryResponse response = gestorDocumentalService.getDocumento(documento);
			byte[] contenido = response.getBytes();
			firmante = validateXMLSignature(contenido, firma, todosFirmantes);
			if(firmante == null){
				Messages.error("Error validando la firma");
			}
		} catch (Exception e) {
			play.Logger.error("Error obteniendo el documento del AED para verificar la firma. Uri = " + documento.uri, e);
			Messages.error("Error validando la firma");
		}
		return firmante;
	}
	
	private Boolean verificarContentSignature(byte[] content, byte[] signature) {
		try {
			Boolean verifySignatureByFormatResponse = firmaPort.verifySignatureByFormat(content, signature, INVOKING_APP, "CADES");
			play.Logger.info("verificarContentSignature() | verifySignatureByFormatResponse: "+verifySignatureByFormatResponse);
			return verifySignatureByFormatResponse;
		} catch (SignatureServiceException_Exception e) {
			play.Logger.error("Error verificando el contenido de la firma", e);
			return false;
		}
	}
	
	@Override
	public List<StringArray> getCertInfo(String certificado) throws FirmaServiceException{
		try {
			String invokingApp = FapProperties.get("fap.platino.firma.invokingApp");
			return firmaPort.getCertInfo(certificado, invokingApp);
		} catch (Exception e) {
			play.Logger.error("Error al recuperar la información del certificado"+e);
		}
		return null;
	}
	
	@Override
	public Firmante validateXMLSignature(byte[] contenidoDoc, String firma) {
		try {
			//Valida la firma
			if (verificarContentSignature(contenidoDoc, firma.getBytes())) {
				
				//Firma válida, extrae la informacion del certificado
				InfoCert certData = extraerCertificado(firma);
				Firmante firmante = firmanteFromCertInfo(certData);
				return firmante;
			}
			return null;
		} catch (FirmaServiceException e){
			play.Logger.error("El certificado no es válido: " + e.getMessage());
		}catch (Exception e) {
			play.Logger.error("Error en validateXMLSignature "+e);
			Messages.error("Error al validar la firma");
		}
		play.Logger.error("Error en validateXMLSignature, la firma no es válida.");
		Messages.error("La firma no es válida.");
		return null;
	}
	
	@Override
	public Firmante validateXMLSignature(byte[] contenidoDoc, String firma, List<Firmante> todosFirmantes) {
		try {
			
			play.Logger.info("Firmantes Válidos: ");
			for (Firmante firmanteAux: todosFirmantes){
        		play.Logger.info(firmanteAux.toString());
        	}
			play.Logger.info("Firmante: "+FirmaController.getIdentificacionFromFirma(firma));
			
			//Valida la firma
			if (verificarContentSignature(contenidoDoc, firma.getBytes())) {
				
				//Firma válida, extrae la informacion del certificado
				InfoCert certData = extraerCertificado(firma);
				Firmante firmante = null;

				String identificadorFirmante = FirmaController.getIdentificacionFromFirma(firma);
                firmante = buscarFirmanteEnFirmantes(identificadorFirmante, todosFirmantes);
				if (firmante == null){
					play.Logger.error("Error en validateXMLSignature, Firmante "+certData.getIdTipo()+" no encontrado: "+identificadorFirmante+" en la lista de firmantes.");
					Messages.error("Error al recuperar el firmante de tipo "+certData.getIdTipo());
					return null;
				}

                firmante = firmanteFromCertInfo(certData);
				return firmante;
			}
		} catch (FirmaServiceException e){
			play.Logger.error("El certificado no es válido: " +e.getMessage());
		}catch (Exception e) {
			play.Logger.error("Error en validateXMLSignature "+e);
			Messages.error("Error al validar la firma");
		}
		play.Logger.error("Error en validateXMLSignature, la firma no es válida");
		Messages.error("La firma no es válida");
		return null;
	}
	
	@Override
	public void firmar(Documento documento, List<Firmante> firmantes, String firma, String valorDocumentofirmanteSolicitado){		
		Firmante firmanteCertificado = getFirmante(firma, documento);
		if(firmanteCertificado != null){
			play.Logger.info("Firmante validado");
            Firmante firmante = comprobarFirmanteValido(firmantes, valorDocumentofirmanteSolicitado, firmanteCertificado, documento);
			if(!Messages.hasErrors()){
				// Guarda la firma en el AED
                firmante.nombre = firmanteCertificado.nombre;
                agregarFirmaEnGestorDocumental(documento, firma, firmante);
                Messages.ok("Firma de documento " + documento.descripcion + " con éxito");
            }
		}else{
			play.Logger.error("firmanteCertificado == null????");
		}
	}

    private void agregarFirmaEnGestorDocumental(Documento documento, String firma, Firmante firmante) {
        try {
            play.Logger.info("Guardando firma en el aed");
            firmante.fechaFirma = new DateTime();
            gestorDocumentalService.agregarFirma(documento, new models.Firma(firma, firmante));
            firmante.save();
            play.Logger.info("Firma del documento " + documento.uri + ", con fecha " + firmante.fechaFirma + " guardada en el AED");
        }catch(GestorDocumentalServiceException e){
            play.Logger.error("Error guardando la firma en el aed: " +e.getMessage());
            Messages.error("Error al guardar la firma");
        }
    }

    private Firmante comprobarFirmanteValido(List<Firmante> firmantes, String valorDocumentofirmanteSolicitado, Firmante firmanteCertificado, Documento documento) {
        Firmante firmante = buscarFirmanteEnFirmantes(firmanteCertificado.idvalor, firmantes);
        if(firmante == null){
            Messages.error("El certificado no se corresponde con uno que puede firmar la solicitud.");
        }else{
            if(firmante.fechaFirma != null){
            	Messages.error("Este certificado ya ha firmado el documento " + documento.descripcion);
            }

            play.Logger.info("Firmante encontrado " + firmante.idvalor );
            play.Logger.info("Esperado " + valorDocumentofirmanteSolicitado);
            if(valorDocumentofirmanteSolicitado != null && !firmante.idvalor.equalsIgnoreCase(valorDocumentofirmanteSolicitado)){
                Messages.error("Se esperaba la firma de " + valorDocumentofirmanteSolicitado);
            }
        }
        return firmante;
    }

    private Firmante buscarFirmanteEnFirmantes(String idValorFirmante, List<Firmante> firmantes) {
        Firmante firmante = null;
        for (Firmante fB : firmantes) {
            if (fB.idvalor.equals(idValorFirmante)) {
                firmante = fB;
                break;
            }
        }
        return firmante;
    }
	
	/**
	 * Realiza la firma mediante el sello del documento indicado.
	 * 
	 * 	1. Se obtiene la firma
	 *  2. Se firma con el sello
	 *  3. Se agrega la firma
	 * 
	 * @param documento
	 * @return
	 */
	public String firmarEnServidor (Documento documento) throws FirmaServiceException {
		String firma = "";
		try {
			firma = firmarDocumento(gestorDocumentalService.getDocumento(documento).getBytes());
			gestorDocumentalService.agregarFirma(documento, firma);
		} catch (GestorDocumentalServiceException ex) {
			play.Logger.error("Error al agregar la firma al documento "+documento.uri+ ": "+ex.getMessage());
			Messages.error("No se ha podido firmar en el servidor (AED exception)");
		} catch (Exception ex) {
			play.Logger.error("No se ha podido firmar en el servidor: "+ex.getMessage());
			Messages.error("No se ha podido firmar en el servidor.");
			throw new FirmaServiceException("Error al firmar XMLSignature ", ex);
		}
		return firma;
	}

    private Firmante firmanteFromCertInfo(InfoCert infoCert) {
        Firmante firmante = new Firmante();
        firmante.idtipo = infoCert.getIdTipo();
        firmante.idvalor = infoCert.getId();
        firmante.nombre = infoCert.getNombreCompleto();
        return firmante;
    }

}
