package controllers;

import messages.Messages;
import models.BusquedaLogs;
import play.mvc.Util;
import validation.CustomValidation;
import controllers.gen.BuscarLgsControllerGen;

public class BuscarLgsController extends BuscarLgsControllerGen {

	public static void index(String accion, Long idBusquedaLogs) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/BuscarLgs/BuscarLgs.html");
		}

		BusquedaLogs busquedaLogs = BuscarLgsController.getBusquedaLogs();

		log.info("Visitando página: " + "gen/BuscarLgs/BuscarLgs.html");
		renderTemplate("gen/BuscarLgs/BuscarLgs.html", accion, idBusquedaLogs, busquedaLogs);
	}
	
	@Util
	public static void buscar(Long idBusquedaLogs, BusquedaLogs busquedaLogs, String botonBuscar) {
		checkAuthenticity();
		if (!permisoBuscar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		BusquedaLogs dbBusquedaLogs = BuscarLgsController.getBusquedaLogs();

		BuscarLgsController.buscarBindReferences(busquedaLogs);

		if (!Messages.hasErrors()) {
			BuscarLgsController.buscarValidateCopy("editar", dbBusquedaLogs, busquedaLogs);
		}
		
		int filas = 0;
		if (!Messages.hasErrors()) {
			// Se comprueba si se han seleccionado filas y se pasa a entero.
			if ((dbBusquedaLogs.numeroFilasSeleccionadas != null) && (!dbBusquedaLogs.numeroFilasSeleccionadas.equals(""))) {
				if (dbBusquedaLogs.numeroFilasSeleccionadas.matches("[0-9]*")) {
					filas = Integer.parseInt(dbBusquedaLogs.numeroFilasSeleccionadas);
				} else {
					Messages.error("Debe introducir un número");
				}
			}
		}

		if (!Messages.hasErrors()) {
			BuscarLgsController.buscarValidateRules(dbBusquedaLogs, busquedaLogs);
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/BuscarLgs/BuscarLgs.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/BuscarLgs/BuscarLgs.html" + " , intentada sin éxito (Problemas de Validación)");
		
		BuscarLgsController.buscarRender(busquedaLogs, filas);
	}

	@Util
	public static void buscarRender(BusquedaLogs busquedaLogs, int filas) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			String[] fechaLog = null;
			String fecha = null;
			// Se coge únicamente la fecha (yyyy-MM-dd) del tipo DateTime.
			if (busquedaLogs.fechaLog != null) {
				fechaLog = busquedaLogs.fechaLog.toString().split("T");
				fecha = fechaLog[0];
			}
			// Se pasa al index de MostrarLogsController la entidad, las filas seleccionadas
			// como entero y la fecha introducida.
			redirect("MostrarLogsController.index", "editar", busquedaLogs, filas, fecha);
		}

		Messages.keep();
		redirect("BuscarLgsController.index", "editar");
	}
	
	@Util
	public static void buscarValidateCopy(String accion, BusquedaLogs dbBusquedaLogs, BusquedaLogs busquedaLogs) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("busquedaLogs", busquedaLogs);
		dbBusquedaLogs.buquedaPorAtributos = busquedaLogs.buquedaPorAtributos;
		if ((busquedaLogs.buquedaPorAtributos != null) && (busquedaLogs.buquedaPorAtributos == true)) {
			dbBusquedaLogs.fechaLog = busquedaLogs.fechaLog;
			dbBusquedaLogs.tipoLog = busquedaLogs.tipoLog;
			dbBusquedaLogs.mensajeLog = busquedaLogs.mensajeLog;
			dbBusquedaLogs.usuario = busquedaLogs.usuario;
			dbBusquedaLogs.claseLog = busquedaLogs.claseLog;
		} else {
			dbBusquedaLogs.fechaLog = null;
			dbBusquedaLogs.tipoLog = null;
			dbBusquedaLogs.mensajeLog = null;
			dbBusquedaLogs.usuario = null;
			dbBusquedaLogs.claseLog = null;
		}
		
		if (!busquedaLogs.numeroFilasSeleccionadas.equals("")) {
			dbBusquedaLogs.numeroFilasSeleccionadas = busquedaLogs.numeroFilasSeleccionadas;
		} else {
			dbBusquedaLogs.numeroFilasSeleccionadas = null;
		}
	}
	
}
