package controllers;

import messages.Messages;
import models.ResolucionFAP;
import play.mvc.Util;
import resolucion.ResolucionBase;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.AportarDocumentoResolucionControllerGen;

public class AportarDocumentoResolucionController extends AportarDocumentoResolucionControllerGen {

	@Util
	private static ResolucionBase getResolucionObject (Long idResolucionFAP) throws Throwable {
		return ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(Long idResolucionFAP, ResolucionFAP resolucionFAP, java.io.File subirArchivoAportarDocumentoResolucion) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		ResolucionFAP dbResolucionFAP = AportarDocumentoResolucionController.getResolucionFAP(idResolucionFAP);

		AportarDocumentoResolucionController.AportarDocumentoResolucionBindReferences(resolucionFAP, subirArchivoAportarDocumentoResolucion);

		if (!Messages.hasErrors()) {

			AportarDocumentoResolucionController.AportarDocumentoResolucionValidateCopy("editar", dbResolucionFAP, resolucionFAP, subirArchivoAportarDocumentoResolucion);

		}

		if (!Messages.hasErrors()) {
			AportarDocumentoResolucionController.editarValidateRules(dbResolucionFAP, resolucionFAP, subirArchivoAportarDocumentoResolucion);
		}
		if (!Messages.hasErrors()) {
			dbResolucionFAP.save();
			// Se avanza de la fase creada a la fase preparada 
			ResolucionBase resolBase = null;
			try {
				resolBase = getResolucionObject (idResolucionFAP);
				resolBase.setLineasDeResolucion(idResolucionFAP);
				resolBase.avanzarFase_Creada(dbResolucionFAP);
			} catch (Throwable e) {
				play.Logger.error("Error obteniendo tipo de resolución: " + e.getMessage());
				Messages.error("Error obteniendo el tipo de resolución");
			}
			log.info("Acción Editar de página: " + "gen/AportarDocumentoResolucion/AportarDocumentoResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/AportarDocumentoResolucion/AportarDocumentoResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		AportarDocumentoResolucionController.editarRender(idResolucionFAP);
	}

}
