package controllers;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import messages.Messages;
import models.Agente;
import models.SolicitudGenerica;
import play.modules.guice.InjectSupport;
import play.mvc.Util;
import services.FirmaService;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.pruebaPlatinoGestorDocumentalControllerGen;
import es.gobcan.platino.servicios.sgrde.Expediente;

public class pruebaPlatinoGestorDocumentalController extends pruebaPlatinoGestorDocumentalControllerGen {

	@Inject
    static GestorDocumentalService gestorDocumentalService;
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formBuscarDocEnExpediente(Long idSolicitud, String btnBuscarDocEnExpediente) {
		checkAuthenticity();
		if (!permisoFormBuscarDocEnExpediente("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			//Prueba de obtener docs del expediente.
			SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
			List<String> rutasDocumentos = new ArrayList<String>();
			try {
				GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		        if(gestorDocumentalService == null){
		            Messages.fatal("El servicio no se inyectó correctamente");
		        } else {
					System.out.println("Inyeccion del GestorDoc: "+gestorDocumentalService);
					rutasDocumentos = gestorDocumentalService.getDocumentosEnExpediente(solicitud.expedientePlatino.getRuta());
		        }
			} catch (GestorDocumentalServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Listando rutas");
			for (String ruta : rutasDocumentos) {
				System.out.println("Ruta: "+ruta);
			}
		}

		if (!Messages.hasErrors()) {
			pruebaPlatinoGestorDocumentalController.formBuscarDocEnExpedienteValidateRules();
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/pruebaPlatinoGestorDocumental/pruebaPlatinoGestorDocumental.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/pruebaPlatinoGestorDocumental/pruebaPlatinoGestorDocumental.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		pruebaPlatinoGestorDocumentalController.formBuscarDocEnExpedienteRender(idSolicitud);
	}
	
}
