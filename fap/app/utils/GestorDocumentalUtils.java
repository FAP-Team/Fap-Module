package utils;

import java.io.File;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;

import models.Documento;
import models.ExpedientePlatino;

import org.joda.time.DateTime;

import config.InjectorConfig;
import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;

import platino.DatosDocumento;
import properties.FapProperties;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.platino.PlatinoBDOrganizacionServiceImpl;
import services.platino.PlatinoGestorDocumentalService;
import services.platino.PlatinoGestorDocumentalServiceException;
import services.platino.PlatinoPortafirmaServiceImpl;

public class GestorDocumentalUtils {
	
	/*
	 * Comprueba si en la property fap.gestordocumental.mimes está configurado
	 * que se acepte el tipo mime que se recibe por parámetro.
	 */
	public static boolean acceptMime(String mimeType){
		String type = mimeType.split("/")[0];
		if (FapProperties.get("fap.gestordocumental.mimes") == null)
			return true;
		Pattern pattern = Pattern.compile("[\\w-]+/(\\*|[\\w-]+)");
		for (String mime: FapProperties.get("fap.gestordocumental.mimes").split(",")){
			mime = mime.trim();
			if (pattern.matcher(mime).matches()){
				if (mime.split("/")[1].equals("*")){
					if (type.equals(mime.split("/")[0]))
						return true;
				}
				else{
					if (mime.equals(mimeType))
						return true;
				}
			}
		}
		return false;
	}
	
	/*
	 * Comprueba si en la property fap.gestordocumental.extensiones está configurado
	 * que se acepte la extensión de archivo que se recibe por parámetro.
	 */
	public static boolean acceptExtension(String extension){
		if (FapProperties.get("fap.gestordocumental.extensions") == null)
			return true;
		for (String ext: FapProperties.get("fap.gestordocumental.extensions").split(",")){
			if (extension.toLowerCase().equals(ext.trim().toLowerCase()))
				return true;
		}
		return false;
	}
	
	/*
	 * "C:\documentos\doc.txt"	--> "txt"
	 * "C:\documentos\doc"  	--> ""
	 */
	public static String getExtension(File file){
		String name = file.getName();
		int dot = name.lastIndexOf(".");
		if (dot != -1)
			return name.substring(dot + 1);
		return "";
	}
	
	private static void restoreSecurityHeadersBackoffice(Object service) {
		String backoffice = FapProperties.get("fap.platino.security.backoffice.uri");
		WSUtils.configureSecurityHeadersWithUser(service, backoffice);
	}
	
	private static void setupSecurityHeadersWithUser(Object service, String uid) {
		try {
			PlatinoBDOrganizacionServiceImpl platinoDBOrgPort = InjectorConfig.getInjector().getInstance(PlatinoBDOrganizacionServiceImpl.class);
			String userUri = platinoDBOrgPort.recuperarURIPersona(uid);
			WSUtils.configureSecurityHeadersWithUser(service, userUri);
			
		} catch (DBOrganizacionException_Exception e) {
			play.Logger.info("Error al configuar cabecera de seguridad para usuario: " + uid + ". " + e.getMessage());
			//throw new PortafirmaFapServiceException("Error al configuar cabecera de seguridad para usuario: " + uid + ". " + e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * @param uriDocumento URI del documento que se sube al gestor documental de Platino si no está en éste.
	 * @param uidUsuario Identificador único del funcionario en el ldap del gobierno. El identificador sepuede pasar en minúsculas o mayúsculas.
	 * @param service Servicio desde el cual se llama este método
	 */
	//TODO Este método debe ir en la implementación del GestorDocumental de Platino
	public static String obtenerURIPlatino(String uriDocumento, String uidUsuario, Object service) {
		PlatinoGestorDocumentalService platinoGestorDocumentalPort = InjectorConfig.getInjector().getInstance(PlatinoGestorDocumentalService.class);
		GestorDocumentalService gestorDocumentalPort = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		
		//Restauramos cabeceras de seguridad por si hace falta subir documentos a platino
		restoreSecurityHeadersBackoffice(service);
		
		//Caso en el que el documento se encuentra en el AED de la ACIISI
		Documento documento = Documento.findByUri(uriDocumento); // Documento subido al gestor documental de la ACIISI
		
		try {
			//Subir documento a firmar a gestor documental de platino (si no está subido)
			if ((documento != null) && (documento.uriPlatino == null)) { // El documento está en el Gestor Documental de la ACIISI y no está en Platino
				
				//Obtenemos la ruta del expediente (convertida a platino)
				ExpedientePlatino expedientePlatino = ExpedientePlatino.all().first();
				String uriPlatinoExpediente = platinoGestorDocumentalPort.convertToHexNoQuery(expedientePlatino.getRuta());
				
				//Obtenemos el documento original del gestor documental
				BinaryResponse doc = gestorDocumentalPort.getDocumentoByUri(documento.uri);
				
				//Configuramos los datos de subida del documento
				DatosDocumento datos = new DatosDocumento();
				datos.setContenido(doc.contenido.getDataSource());
				datos.setTipoMime(doc.contenido.getContentType());
				datos.setFecha(PlatinoPortafirmaServiceImpl.DateTime2XMLGregorianCalendar(DateTime.now())); // TODO: Cambiar el modo de conversion
				datos.setDescripcion(documento.descripcionVisible);
				datos.setAdmiteVersionado(true);
				
				//Subimos el documento al gestor documental de platino
				documento.uriPlatino = platinoGestorDocumentalPort.guardarDocumento(uriPlatinoExpediente, datos);
				documento.save();
				
				return documento.uriPlatino;
			}
			else if ((documento != null) && (documento.uriPlatino != null)) {  // El documento está en el Gestor Documental de la ACIISI y está en Platino
				return documento.uriPlatino;
			}
			else { //  El documento no está en el Gestor Documental de la ACIISI
				Documento documentoPlatino = Documento.findByUriPlatino(uriDocumento);
				if (documentoPlatino != null)
					return documentoPlatino.uriPlatino;
				else {
					play.Logger.error("El documento con la uri "+uriDocumento+" no existe.");
					return null;
				}
			}
		} catch (PlatinoGestorDocumentalServiceException e) {
			e.printStackTrace();
			play.Logger.error("Error al acceder al gestor documental de platino: " + e.getMessage());
			//throw new SolicitudFirmaExcepcion("Error al acceder al gestor documental de platino: " + e.getMessage(), e);
		} catch (GestorDocumentalServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			setupSecurityHeadersWithUser(service, uidUsuario);
		}
		return null;	
	}
	
}
