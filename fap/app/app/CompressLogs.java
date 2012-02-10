/* Clase para que cada día, o al iniciar la aplicación, busque si hay ficheros de logs, no actuales, o sea
 * de día anteriores, y los comprime situandolos en las carpetas Daily y Auditable correspondientes dentro
 * de logs/backups.
 * De esta manera optimizamos el espacio usado por la aplicación, ya que estos ficheros de logs, puede ser
 * bastante pesados con aplicaciones que se usan constantemente.
 */

package app;

import java.io.File;

import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.jobs.On;
import properties.Properties;

// Ejecuta el doJob de la clase, cada día a las 12 de la noche y 50 minutos y al iniciar la aplicación
@On("0 50 12 * * ?")
@OnApplicationStart
public class CompressLogs extends Job {
	
	public void doJob() {
		// Preparamos una variable para gestionar el directorio de logs
		File directorioLogs = new File("logs");
		// Buscamos en dicho directorio ficheros cuyos nombres tengan fechas, que indicaran que son de dias anteriores
		for (File fichero : directorioLogs.listFiles()){
			// Si es un fichero antiguo, lo comprimimos, dependiendo del tipo que sea Daily o Auditable, para colocarlo en su carpeta
			// Para los de tipo Auditable
			if (fichero.getName().matches(".*Auditable\\.log\\.\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d$")){
				// Si se comprime bien, borramos el fichero
				if (!(new File("logs/backups/Auditable/"+fichero.getName()+".zip").exists()) && (utils.ZipUtils.comprimirEnZip(new String[]{"logs/"+fichero.getName()}, "logs/backups/Auditable/"+fichero.getName()+".zip"))){
					Logger.info("BackUps Logs: Fichero '"+fichero.getName()+"' comprimido en la carpeta <logs/backups/Auditable>");
					fichero.delete();
				}
				else
					Logger.error("BackUps Logs: Compresión de '"+fichero.getName()+"' fallida o ya existe el fichero comprimido en la carpeta <logs/backups/Auditable>");
			} else {
				// Para los de tipo Daily
				if (fichero.getName().matches(".*log\\.\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d$")){
					// Si se comprime bien, borramos el fichero
					if (!(new File("logs/backups/Daily/"+fichero.getName()+".zip").exists()) && (utils.ZipUtils.comprimirEnZip(new String[]{"logs/"+fichero.getName()}, "logs/backups/Daily/"+fichero.getName()+".zip"))){
						Logger.info("BackUps Logs: Fichero '"+fichero.getName()+"' comprimido en la carpeta <logs/backups/Daily>");
						fichero.delete();
					}
					else
						Logger.error("BackUps Logs: Compresión de '"+fichero.getName()+"' fallida o ya existe el fichero comprimido en la carpeta <logs/backups/Daily>");
				} 
			}
		}
		
	}
}
	
	
