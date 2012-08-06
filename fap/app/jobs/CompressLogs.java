/* Clase para que cada día, o al iniciar la aplicación, busque si hay ficheros de logs, no actuales, o sea
 * de día anteriores, y los comprime situandolos en las carpetas Daily y Auditable correspondientes dentro
 * de logs/backups.
 * De esta manera optimizamos el espacio usado por la aplicación, ya que estos ficheros de logs, puede ser
 * bastante pesados con aplicaciones que se usan constantemente.
 */

package jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;

import audit.Auditable;

import play.Logger;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.jobs.On;
import properties.FapProperties;
import properties.Properties;

// Ejecuta el doJob de la clase, cada día a las 12 de la noche y 50 minutos y al iniciar la aplicación
@On("0 50 0 * * ?")
@OnApplicationStart
public class CompressLogs extends Job {
	
	public void doJob() {
		play.Logger.info("Ejecutando el job CompressLog: "+Play.mode);
		if (!Play.mode.isProd())
			return;
		// Preparamos una variable para gestionar el directorio de logs
		String fileName, dirName = null;
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("app");
		for (Enumeration<Appender> e = logger.getAllAppenders(); e.hasMoreElements();){
			Appender appender = e.nextElement();
			File rutaLogs = null;
			
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
				play.Logger.info("Ruta Jobs: "+rutaLogs.getAbsolutePath());
				String ruta = rutaLogs.getAbsolutePath();
				//Comprobar que existen /logs/Auditable  y  /logs/Daily  y si no crearlos
				File logsAuditable = new File(ruta+"/Auditable");
				if (!(logsAuditable.exists())){
					logsAuditable.mkdir();
				}
				File logsDaily = new File(ruta+"/Daily");
				if (!(logsDaily.exists())){
					logsDaily.mkdir();
				}
				
				// Comprobar que existen /logs/backups, /logs/backups/Daily y /logs/backups/Auditable y si no se crean.
				File logsBackups = new File(ruta+"/backups");
				if (!(logsBackups.exists())){
					logsBackups.mkdir();
				}
				File logsBackupsDaily = new File(ruta+"/backups/Daily");
				if (!(logsBackupsDaily.exists())){
					logsBackupsDaily.mkdir();
				}
				File logsBackupsAuditable = new File(ruta+"/backups/Auditable");
				if (!(logsBackupsAuditable.exists())){
					logsBackupsAuditable.mkdir();
				}
						
				// Buscamos en dicho directorio ficheros cuyos nombres tengan fechas, que indicaran que son de dias anteriores
				for (File fichero : rutaLogs.listFiles()){
					// Si es un fichero antiguo, lo comprimimos, dependiendo del tipo que sea Daily o Auditable, para colocarlo en su carpeta
					// Para los de tipo Auditable
					if (fichero.getName().matches(".*Auditable\\.log\\.\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d$")){
						// Si se comprime bien, borramos el fichero
						if (!(new File(ruta+"/backups/Auditable/"+fichero.getName()+".zip").exists()) && (utils.ZipUtils.comprimirEnZip(new String[]{"/"+fichero.getName()}, "/backups/Auditable/"+fichero.getName()+".zip", rutaLogs))){
							Logger.info("BackUps Logs: Fichero '"+fichero.getName()+"' comprimido en la carpeta <backups/Auditable>");
							// Eliminamos el fichero si lo indica la property (es un backup)
							if (FapProperties.getBoolean("fap.deleteLogs.textoPlano"))
								fichero.delete();
						}
						else
							Logger.error("BackUps Logs: Compresión de '"+fichero.getName()+"' fallida o ya existe el fichero comprimido en la carpeta <backups/Auditable>");
					} else {
						// Para los de tipo Daily
						if (fichero.getName().matches(".*log\\.\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d$")){
							// Si se comprime bien, borramos el fichero
							if (!(new File(ruta+"/backups/Daily/"+fichero.getName()+".zip").exists()) && (utils.ZipUtils.comprimirEnZip(new String[]{"/"+fichero.getName()}, "/backups/Daily/"+fichero.getName()+".zip", rutaLogs))){
								Logger.info("BackUps Logs: Fichero '"+fichero.getName()+"' comprimido en la carpeta <backups/Daily>");
								// Eliminamos el fichero Daily si lo indica la property(es un backup)
								if (FapProperties.getBoolean("fap.deleteLogs.textoPlano"))
									fichero.delete();
							}
							else
								Logger.error("BackUps Logs: Compresión de '"+fichero.getName()+"' fallida o ya existe el fichero comprimido en la carpeta <backups/Daily>");
						} 
					}
				}
			}
		}
	}
}
	
	
