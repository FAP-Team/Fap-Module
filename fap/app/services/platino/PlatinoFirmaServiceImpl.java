package services.platino;

import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import messages.Messages;
import models.Documento;
import models.Firmante;
import net.java.dev.jaxb.array.StringArray;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import platino.InfoCert;
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
import controllers.fap.FirmaController;
import es.gobcan.platino.servicios.sfst.FirmaInfoResult;
import es.gobcan.platino.servicios.sfst.NodoInfoResult;
import es.gobcan.platino.servicios.sfst.PlatinoSignatureServerBean;
import es.gobcan.platino.servicios.sfst.SfstService;
import es.gobcan.platino.servicios.sfst.SignatureServiceException_Exception;
import es.gobcan.platino.servicios.sfst.ValidateCertResult;
import es.gobcan.platino.servicios.sfst.VerifySignatureResult;

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
            firmaPort = new SfstService(wsdlURL).getFirmaService();
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

	@Override
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

	@Override
    public List<String> getFirmaEnClienteJS() {
        List<String> jsclient = new ArrayList<String>();
        String host;
		host = getHostJavaScripts();

        jsclient.add(host + "common-js/deployJava.js");
        jsclient.add(host + "miniapplet.js");
        jsclient.add(host + "sfest.base.js");

        jsclient.add("/public/javascripts/firma/firma.js");
        jsclient.add("/public/javascripts/firma/firma-platino.js");
        jsclient.add("/public/javascripts/msg.js");
        return jsclient;
    }

    @Override
	public String firmarContenido(byte[] contenido) throws FirmaServiceException {
    	 String firma = null;
         try {
             firma = firmaPort.signContent(contenido, INVOKING_APP, ALIAS);
         } catch (Exception e) {
             throw newFirmaServiceException("Error al intentar firmar ", e);
         }
         return firma;
	}

	@Override
	public boolean validarFirma(byte[] contenido, String firma)	throws FirmaServiceException {
		boolean result = false;
        try {
            VerifySignatureResult vefifyResult = firmaPort.verifyContentSignature(contenido, firma.getBytes(), INVOKING_APP);
            
            if (vefifyResult != null)
                result = vefifyResult.isValido();
        } catch (Exception e) {
            throw newFirmaServiceException("Error al validar la firma", e);
        }
        return result;
	}
    
    @Override
	public String firmarContenidoEnFormato(byte[] contenido, String uri, String formato, boolean timeStamp, boolean detached) throws FirmaServiceException {
        String firma = null;
        try {
        	firma = firmaPort.signByFormat(contenido, uri, INVOKING_APP, ALIAS, formato, timeStamp, detached);
        } catch (Exception e) {
            throw newFirmaServiceException("Error al intentar firmar con formato", e);
        }
        return firma;
	}
    
	@Override
	public boolean validarFirmaEnFormato(byte[] contenido, String firma, String formato) throws FirmaServiceException {
		boolean result = false;
        try {
            VerifySignatureResult vefifyResult = firmaPort.verifySignatureByFormat(contenido, firma.getBytes(), INVOKING_APP, formato);
            
            if (vefifyResult != null)
                result = vefifyResult.isValido();
        } catch (NullPointerException e) {
            throw e;
        } catch (Exception e) {
            throw newFirmaServiceException("Error al validar la firma con formato", e);
        }
        return result;
	}

    @Override
	public InfoCert extraerCertificado(String firma) throws FirmaServiceException {
        String certificado = extraerCertificadoDeFirma(firma);
        boolean certificadoValido = isValidCertificado(certificado);
        if(!certificadoValido)
            throw new FirmaServiceException("El certificado no es válido");
        return getInformacion(certificado);
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
            Firmante firmante = comprobarFirmanteValido(firmantes, valorDocumentofirmanteSolicitado, firmanteCertificado, documento);
			if(!Messages.hasErrors()){
				// Guarda la firma en el AED
                firmante.nombre = firmanteCertificado.nombre;
                agregarFirmaEnGestorDocumental(documento, firma, firmante);
            }
		}else{
			play.Logger.error("firmanteCertificado == null????");
		}
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
	@Override
	public String firmarEnServidor (Documento documento) throws FirmaServiceException {
		String firma = "";
		try {
			firma = firmarContenido(gestorDocumentalService.getDocumento(documento).getBytes());
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
	
	protected PlatinoSignatureServerBean getFirmaPort(){
    	return this.firmaPort;
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
	
	protected InfoCert getInformacion(String certificado) throws FirmaServiceException{
		try {
			List<StringArray> certInfo = firmaPort.getCertInfo(certificado, INVOKING_APP);
			InfoCert infoCert = new InfoCert(certInfo);
			return infoCert;
		} catch (Exception e) {
			throw newFirmaServiceException("Error al extraer la información del certificado", e);
		}
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
	
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.firma.url");
	}

	private FirmaServiceException newFirmaServiceException(String msg, Exception cause){
	    if(cause instanceof SignatureServiceException_Exception){
	        SignatureServiceException_Exception signatureException = (SignatureServiceException_Exception) cause;
	        return new FirmaServiceException(msg + " - " + signatureException.getFaultInfo().getMessage(), cause);
	    }else{
	        return new FirmaServiceException("Error al realizar firma signContent", cause);
	    }
	}
	
	private String getHostJavaScripts() {
		String baseJavascript = FapProperties.get("fap.platino.firma.baseJavascript");
		StringBuilder hostBuilder = new StringBuilder();
		if ((baseJavascript != null) && (!"undefined".equals(baseJavascript))) {
			hostBuilder.append(baseJavascript);
		} else {
			String baseUrl = FapProperties.get("application.baseUrl");
			String entornoJS = getEntornoJS(baseUrl);
			hostBuilder.append("https://")
					.append(entornoJS)
					.append("/platino/")
					.append(APPLET_FIRMA);
		}

		return hostBuilder.toString();
	}

	private String getEntornoJS(String baseUrl) {
		StringBuilder entorno = new StringBuilder();
		if (baseUrl.contains("sede.gobcan.es")) {
			entorno.append("sede.gobcan.es");
		} else {
			entorno.append("gobiernodecanarias.org");
		}
		entorno.insert(0,getPrefijoEntornoJS(entorno.toString()));
		return entorno.toString();
	}

	private String getPrefijoEntornoJS(String entornoJs) {
		String prefijoEntorno = "";
		if("pre".equals(JS_ENTORNO.toLowerCase())){
            if (entornoJs.contains("gobiernodecanarias.org")) {
                prefijoEntorno = "www-pre.";
            } else if (entornoJs.contains("sede.gobcan.es")) {
                prefijoEntorno = "pre-";
            }
        }else{
            if (entornoJs.contains("gobiernodecanarias.org")) {
                prefijoEntorno = "www.";
            }
        }
		return prefijoEntorno;
	}
	
	private Firmante firmanteFromCertInfo(InfoCert infoCert) {
        Firmante firmante = new Firmante();
        firmante.idtipo = infoCert.getIdTipo();
        firmante.idvalor = infoCert.getId();
        firmante.nombre = infoCert.getNombreCompleto();
        return firmante;
    }

	private boolean isValidCertificado(String certificado) throws FirmaServiceException{
		try {
			ValidateCertResult result = firmaPort.validateCert(certificado, INVOKING_APP);
			return result.getCode() == 6; //Codigo 6 Certificado OK
		} catch (Exception e) {
		    throw new FirmaServiceException("Error validando el certificado", e);
		}
	}
	
	private Boolean verificarContentSignature(byte[] content, byte[] signature) {
		boolean result = false;
		try {
			VerifySignatureResult verifySignatureByFormatResponse = firmaPort.verifySignatureByFormat(content, signature, INVOKING_APP, "CADES");

			if(verifySignatureByFormatResponse != null)
				result = verifySignatureByFormatResponse.isValido();
			
			play.Logger.info("verificarContentSignature() | verifySignatureByFormatResponse: "+verifySignatureByFormatResponse);
		} catch (SignatureServiceException_Exception e) {
			play.Logger.error("Error verificando el contenido de la firma", e);
		}	
		return result;
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
	            play.Logger.info("Error: este certificado no es válido para firmar este documento");
	        }else{
	            if(firmante.fechaFirma != null){
	            	Messages.error("Este certificado ya ha firmado el documento " + documento.descripcion);
	            	play.Logger.info("Error: Documento ya firmado por este certificado");
	            }

	            play.Logger.info("Firmante encontrado " + firmante.idvalor );
	            if(valorDocumentofirmanteSolicitado != null && !firmante.idvalor.equalsIgnoreCase(valorDocumentofirmanteSolicitado)){
	                Messages.error("Se esperaba la firma de " + valorDocumentofirmanteSolicitado);
	            }
	        }
	        return firmante;
	    }

	    private Firmante buscarFirmanteEnFirmantes(String idValorFirmante, List<Firmante> firmantes) {
	        Firmante firmante = null;
	        play.Logger.info("Posibles firmantes: ");
	        for (Firmante fB : firmantes) {
	        	play.Logger.info("Firmate: (idvalor: " + fB.idvalor + ", tipo: " + fB.tipo + ")");
	            if (fB.idvalor.equals(idValorFirmante)) {
	                firmante = fB;
	            }
	        }
	        return firmante;
	    }

}
