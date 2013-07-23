package controllers;

import messages.Messages;
import play.mvc.Util;
import resolucion.ResolucionBase;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.PaginaFinalizarResolucionControllerGen;
import enumerado.fap.gen.EstadoResolucionEnum;

public class PaginaFinalizarResolucionController extends PaginaFinalizarResolucionControllerGen {

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formFinalizar(Long idResolucionFAP, String btnFinalizar) {
		checkAuthenticity();
		if (!permisoFormFinalizar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {

		}

		if (!Messages.hasErrors()) {
			PaginaFinalizarResolucionController.formFinalizarValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			try {
				ResolucionBase resolucionBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
				resolucionBase.avanzarFase_PublicadaYONotificada(resolucionBase.resolucion);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución base", e);
			}
		}
		
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaFinalizarResolucion/PaginaFinalizarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaFinalizarResolucion/PaginaFinalizarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaFinalizarResolucionController.formFinalizarRender(idResolucionFAP);
	}
	
}
