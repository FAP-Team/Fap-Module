package controllers;

import messages.Messages;
import models.ResolucionFAP;
import play.mvc.Util;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import config.InjectorConfig;
import controllers.gen.CambiarDocumentoResolucionControllerGen;

public class CambiarDocumentoResolucionController extends CambiarDocumentoResolucionControllerGen {

	//Inyeccion manual	
	static GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formCambiarDocumentoResolucion(Long idResolucionFAP, String botonSubirNuevoDocumentoResolucion) {
		checkAuthenticity();
		if (!permisoFormCambiarDocumentoResolucion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (botonSubirNuevoDocumentoResolucion != null)
			botonSubirNuevoDocumentoResolucionFormCambiarDocumentoResolucion(idResolucionFAP);
			
		if (!Messages.hasErrors()) {

		}

		if (!Messages.hasErrors()) {
			CambiarDocumentoResolucionController.formCambiarDocumentoResolucionValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/CambiarDocumentoResolucion/CambiarDocumentoResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/CambiarDocumentoResolucion/CambiarDocumentoResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		CambiarDocumentoResolucionController.formCambiarDocumentoResolucionRender(idResolucionFAP);
	}
	
	@Util
	public static void botonSubirNuevoDocumentoResolucionFormCambiarDocumentoResolucion(Long idResolucionFAP) {
		//Sobreescribir este método para asignar una acción
		ResolucionFAP resolucionFAP = EditarResolucionController.getResolucionFAP(idResolucionFAP);
		try {
			gestorDocumentalService.deleteDocumento(resolucionFAP.docResolucion);
			resolucionFAP.docResolucion.uri = null;
			redirect("AportarDocumentoResolucionController.index", AportarDocumentoResolucionController.getAccion(), idResolucionFAP);
		} catch (GestorDocumentalServiceException e) {
			Messages.error("Error borrando el documento de resolución del gestor documental");
		}		
	}

}
