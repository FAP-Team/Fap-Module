package controllers;

import messages.Messages;
import play.mvc.Util;
import resolucion.ResolucionBase;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.PaginaNotificarResolucionControllerGen;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadoResolucionPublicacionEnum;

public class PaginaNotificarResolucionController extends PaginaNotificarResolucionControllerGen {
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formCopiaExpediente(Long idResolucionFAP, String btnCopiaExpediente) {
		checkAuthenticity();
		if (!permisoFormCopiaExpediente("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		if (!Messages.hasErrors()) {
			PaginaNotificarResolucionController.formCopiaExpedienteValidateRules();
		}
		
		ResolucionBase resolBase = null;		
		if (!Messages.hasErrors()) {
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
				resolBase.publicarCopiarEnExpedientes(idResolucionFAP);
				resolBase.resolucion.estadoNotificacion = EstadoResolucionEnum.notificada.name();
				resolBase.resolucion.save();
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		}


		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaNotificarResolucionController.formCopiaExpedienteRender(idResolucionFAP);
	}
	
}
