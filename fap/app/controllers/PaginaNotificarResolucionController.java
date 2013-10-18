package controllers;

import messages.Messages;
import models.LineaResolucionFAP;
import play.mvc.Util;
import resolucion.ResolucionBase;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.PaginaNotificarResolucionControllerGen;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadoResolucionPublicacionEnum;

public class PaginaNotificarResolucionController extends PaginaNotificarResolucionControllerGen {
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formGenerarOficioRemision(Long idResolucionFAP, String botonGenerarOficioRemision) {
		checkAuthenticity();
		if (!permisoFormGenerarOficioRemision("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {

		}

		if (!Messages.hasErrors()) {
			PaginaNotificarResolucionController.formGenerarOficioRemisionValidateRules();
		}
		
		ResolucionBase resolBase = null;
		if (!Messages.hasErrors()) {
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
				resolBase.generarOficioRemision(idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		} else {
			play.Logger.info("No se genero el documento de oficio de remision para la resolucion "+idResolucionFAP);
		}

		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaNotificarResolucionController.formGenerarOficioRemisionRender(idResolucionFAP);
	}
	
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
				resolBase.notificarCopiarEnExpedientes(idResolucionFAP);
				resolBase.resolucion.estadoNotificacion = EstadoResolucionEnum.notificada.name();
				resolBase.resolucion.save();
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		}


		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito");
			redirect("EditarResolucionController.index", EditarResolucionController.getAccion(), idResolucionFAP);
		} else {
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
			PaginaNotificarResolucionController.formCopiaExpedienteRender(idResolucionFAP);
		}
	}
	
//	public static void guardarValoresNotificacion (int plazoAcceso, int frecuenciaRecordatorioAcceso, int plazoRespuesta, int frecuenciaRecordatorioRespuesta){
//		System.out.println("Llegamos por Ajax");
//		Long idResolucionFap = Long.parseLong(params.get("idResolucionFap"));
//		ResolucionBase resolBase = null;		
//		try {
//				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFap);
//		} catch (Exception e) {
//			e.printStackTrace();	
//		} catch (Throwable e) {
//		// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for (LineaResolucionFAP linea : resolBase.resolucion.lineasResolucion) {
//			linea.solicitud.notificaciones.get(linea.solicitud.notificaciones.size()-1)
//		}	
//	}

}
