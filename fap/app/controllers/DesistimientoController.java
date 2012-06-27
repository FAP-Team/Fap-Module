package controllers;

import play.mvc.Util;
import tramitacion.TramiteDesistimiento;
import messages.Messages; 
import models.SolicitudGenerica;
import controllers.gen.DesistimientoControllerGen;

public class DesistimientoController extends DesistimientoControllerGen {
	
	 public static void prepararFirmar(Long idSolicitud, Long idDesistimiento, String botonPrepararFirmar) {
		checkAuthenticity();
		if ((permisoPrepararFirmar("editar")) || (permisoPrepararFirmar("crear"))) {
		 	SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
		 	if(!validation.hasErrors()) {
		 	   TramiteDesistimiento tramite = new TramiteDesistimiento(solicitud);
 		 	   tramite.prepararFirmar();
 		 	   tramite.guardar();
		 	}else{
		 	   Messages.error("No tiene permisos suficientes para realizar la acción");
		 	}
		 	
		 	if (!Messages.hasErrors()) {
		 	    log.info("Acción Editar de página: " + "gen/Desistimiento/Desistimiento.html" + " , intentada con éxito");
		 	} else
		 	   log.info("*Acción Editar de página: " + "gen/Desistimiento/Desistimiento.html" + " , intentada sin éxito (Problemas de Validación)");
		DesistimientoController.prepararFirmarRender(idSolicitud, idDesistimiento);
		}
	 }
		 	       
	 public static void deshacer(Long idSolicitud, Long idDesistimiento, String botonModificar) {
		checkAuthenticity();
		if ((permisoDeshacer("editar")) || (permisoDeshacer("crear"))) {
		   SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
		   if(!validation.hasErrors()){
			   TramiteDesistimiento tramite = new TramiteDesistimiento(solicitud);
			   play.Logger.info("tramite: "+tramite);
			   tramite.deshacer();
		    }
		}else{
		  Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		 	
		if (!Messages.hasErrors()) {
		   DesistimientoController.deshacerValidateRules();
		}
		if (!Messages.hasErrors()) {
		   log.info("Acción Editar de página: " + "gen/Desistimiento/Desistimiento.html" + " , intentada con éxito");
		} else
		   log.info("Acción Editar de página: " + "gen/Desistimiento/Desistimiento.html" + " , intentada sin éxito (Problemas de Validación)");
		   DesistimientoController.deshacerRender(idSolicitud, idDesistimiento);
		}

	 
	 
		@Util
		// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
		public static void frmPresentar(Long idSolicitud, Long idDesistimiento, String botonPresentar) {
			checkAuthenticity();
			if (permisoFrmPresentar("editar") || permisoFrmPresentar("crear")) {	}
			else{
				Messages.error("No tiene permisos suficientes para realizar la acción");
			}
			if (!Messages.hasErrors()) {
				DesistimientoController.frmPresentarValidateRules();
			}
			if (!Messages.hasErrors()) {
				log.info("Acción Editar de página: " + "gen/Desistimiento/Desistimiento.html" + " , intentada con éxito");
				
			} else
				log.info("Acción Editar de página: " + "gen/Desistimiento/Desistimiento.html" + " , intentada sin éxito (Problemas de Validación)");
			
			DesistimientoController.frmPresentarRender(idSolicitud, idDesistimiento);
		}

		
		@Util
		public static void frmPresentarRender(Long idSolicitud, Long idDesistimiento) {
			if (!Messages.hasMessages()) {
				Messages.ok("Página editada correctamente");
				Messages.keep();
				redirect("DesistimientoPresentarController.index", "editar", idSolicitud, idDesistimiento);
			}
			Messages.keep();
			redirect("DesistimientoController.index", "editar", idSolicitud, idDesistimiento);
		}
		
		
}
