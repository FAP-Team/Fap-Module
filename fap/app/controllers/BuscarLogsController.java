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
	
	public static void logs(long fecha1, long fecha2, int filas, BusquedaLogs busquedaLogs) throws IOException{
		
		// Intervalo de fechas de la tabla
		Date date1 = new Date(fecha1);
		Date date2 = new Date(fecha2);
		int logsD=0, logsA=0;
		ArrayList<String> borrarDaily = new ArrayList<String>();
		ArrayList<String> borrarAuditable = new ArrayList<String>();
		Gson gson = new Gson();
		ArrayList<BufferedReader> brDaily = new ArrayList<BufferedReader>();
		ArrayList<BufferedReader> brAuditable = new ArrayList<BufferedReader>();
		ArrayList<FileReader> ficherosACerrar = new ArrayList<FileReader>();
		boolean seguirLeyendo=true, error;
		Date date = date1;
		while (seguirLeyendo){
			try {
				String ficheroLogs = nombreFichero(date, "Daily");
				if (ficheroLogs != null){
					error = false;
					// Si el fichero no es del día actual, lo recuperamos de los backups, descomprimiendolo
					if (!esHoy(date)){
						if (utils.ZipUtils.descomprimirEnZip("logs/backups/Daily/"+ficheroLogs.replace("logs/", "")+".zip", ficheroLogs)){
							// Lo anotamos para despues borrarlo, y no dejar basura
							borrarDaily.add(ficheroLogs);
						} else{
							error = true;
							play.Logger.error("Descompresión de 'logs/backups/Daily/"+ficheroLogs.replace("logs/", "")+".zip' fallida o no existe el fichero");
						}
					}
					if (!error){
						if ((new File(ficheroLogs)).exists()){
							FileReader ficheroDaily = new FileReader(ficheroLogs);
							brDaily.add(new BufferedReader(ficheroDaily));
							ficherosACerrar.add(ficheroDaily);
						} else {
							play.Logger.error("Fichero '"+ficheroLogs+"' no existe. Imposible mostrarlo en la tabla de Logs");
						}
					}
				}
			} catch (FileNotFoundException e) {
				play.Logger.error(e,"Fichero de log del Daily no encontrado");
			}
			
			try {
				String ficheroLogs = nombreFichero(date, "Auditable");
				if (ficheroLogs != null){
					error = false;
					// Si el fichero no es del día actual, lo recuperamos de los backups, descomprimiendolo
					if (!esHoy(date)){
						if (utils.ZipUtils.descomprimirEnZip("logs/backups/Auditable/"+ficheroLogs.replace("logs/", "")+".zip", ficheroLogs)){
							// Lo anotamos para despues borrarlo, y no dejar basura
							borrarAuditable.add(ficheroLogs);
						} else{
							error = true;
							play.Logger.error("Descompresión de 'logs/backups/Auditable/"+ficheroLogs.replace("logs/", "")+".zip' fallida o no existe el fichero");
						}
					}
					if (!error){
						if ((new File(ficheroLogs)).exists()){
							FileReader ficheroAuditable = new FileReader(ficheroLogs);
							brAuditable.add(new BufferedReader(ficheroAuditable));
							ficherosACerrar.add(ficheroAuditable);
						} else {
							play.Logger.error("Fichero '"+ficheroLogs+"' no existe. Imposible mostrarlo en la tabla de Logs");
						}
					}
				}
			} catch (FileNotFoundException e) {
				play.Logger.error(e,"Fichero de log Auditable no encontrado");
			}
			if (diaSiguiente(date).before(date2)){
				date = diaSiguiente(date);
			} else {
				seguirLeyendo=false;
			}
		}
		java.util.List<Log> rows = new ArrayList<Log>();
		for (int i=0; i<brDaily.size(); i++){
			String linea;
			try {
				while ((linea = brDaily.get(i).readLine()) != null) {
					rows.add(gson.fromJson(linea, Log.class));
					logsD++;
				}
			} catch (JsonSyntaxException e) {
				play.Logger.error(e,"Error de formato en el fichero de log Daily");
			} catch (IOException e) {
				play.Logger.error(e,"Error al leer el fichero de log Daily");
			}
		}
		for (int i=0; i<brAuditable.size(); i++){
			String linea;
			try {
				while ((linea = brAuditable.get(i).readLine()) != null) {
					rows.add(gson.fromJson(linea, Log.class)); 
					logsA++;
				}
			} catch (JsonSyntaxException e) {
				play.Logger.error(e,"Error de formato en el fichero de log Auditable");
			} catch (IOException e) {
				play.Logger.error(e,"Error al leer el fichero de log Auditable");
			}
		}
		List<Log> rowsFiltered = rows; //Tabla sin permisos, no filtra
		
//		for (int i = 0; i < rows.size(); i++) {
//			System.out.println(rows.get(i).time + "|" + rows.get(i).level + "|" + rows.get(i).message + "|" + rows.get(i).class_);
//		}
		
		// Se filtrarán las filas obtenidas antes según las búsquedas realizadas.
		String serialize = null;
		
//		if (todo != null) {
//			if (todo) {
				tables.TableRenderNoPermisos<Log> response = new tables.TableRenderNoPermisos<Log>(rowsFiltered);
				flexjson.JSONSerializer flex = new flexjson.JSONSerializer().include("rows.level", "rows.time", "rows.class_", "rows.user", "rows.message", "rows.trace").transform(new serializer.DateTimeTransformer(), org.joda.time.DateTime.class).exclude("*");
			    serialize = flex.serialize(response);
//			}
//		}
		
		// Antes de renderizar la página, eliminamos los ficheros descomprimidos, para no dejar basura, si los hay.
		for(int i=0; i<ficherosACerrar.size(); i++){
			ficherosACerrar.get(i).close();
		}
		for(int i=0; i<borrarDaily.size(); i++){
			File borrado = new File(borrarDaily.get(i));
			borrado.delete();
		}
		for(int i=0; i<borrarAuditable.size(); i++){
			File borrado = new File(borrarAuditable.get(i));
			borrado.delete();
		}
		renderJSON(serialize);
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
