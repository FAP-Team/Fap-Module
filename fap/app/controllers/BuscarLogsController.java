package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.joda.time.DateTime;
import org.postgresql.jdbc2.optional.SimpleDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import messages.Messages;
import models.BusquedaLogs;
import models.Log;
import models.SolicitudGenerica;
import play.Logger;
import play.data.binding.As;
import play.mvc.Util;
import validation.CustomValidation;
import controllers.fap.LoggerController;
import controllers.gen.BuscarLogsControllerGen;

public class BuscarLogsController extends BuscarLogsControllerGen {
	
	public static void index(String accion, Long idBusquedaLogs) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/BuscarLogs/BuscarLogs.html");
		}

		BusquedaLogs busquedaLogs = BuscarLogsController.getBusquedaLogs();

		log.info("Visitando página: " + "gen/BuscarLogs/BuscarLogs.html");
		renderTemplate("gen/BuscarLogs/BuscarLogs.html", accion, idBusquedaLogs, busquedaLogs);
	}
	
	@Util
	public static void buscar(Long idBusquedaLogs, BusquedaLogs busquedaLogs, String botonBuscar) {
		checkAuthenticity();
		if (!permisoBuscar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		BusquedaLogs dbBusquedaLogs = BuscarLogsController.getBusquedaLogs();

		BuscarLogsController.buscarBindReferences(busquedaLogs);

		if (!Messages.hasErrors()) {
			BuscarLogsController.buscarValidateCopy("editar", dbBusquedaLogs, busquedaLogs);
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
			BuscarLogsController.buscarValidateRules(dbBusquedaLogs, busquedaLogs);
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/BuscarLogs/BuscarLogs.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/BuscarLogs/BuscarLogs.html" + " , intentada sin éxito (Problemas de Validación)");
		
		BuscarLogsController.buscarRender(busquedaLogs, filas);
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
		redirect("BuscarLogsController.index", "editar");
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

	@Util
	@SuppressWarnings("deprecation")
	private static boolean esHoy(Date date) {
		Date hoy = new Date();
		if ((hoy.getDate() == date.getDate()) && (hoy.getMonth() == date.getMonth()) && (hoy.getYear() == date.getYear()))
			return true;
		return false;
	}


	@Util
	private static String nombreFichero(Date date, String loggerName) {
		
		String fileName = null;
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("app");
		
		// busca entre los appenders dos que sean de tipo DailyRollingFile,
		// preferentemente aquel cuyo nombre es "Daily" o "Auditable", que son los configurados inicialmente en
		// los ficheros de configuracion
		
		for (Enumeration<Appender> e = logger.getAllAppenders(); e.hasMoreElements();){
			Appender appender = e.nextElement();
			if (appender instanceof DailyRollingFileAppender){
				fileName = ((DailyRollingFileAppender)appender).getFile();
				if (((DailyRollingFileAppender)appender).getName().equals(loggerName)){
					break;
				}
			}
		}
		if (fileName == null){
			return fileName;
		}
		
		if (!esHoy(date)) {
			fileName += "."+(date.getYear()+1900)+"-";
			if (date.getMonth() < 9)
				fileName += "0";
			fileName += (date.getMonth()+1)+"-";
			if (date.getDate() < 10)
				fileName += "0";
			fileName += date.getDate();
		}
		return fileName;
	}

	@SuppressWarnings("deprecation")
	public static Date diaSiguiente (Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, 1);
		return cal.getTime();
	}
	
}
