package controllers;

import java.io.File;

import reports.Report;
import messages.Messages;
import models.Solicitud;
import controllers.gen.RenderPdfControllerGen;

public class RenderPdfController extends RenderPdfControllerGen {

	public static void renderPdfSolicitud(Long idSolicitud, String pdfBotonRender) {
		checkAuthenticity();
		if (!permisoRenderPdfSolicitud("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			File borrador = null;
			Solicitud solicitud = getSolicitud(idSolicitud);
			try {
				new Report("reports/solicitud_simple.html").header("reports/header.html").footer("reports/footer-borrador.html").registroSize().renderResponse(solicitud);
			}
			catch (Exception ex1) {
				Messages.error("Error en la generaciÃ³n del pdf "+ex1.getMessage());
			}
		}

		if (!Messages.hasErrors()) {
			RenderPdfController.renderPdfSolicitudValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/RenderPdf/RenderPdf.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/RenderPdf/RenderPdf.html" + " , intentada sin éxito (Problemas de Validación)");
		RenderPdfController.renderPdfSolicitudRender(idSolicitud);
	}
}
