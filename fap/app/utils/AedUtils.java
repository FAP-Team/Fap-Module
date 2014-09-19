package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import models.Documento;
import models.Firmante;
import models.Persona;

import play.libs.Crypto;
import play.mvc.Router;
import properties.FapProperties;

public class AedUtils {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private static final Pattern pattern = Pattern.compile("date=\\[(.*?)\\]&uri=\\[(.*?)\\]");
	
	private static String actualDate(){
		return dateFormat.format(new Date());
	}
	
	public static String encriptarUri(String uri){
		String tmp = "date=[" + actualDate() + "]&uri=[" + uri + "]";
		return Crypto.encryptAES(tmp);
	}
	
	public static String desencriptarUri(String uri){
		String tmp = Crypto.decryptAES(uri);
		// Compile and use regular expression
		Matcher matcher = pattern.matcher(tmp);
		if (matcher.find()) {
			if(matcher.group(1).equals(actualDate())){
				return matcher.group(2);
			}
		}
		return null;
	}
	
	public static String crearUrl(String uri){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("k", encriptarUri(uri));
		return Router.reverse("fap.DescargasAedController.descargar", params).toString();		
	}
	
	public static String crearFullUrl(String uri){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("k", encriptarUri(uri));
		return Router.getFullUrl("fap.DescargasAedController.descargar", params).toString();		
	}
	
	public static String crearUrlConInformeDeFirma(String uri){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("k", encriptarUri(uri));
		return Router.reverse("fap.DescargasAedController.descargarFirmado", params).toString();		
	}
	
	public static String crearFullConInformeDeFirma(String uri){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("k", encriptarUri(uri));
		return Router.getFullUrl("fap.DescargasAedController.descargarFirmado", params).toString();		
	}
	
	/**
	 * Si existe un servidor Apache delante, y no tiene configurado el ProxyPreserveHost;
	 * es decir: ProxyPreserveHost Off
	 * @param uri
	 * @return
	 */
	public static String crearExternalFullUrl(String uri) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("k", encriptarUri(uri));
        Router.ActionDefinition rd = Router.reverse("fap.DescargasAedController.descargar", params);
        
        String urlCompleta = FapProperties.get("application.baseUrl");
        urlCompleta += rd.url;
        
        return urlCompleta;                
	}
	
	public static String crearExternalFirmadoFullUrl(String uri) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("k", encriptarUri(uri));
        Router.ActionDefinition rd = Router.reverse("fap.DescargasAedController.descargarFirmado", params);
        
        String urlCompleta = FapProperties.get("application.baseUrl");
        urlCompleta += rd.url;
        
        return urlCompleta;                
	}
	
	/**
	 * 
	 * Añade los firmantes de un documento de platino a un documento FAP.
	 * 
	 * @param documento Documento FAP
	 * @param documentoPlatino Documento de platino
	 */
	public static void docPlatinotoDocumentoFirmantes(Documento documento, es.gobcan.platino.servicios.sgrde.Documento documentoPlatino){
		
		DateTimeFormatter formateoFecha = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		
		for (int i = 0; i < documentoPlatino.getMetaInformacion().getFirmasElectronicas().getInformacionFirmaElectronica().size(); i++) {

			Firmante firmante = new Firmante();
			firmante.idvalor = documentoPlatino.getMetaInformacion().getFirmasElectronicas().getInformacionFirmaElectronica().get(i).getIdFirmante();
			firmante.nombre = documentoPlatino.getMetaInformacion().getFirmasElectronicas().getInformacionFirmaElectronica().get(i).getDescFirmante();
			
			String fechaHora = documentoPlatino.getMetaInformacion().getFirmasElectronicas().getInformacionFirmaElectronica().get(i).getFechaFirma().toXMLFormat();
			String dateToken = fechaHora.substring(0, fechaHora.indexOf('T'));
			String hourToken = fechaHora.substring(fechaHora.indexOf('T')+1, fechaHora.indexOf('+'));
			firmante.fechaFirma = formateoFecha.parseDateTime(dateToken + " " + hourToken);

			documento.firmantes.todos.add(firmante);
		}		
	}
	
}
