package controllers.fap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;

import models.Metadato;
import models.Metadatos;

import org.apache.log4j.Logger;

import config.InjectorConfig;

import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.aed.AedGestorDocumentalServiceImpl;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.AedPortType;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;

public class MetadatosFAPController extends InvokeClassController {
	
	private static final Logger logger = Logger.getLogger(MetadatosFAPController.class);
	
	// (Metadatos.json)
	// Metadatos comunes:  VersionNTI, Organo, Tipo documental, OrigenCiudadanoAdministracion, TipoFirmasElectronicas 
	// Metadatos calculados:  Identificador, FechaCaptura, Nombre del formato
	// Metadatos preguntados al usuario:  EstadoElaboracion
	
	/**
	 * Obtenemos la lista de metadatos correspondientes a un tipoDocumento seteados en base de datos (metadatos.json).
	 * 
	 * @param tipoDocumento
	 * @return Lista con los metadatos 
	 */
	public static List<Metadato> getMetadatosComunes(String tipoDocumento) {
		Metadatos metadatos = Metadatos.find("select metadato from Metadatos metadato where tipodocumento=?", tipoDocumento).first();
		if (metadatos != null)
			return metadatos.listaMetadatos;
		else 
			 logger.error("No se ha podido obtener los metadatos correspondientes al tipoDocumento " + tipoDocumento);
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
		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		try {
			return gestorDocumentalService.construyeMetadatoNombreFormato(uriDocumento);
		} catch (GestorDocumentalServiceException e) { e.printStackTrace(); }
		return null;
	}

	/**
	 * 
	 * @param uriDocumento
	 */
	public static void setMetadatos(String uriDocumento, String estadoElaboracion, String tipoDocumento) {
		List<Metadato> listaMetadatos = new ArrayList<Metadato>();
		List<Metadato> listaMetadatosComunes = new ArrayList<Metadato>();
		listaMetadatosComunes = getMetadatosComunes(tipoDocumento);
		String organo = null;
		
		// Metadatos comunes a todos los tipos de documentos (guardados en bbdd a partir de metadatos.json)
		//  VersionNTI, Organo, Tipo documental, OrigenCiudadanoAdministracion, TipoFirmasElectronicas 
		if (listaMetadatosComunes != null)
			for(Metadato m : listaMetadatosComunes) {
				listaMetadatos.add(m);
				if (m.nombre.equals("Organo"))
						organo = m.valor;
			}
		
		// Metadatos preguntados al usuario
		Metadato metadatoEE = new Metadato("EstadoElaboracion", estadoElaboracion);
		listaMetadatos.add(metadatoEE);
		
		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);			
		try {
			// Metadatos calculados
			Metadato metadatoId = new Metadato("Identificador", gestorDocumentalService.construyeMetadatoIdentificador(uriDocumento, organo));
			listaMetadatos.add(metadatoId);
			String fecha = (gestorDocumentalService.construyeMetadatoFechaCaptura(uriDocumento) == null) ? null : gestorDocumentalService.construyeMetadatoFechaCaptura(uriDocumento).toString();
			Metadato metadatoFecha = new Metadato("FechaCaptura", fecha);	
			listaMetadatos.add(metadatoFecha);
			Metadato metadatoFormato = new Metadato("Nombre del formato", gestorDocumentalService.construyeMetadatoNombreFormato(uriDocumento));		
			listaMetadatos.add(metadatoFormato);
		} catch (GestorDocumentalServiceException e) { e.printStackTrace(); }

		try {
			gestorDocumentalService.setMetadatosDocumento(uriDocumento, listaMetadatos);
		} catch (GestorDocumentalServiceException e) { e.printStackTrace(); }
	}
	
	/**
	 * 
	 * @param uriDocumento
	 * @return lista de metadatos del documento
	 */
	public static List<Metadato> getMetadatosDocumento(String uriDocumento) {
		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		try {
			return gestorDocumentalService.getMetadatosDocumento(uriDocumento);
		} catch (GestorDocumentalServiceException e) { e.printStackTrace(); }
		return null;
	}

}
