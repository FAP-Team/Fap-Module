/* Clase para que cada día, o al iniciar la aplicación, busque si hay ficheros de logs, no actuales, o sea
 * de día anteriores, y los comprime situandolos en las carpetas Daily y Auditable correspondientes dentro
 * de logs/backups.
 * De esta manera optimizamos el espacio usado por la aplicación, ya que estos ficheros de logs, puede ser
 * bastante pesados con aplicaciones que se usan constantemente.
 */

package jobs;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.AdministracionFapJobs;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;

import audit.Auditable;

import play.Logger;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.jobs.On;
import play.libs.Files;
import properties.FapProperties;
import properties.Properties;

@On("0 30 23 * * ?")
public class ExtraBackupLogs extends Job {
		
	public void doJob() {
		if (FapProperties.getBoolean("fap.log.copy.extra")){
			play.Logger.info("Ejecutando la copia extra de logs.");
			if (!Play.mode.isProd())
				return;
			// Preparamos una variable para gestionar el directorio de logs
			String fileName, dirName = null;
			org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("app");
			for (Enumeration<Appender> e = logger.getAllAppenders(); e.hasMoreElements();){
				Appender appender = e.nextElement();
				File rutaLogs = null;
				
				/// Si es un DailyRollingFileAppender debemos copiarlo
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
	
				if (rutaLogs != null) {
					play.Logger.info("Copy Extra Ruta Jobs: "+rutaLogs.getAbsolutePath());
					String ruta = rutaLogs.getAbsolutePath();
					
					// Comprobar que existen /logs/backups/BackupExtra, y si no se crean.
					File logsBackups = new File(ruta+"/backups");
					if (!(logsBackups.exists())) {
						logsBackups.mkdir();
					}
					File logsBackupsDaily = new File(ruta+"/backups/BackupExtra");
					if (!(logsBackupsDaily.exists())) {
						logsBackupsDaily.mkdir();
					}
					
					Pattern fileLogs = Pattern.compile("(.*)\\.log$");
					Matcher matcher;
					
					// Calculo el día anterior a hoy
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					
					// Buscamos en dicho directorio ficheros cuyos nombres NO tengan fechas
					for (File fichero : rutaLogs.listFiles()) {
						matcher = fileLogs.matcher(fichero.getName());
						if (matcher.matches()){
							String fileNewName = matcher.group(1)+dateFormat.format(new Date())+".log";
							if (!(new File(ruta+"/backups/BackupExtra/"+fileNewName).exists())){
								File file = new File(ruta+"/backups/BackupExtra/"+fileNewName);
								Files.copy(fichero, file);
								play.Logger.info("BackUps Extra Logs: Fichero '"+fichero.getName()+"' copiado en la carpeta <backups/BackupExtra>");
							}
							else
								play.Logger.error("BackUps Extra Logs: El fichero ya existe '"+fileNewName);
						} 
					}
				}
			}
		}
	}
}
	
	
