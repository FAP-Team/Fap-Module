package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import messages.Messages;
import models.SolicitudGenerica;
import play.mvc.Util;
import reports.Report;
import controllers.gen.SeguimientoControllerGen;

public class SeguimientoController extends SeguimientoControllerGen {

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void generarSeguimientoForm(String generarSeguimiento) {
		checkAuthenticity();
		if (!permisoGenerarSeguimientoForm("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			List<SolicitudGenerica> todasSolicitudes = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud").fetch();
			List<SolicitudGenerica> solicitudes = new ArrayList<SolicitudGenerica>();
			for (SolicitudGenerica sol: todasSolicitudes) {
				if (sol.datosAnotaciones.anotaciones.size() != 0) {
					solicitudes.add(sol);
				}
			}

			if (!Messages.hasErrors()) {
				File informeSeguimiento;
				try {
					// Genera el informe
					informeSeguimiento = new Report("reports/seguimientoInformeTodos.html")
										.header("reports/header.html")
										.footer("reports/footer-borrador.html")
										.renderTmpFile(solicitudes);
					
					renderBinary(informeSeguimiento);
					
				} catch (Exception e) {
					play.Logger.error("Error generando el borrador del informe. "+e);
					Messages.error("Error generando el borrador del informe.");
				}
			}
		}

		if (!Messages.hasErrors()) {
			SeguimientoController.generarSeguimientoFormValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/Seguimiento/Seguimiento.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/Seguimiento/Seguimiento.html" + " , intentada sin éxito (Problemas de Validación)");
		SeguimientoController.generarSeguimientoFormRender();
	}
	
}
