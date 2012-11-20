package controllers.fap;

import java.util.Calendar;

import javax.inject.Inject;

import org.joda.time.DateTime;

import models.Metadato;
import org.apache.log4j.Logger;

import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.aed.AedGestorDocumentalServiceImpl;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.AedPortType;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;

public class MetadatosFAPController extends InvokeClassController {
	
	private static final Logger logger = Logger.getLogger(MetadatosFAPController.class);
	@Inject
	public static GestorDocumentalService gestorDocumentalService;
	
	
	//
	// Metadatos fijos: 		VersionNTI, Organo y Tipo documental, OrigenCiudadanoAdministracion (ver metadatos.json)
	// Metadatos calculados: 	Identificador, FechaCaptura, EstadoElaboracion, Nombre del formato, TipoFirmasElectronicas
	//
	
	public static String getVersionNTI() {
		Metadato metadato = Metadato.find("select metadato from Metadato metadato where nombre = 'VersionNTI'").first();
		if (metadato != null)
			return metadato.valor;
		else 
			 logger.error("No se ha podido obtener el metadato VersionNTI");
		return null;
	}
	
	public static String getOrgano() {
		Metadato metadato = Metadato.find("select metadato from Metadato metadato where nombre = 'Organo'").first();
		if (metadato != null)
			return metadato.valor;
		else
			logger.error("No se ha podido obtener el metadato Organo");
		return null;
	}
	
	public static String getTipoDocumental() {
		Metadato metadato = Metadato.find("select metadato from Metadato metadato where nombre = 'TipoDocumental'").first();
		if (metadato != null)
			return metadato.valor;
		else
			logger.error("No se ha podido obtener el metadato TipoDocumental");
		return null;
	}
	
	public static String getOrigenCiudadanoAdministracion() {
		Metadato metadato = Metadato.find("select metadato from Metadato metadato where nombre = 'OrigenCiudadanoAdministracion'").first();
		if (metadato != null)
			return metadato.valor;
		else
			logger.error("No se ha podido obtener el metadato OrigenCiudadanoAdministracion");
		return null;
	}
	
	
	/**
	 * Metadato Identificador: 
	 * "ES_A05003341_" + AAAA + "_" + B(30). AAAA Año en que se digitaliza el documento. B(30) El identificador 
	 * único de documento asignado por el AED que forma parte de la URI del documento. Se rellena con espacios 
	 * a la derecha.
	 * 
	 * @param uriDocumento
	 * @return Identificador
	 */
	public static String getIdentificador(String uriDocumento) {
		try {
			return gestorDocumentalService.construyeIdentificador(uriDocumento);
		} catch (GestorDocumentalServiceException e) { e.printStackTrace(); }
		return null;
	}
	
	/**
	 * Metadato FechaCaptura: 
	 * Fecha en formato ISO 8601 en el que se incorpora el documento al AED. Fecha del AED.
	 * 
	 * @param uriDocumento
	 * @return Fecha de la captura
	 */
	public static DateTime getFechaCaptura(String uriDocumento) {
		try {
			return gestorDocumentalService.construyeFechaCaptura(uriDocumento);
		} catch (GestorDocumentalServiceException e) { e.printStackTrace(); }
		return null;
	}
	
	/**
	 * Metadato EstadoElaboracion: 
	 * "EE01" Original.
	 * "EE02" Copia electrónica auténtica con cambio de formato
	 * "EE02" Copia electrónica auténtica de documento papel
	 * "EE03" Copia electrónica parcial auténtica
	 * "EE99" Otros
	 *  
	 * @param uriDocumento
	 * @return 
	 */
	public static String getEstadoElaboracion(String uriDocumento) {
		try {
			return gestorDocumentalService.construyeEstadoElaboracion(uriDocumento);
		} catch (GestorDocumentalServiceException e) { e.printStackTrace(); }
		return null;
	}
	
	/**
	 * Metadato NombreFormato: 
	 * "PDF" o "PDF/A"
	 * 
	 * @param uriDocumento
	 * @return
	 */
	public static String getNombreFormato(String uriDocumento) {
		try {
			return gestorDocumentalService.construyeNombreFormato(uriDocumento);
		} catch (GestorDocumentalServiceException e) { e.printStackTrace(); }
		return null;
	}
	
	/*
	 *  ???????????????? Falta fijar el tipo de firma de los documentos  ?????????????
	 */
	public static String getTipoFirmasElectronicas(String uriDocumento) {	
		return null; 
	}

}
