package controllers.fap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import properties.FapProperties;

public class WSPortafirma extends GenericController {

	public static void setFirmada (Long idSolicitud, String token, String hash) {
		play.Logger.info("setFirmada (Portafirma) -> Solicitud "+idSolicitud+", token <"+token+">, hash <"+hash+">");
		
		if  (idSolicitud == null || token == null || hash == null) {
			play.Logger.error("NULL en setFirmada (Portafirma) -> Solicitud "+idSolicitud+", token <"+token+">, hash <"+hash+">");
			return;
		}
		
		String value = idSolicitud.toString() + "_" + token;
		String clave = FapProperties.get("application.secret"); //fap.portafirma.key
		String result = "";
		
		try {
			result = encodeHmac(value, clave);
		} catch (Exception e) {
			play.Logger.error("No se pudo obtener la HMAC ");
			return;
		}
		
		if (hash.equals(result)) {
			play.Logger.info("El hash es el correcto .... debemos realizar los cambios necesarios");
		} else{
			play.Logger.error("El hash recibido no es igual al calculado: "+result);
		}
		
	}
	
	private static String encodeHmac(String value, String key) {
		try {
			byte[] keyBytes = key.getBytes();
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

			// Get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);

			// Compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(value.getBytes());

			// Convert raw bytes to Hex
			return Base64.encodeBase64URLSafeString(rawHmac);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
