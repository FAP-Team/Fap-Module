
package controllers.fap;

import play.*;
import play.mvc.*;
import properties.FapProperties;
import controllers.fap.*;
import tags.ReflectionUtils;
import validation.*;
import models.*;

import java.text.SimpleDateFormat;
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


	public static void logs(long fecha1, long fecha2) throws IOException{
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
		
		String fileName, dirName = null;
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("app");
		File rutaLogs = null;
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
		tables.TableRenderNoPermisos<Log> response = new tables.TableRenderNoPermisos<Log>(rowsFiltered);

		flexjson.JSONSerializer flex = new flexjson.JSONSerializer().include("rows.level", "rows.time", "rows.class_", "rows.user", "rows.message", "rows.trace").transform(new serializer.DateTimeTransformer(), org.joda.time.DateTime.class).exclude("*");
		String serialize = flex.serialize(response);
		
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
