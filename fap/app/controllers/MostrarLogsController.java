package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import play.Play;
import play.data.binding.As;
import play.mvc.Util;
import properties.FapProperties;

import messages.Messages;
import models.BusquedaLogs;
import models.Log;
import controllers.gen.MostrarLogsControllerGen;

public class MostrarLogsController extends MostrarLogsControllerGen {
	
	@SuppressWarnings("deprecation")
	public static void index(String accion, BusquedaLogs busquedaLogs, int filas, String fechaLog) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/BuscarLogs/BuscarLogs.html");
		}

		Date date = null;
		// En caso de haber sido seleccionado la fecha como atributo se transforma a Date
		// para poder hacer las comparaciones posteriores.
		if (fechaLog != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				date = sdf.parse(fechaLog);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
  
		Date fecha_ = date;
		boolean porAtributos = busquedaLogs.buquedaPorAtributos;
		String tipo = busquedaLogs.tipoLog;
		String mensaje = busquedaLogs.mensajeLog;
		String usuario = busquedaLogs.usuario;
		String clase = busquedaLogs.claseLog;
		String month = null;
		String fechaLogBuscado = null;
		if (fecha_ != null) {
			int dia = fecha_.getDate();
			int mes = fecha_.getMonth();
			int año = fecha_.getYear();
			int day = (dia < 10) ? '0' + dia : dia;
			int m = mes + 1;
			NumberFormat formatter = new DecimalFormat("00");
			month = formatter.format(m);
			int year = (año < 1000) ? año + 1900 : año;
			fechaLogBuscado = day + "-" + month + "-" + year;
		}
		
		log.info("Visitando página: " + "fap/Admin/logsBusqueda.html");
		renderTemplate("fap/Admin/logsBusqueda.html", accion, filas, porAtributos, fechaLog, tipo, mensaje, usuario, clase, fechaLogBuscado);
	}
	
	public static void logs(long fecha1, long fecha2, int filas, boolean porAtributos, String fecha, String tipo, String mensaje, String usuario, String clase) throws IOException{

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
		
		Date fechaBusqueda = null;
		Date date = null;
		// Se transforma la fecha para poder hacer las comparaciones posteriores.
		if (!fecha.equals("")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				fechaBusqueda = sdf.parse(fecha);
				date = fechaBusqueda;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		// Si no se ha seleccionado fecha como atributo se coge la fecha de hoy para realizar la búsqueda.
		if (date == null)
			date = date1;
		
		String fileName, dirName = null;
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("app");
		File rutaLogs = null;
		
		// Se obtiene la ruta a partir de los appender.
		for (Enumeration<Appender> e = logger.getAllAppenders(); e.hasMoreElements();){
			Appender appender = e.nextElement();
			
			if (appender instanceof DailyRollingFileAppender){
				fileName = ((DailyRollingFileAppender)appender).getFile();
				int indexBackup = fileName.lastIndexOf("/");
			
				if (indexBackup == -1) {
					rutaLogs = Play.applicationPath;
				}
				else {
					dirName = fileName.substring(0, indexBackup);
					if ((dirName.matches("^[a-zA-Z]:.*")) || (dirName.startsWith("/"))) {
						rutaLogs = new File(dirName);
					} else {
						rutaLogs = new File(Play.applicationPath.getAbsolutePath() + "/" + dirName);
					}
				}
			}
		}
		
		while (seguirLeyendo){
			try {
				String ficheroLogs = nombreFichero(date, "Daily");
				String nameLogs = null;
				
				if (ficheroLogs != null){
					int indexBackup = ficheroLogs.lastIndexOf("/");
					
					if (indexBackup == -1) {
						nameLogs = ficheroLogs;
					}
					else {
						nameLogs = ficheroLogs.substring(indexBackup+1);
					}
					
					error = false;
					String rutaFichero = rutaLogs + "/" + nameLogs;
					
					// Si el fichero no es del día actual, lo recuperamos de los backups, descomprimiendolo
					if (!esHoy(date)){
						if (utils.ZipUtils.descomprimirEnZip(rutaLogs+"/backups/Daily/"+nameLogs+".zip", rutaFichero)){
							// Lo anotamos para despues borrarlo, y no dejar basura
							borrarDaily.add(rutaFichero);
						} else{
							error = true;
							play.Logger.error("Descompresión de '" + rutaLogs + "/backups/Daily/"+nameLogs+".zip' fallida o no existe el fichero");
						}
					}
					if (!error){
						if ((new File(rutaFichero)).exists()){
							FileReader ficheroDaily = new FileReader(rutaFichero);
							brDaily.add(new BufferedReader(ficheroDaily));
							ficherosACerrar.add(ficheroDaily);
						} else {
							play.Logger.error("Fichero '"+ rutaFichero +"' no existe. Imposible mostrarlo en la tabla de Logs");
						}
					}
				}
			} catch (FileNotFoundException e) {
				play.Logger.error(e,"Fichero de log del Daily no encontrado");
			}
			
			try {
				String ficheroLogs = nombreFichero(date, "Auditable");
				String nameLogs = null;

				if (ficheroLogs != null){
					int indexBackup = ficheroLogs.lastIndexOf("/");
					
					if (indexBackup == -1) {
						nameLogs = ficheroLogs;
					}
					else {
						nameLogs = ficheroLogs.substring(indexBackup+1);
					}
					
					error = false;
					String rutaFichero = rutaLogs + "/" + nameLogs;
					
					// Si el fichero no es del día actual, lo recuperamos de los backups, descomprimiendolo
					if (!esHoy(date)){
						if (utils.ZipUtils.descomprimirEnZip(rutaLogs+"/backups/Auditable/"+nameLogs+".zip", rutaFichero)){
							// Lo anotamos para despues borrarlo, y no dejar basura
							borrarAuditable.add(rutaFichero);
						} else{
							error = true;
							play.Logger.error("Descompresión de '" + rutaLogs +"/backups/Auditable/"+nameLogs+".zip' fallida o no existe el fichero");
						}
					}
					if (!error){
						if ((new File(rutaFichero)).exists()){
							FileReader ficheroAuditable = new FileReader(rutaFichero);
							brAuditable.add(new BufferedReader(ficheroAuditable));
							ficherosACerrar.add(ficheroAuditable);
						} else {
							play.Logger.error("Fichero '"+ rutaFichero +"' no existe. Imposible mostrarlo en la tabla de Logs");
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
		List<Log> searchedRows = new ArrayList<Log>();
		
		// Se filtrarán las filas obtenidas antes según las búsquedas realizadas.
		String serialize = null;
		
		if (!porAtributos) {
			tables.TableRenderNoPermisos<Log> response = null;
			if (filas != 0)
				response = new tables.TableRenderNoPermisos<Log>(rowsFiltered.subList(rows.size()-filas, rows.size()));
			else
				response = new tables.TableRenderNoPermisos<Log>(rowsFiltered);
			flexjson.JSONSerializer flex = new flexjson.JSONSerializer().include("rows.level", "rows.time", "rows.class_", "rows.user", "rows.message", "rows.trace").transform(new serializer.DateTimeTransformer(), org.joda.time.DateTime.class).exclude("*");
		    serialize = flex.serialize(response);
		    renderJSON(serialize);
		}
		
		if ((porAtributos) && (!fecha.equals(""))) {
			Date fechaToDate = null;
			for (Log log: rows) {
				String time_ = log.time.split(" ")[0];
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				try {
					fechaToDate = sdf.parse(time_);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if (fechaToDate.equals(fechaBusqueda)) {
					searchedRows.add(log);
				}
			}
		}
		if ((porAtributos) && (!tipo.equals(""))) {
			for (Log log: rows) {
				if (log.level.contains(tipo.toUpperCase())) {
					searchedRows.add(log);
				}
			}
		}
		if ((porAtributos) && (!mensaje.equals(""))) {
			for (Log log: rows) {
				if (log.message.contains(mensaje)) {
					searchedRows.add(log);
				}
			}
		}
		if ((porAtributos) && (!usuario.equals(""))) {
			for (Log log: rows) {
				if (log.user.contains(usuario)) {
					searchedRows.add(log);
				}
			}
		}
		if ((porAtributos) && (!clase.equals(""))) {
			for (Log log: rows) {
				if (log.class_.contains(clase)) {
					searchedRows.add(log);
				}
			}
		}
		
		tables.TableRenderNoPermisos<Log> response = null;
		if (filas != 0)
			response = new tables.TableRenderNoPermisos<Log>(searchedRows.subList(searchedRows.size()-filas, searchedRows.size()));
		else
			response = new tables.TableRenderNoPermisos<Log>(searchedRows);
		flexjson.JSONSerializer flex = new flexjson.JSONSerializer().include("rows.level", "rows.time", "rows.class_", "rows.user", "rows.message", "rows.trace").transform(new serializer.DateTimeTransformer(), org.joda.time.DateTime.class).exclude("*");
	    serialize = flex.serialize(response);
		
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
