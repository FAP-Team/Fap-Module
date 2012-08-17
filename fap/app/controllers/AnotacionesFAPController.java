package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import messages.Messages;
import models.SolicitudGenerica;
import play.mvc.Util;
import reports.Report;
import controllers.gen.AnotacionesFAPControllerGen;

public class AnotacionesFAPController extends AnotacionesFAPControllerGen {

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmSeguimientoSolicitud(Long idSolicitud, String btnSeguimientoSolictud) {
		checkAuthenticity();
		if (!permisoFrmSeguimientoSolicitud("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);

		if (!Messages.hasErrors()) {
			File informeSeguimiento;
			try {
				// Genera el informe
				informeSeguimiento = new Report("reports/seguimientoInforme.html")
									.header("reports/header.html")
									.footer("reports/footer-borrador.html")
									.renderTmpFile(solicitud);
				
				renderBinary(informeSeguimiento);
				
			} catch (Exception e) {
				play.Logger.error("Error generando el borrador del informe. "+e);
				Messages.error("Error generando el borrador del informe.");
			}
		}
		
		if (!Messages.hasErrors()) {
			AnotacionesFAPController.frmSeguimientoSolicitudValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/AnotacionesFAP/AnotacionesFAP.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/AnotacionesFAP/AnotacionesFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		AnotacionesFAPController.frmSeguimientoSolicitudRender(idSolicitud);
	}

}
