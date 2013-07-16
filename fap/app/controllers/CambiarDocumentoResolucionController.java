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

		if (botonSubirNuevoDocumentoResolucion != null) {
			ResolucionFAP resolucionFAP = EditarResolucionController.getResolucionFAP(idResolucionFAP);
			try {
				gestorDocumentalService.deleteDocumento(resolucionFAP.registro.oficial);
				resolucionFAP.registro.oficial.tipo = null;
				resolucionFAP.registro.oficial.descripcion = null;
				resolucionFAP.registro.oficial.uri = null;
				resolucionFAP.save();
				redirect("AportarDocumentoResolucionController.index", AportarDocumentoResolucionController.getAccion(), idResolucionFAP);
			} catch (GestorDocumentalServiceException e) {
				Messages.error("Error borrando el documento de resolución del gestor documental");
			}
		}
		
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

}
