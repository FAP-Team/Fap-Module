
package controllers.fap;

import play.*;
import play.mvc.*;
import controllers.fap.*;
import tags.ReflectionUtils;
import validation.*;
import models.*;
import java.util.*;

import messages.Messages;
import messages.Messages.MessageType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.config.PropertyGetter.PropertyCallback;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import emails.Mails;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;


public class LoggerController extends GenericController {

	private static Logger log = Logger.getLogger("Paginas");
	
	public static void index(){
		log.info("Visitando página: fap/Admin/logs.html");
		renderTemplate("fap/Admin/logs.html");
	}


	public static void logs(long fecha) throws IOException{
		Date date = new Date(fecha);
		int logsD=0, logsA=0;
		String borrarDaily=null, borrarAuditable=null;
		FileReader fileAuditable=null, fileDaily=null;
		Gson gson = new Gson();
		BufferedReader brDaily = null, brAuditable = null;
		try {
			String ficheroLogs = nombreFichero(date, "Daily");
			if (ficheroLogs != null){
				// Si el fichero no es del día actual, lo recuperamos de los backups, descomprimiendolo
				if (!esHoy(date)){
					if (utils.ZipUtils.descomprimirEnZip("logs/backups/Daily/"+ficheroLogs.replace("logs/", "")+".zip", ficheroLogs)){
						// Lo anotamos para despues borrarlo, y no dejar basura
						borrarDaily=ficheroLogs;
					} else{
						play.Logger.error("Descompresión de 'logs/backups/Daily/"+ficheroLogs.replace("logs/", "")+".zip' fallida o no existe el fichero");
					}
				}
				if ((new File(ficheroLogs)).exists()){
					fileDaily = new FileReader(ficheroLogs);
					brDaily = new BufferedReader(fileDaily);
				} else {
					play.Logger.error("Fichero '"+ficheroLogs+"' no existe. Imposible mostrarlo en la tabla de Logs");
				}
			}
		} catch (FileNotFoundException e) {
			play.Logger.error(e,"Fichero de log del Daily no encontrado");
		}
		
		try {
			String ficheroLogs = nombreFichero(date, "Auditable");
			if (ficheroLogs != null){
				// Si el fichero no es del día actual, lo recuperamos de los backups, descomprimiendolo
				if (!esHoy(date)){
					if (utils.ZipUtils.descomprimirEnZip("logs/backups/Auditable/"+ficheroLogs.replace("logs/", "")+".zip", ficheroLogs)){
						// Lo anotamos para despues borrarlo, y no dejar basura
						borrarAuditable=ficheroLogs;
					} else{
						play.Logger.error("Descompresión de 'logs/backups/Auditable/"+ficheroLogs.replace("logs/", "")+".zip' fallida o no existe el fichero");
					}
				}
				if ((new File(ficheroLogs)).exists()){
					fileAuditable = new FileReader(ficheroLogs);
					brAuditable = new BufferedReader(fileAuditable);
				} else {
					play.Logger.error("Fichero '"+ficheroLogs+"' no existe. Imposible mostrarlo en la tabla de Logs");
				}
			}
		} catch (FileNotFoundException e) {
			play.Logger.error(e,"Fichero de log Auditable no encontrado");
		}
		
		java.util.List<Log> rows = new ArrayList<Log>();

		if (brDaily != null){
			String linea;
			try {
				while ((linea = brDaily.readLine()) != null) {
					rows.add(gson.fromJson(linea, Log.class));
					logsD++;
				}
			} catch (JsonSyntaxException e) {
				play.Logger.error(e,"Error de formato en el fichero de log Daily");
			} catch (IOException e) {
				play.Logger.error(e,"Error al leer el fichero de log Daily");
			}
		}
		if (brAuditable != null) {
			String linea;
			try {
				while ((linea = brAuditable.readLine()) != null) {
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
		tables.TableRenderNoPermisos<Log> response = new tables.TableRenderNoPermisos<Log>(rowsFiltered);

		flexjson.JSONSerializer flex = new flexjson.JSONSerializer().include("rows.level", "rows.time", "rows.class_", "rows.user", "rows.message", "rows.trace").transform(new serializer.DateTimeTransformer(), org.joda.time.DateTime.class).exclude("*");
		String serialize = flex.serialize(response);
		// Antes de renderizar la página, eliminamos los ficheros descomprimidos, para no dejar basura, si los hay.
		if (borrarDaily != null){
			// Cerramos los FileReader usados, antes de borrar, si no daria fallo.
			fileDaily.close();
			File borrado = new File(borrarDaily);
			borrado.delete();
		}
		if (borrarAuditable!= null){
			fileAuditable.close();
			File borrado = new File(borrarAuditable);
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
	
}
