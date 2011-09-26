package platino;

import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;

import es.gobcan.eadmon.aed.ws.AedExcepcion;

import aed.AedClient;
import messages.Messages;
import models.Documento;
import models.Firmante;
import models.Persona;

public class Firma {

	public String firma;

	/**
	 * Valida la firma y devuelve el firmante del documento
	 * @param documento
	 * @return firmante del documento, null si se produjo un error
	 * @throws IOException 
	 * @throws AedExcepcion 
	 */
	public Firmante validaFirmayObtieneFirmante(Documento documento) {
		if(firma == null || firma.isEmpty()){
			Messages.error("La firma llegó vacía");
			return null;
		}
		
		Firmante firmante = null;
		try {
			byte[] contenido = AedClient.obtenerDocBytes(documento.uri);
			firmante = FirmaClient.validateXMLSignature(contenido, firma);
			
			if(firmante == null){
				Messages.error("Error validando la firma");
			}
			
		} catch (Exception e) {
			play.Logger.error("Error obteniendo el documento del AED para verificar la firma. Uri = " + documento.uri);
			Messages.error("Error validando la firma");
		}
		

		
		return firmante;		
	}
	
}
