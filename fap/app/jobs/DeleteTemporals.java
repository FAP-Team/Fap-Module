package jobs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.AdministracionFapJobs;

import org.joda.time.DateTime;

import play.jobs.*;
import properties.FapProperties;


/**
 * Job que elimina los documentos temporales con más de DeleteTemporals.HOURS horas de antelación
 * @author Jorge Carballo
 *
 */
@Every("5h")
public class DeleteTemporals extends Job {
	
    public void doJob() {
    	List <AdministracionFapJobs> jobs = AdministracionFapJobs.findAll();
		if ((jobs.isEmpty()) || (jobs.get(0).eliminarTemporales == null) || (jobs.get(0).eliminarTemporales)){
	    	String borrar = FapProperties.get("fap.delete.temporals");
	    	if (borrar.equals("true"))
	    		deleteReports();
		}
    }
    
    private static void deleteReports () {
    	try {
    		File tmp = File.createTempFile("report_", ".pdf");
    		File dir = new File(tmp.getParent());
    		ExtFilter eF = new ExtFilter("pdf");
    		File[] files = dir.listFiles(eF);
    		String toDelete=FapProperties.get("fap.delete.temporals.old");
    		for (File f: files) {
    			long time = f.lastModified();
    			if (getTimePDF(time).getTime().before(getTimetoDelete(toDelete).getTime())){ // Si el PDF temporal tiene una fecha de creacion anterior al limite establecido en la property (FechaActual-LaProperty)
    				play.Logger.info("Borrando fichero temporal PDF: "+f.getAbsolutePath());
    				if (!f.delete())
    					play.Logger.error("Fallo intentando borrar el fichero temporal: "+f.getAbsolutePath());
    			}
    		}
    		tmp.delete(); // Borramos el fichero temporal creado para conocer el PATH de donde el sistema guarda los temporales
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    private static Calendar getTimePDF(long time){
    	Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(time);
    	return cal;
    }
    
    private static Calendar getTimetoDelete(String time){
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(new Date());
    	Pattern expReg = Pattern.compile("^\\d\\d?[dhm]$");
    	Matcher cazo = expReg.matcher(time);
    	if (cazo.find()){
    		int timeDelete = 0;
    		char typeTime = ' ';
    		if (time.length() == 3){ // Si es formato XX[hdm], es decir 2 digitos
    			timeDelete = (Integer.parseInt(String.valueOf(time.charAt(0)))*10) + Integer.parseInt(String.valueOf(time.charAt(1)));
    			typeTime = time.charAt(2);
    		} else { // Si tiene el formato X[hdm], es decir de 1 digito
    			timeDelete = Integer.parseInt(String.valueOf(time.charAt(0)));
    			typeTime = time.charAt(1);
    		}
    		switch (typeTime){
    			case 'd' :  cal.add(Calendar.DATE, -timeDelete);
    				        break;
    			case 'h' :  cal.add(Calendar.HOUR, -timeDelete);
    				        break;
    			case 'm' :  cal.add(Calendar.MINUTE, -timeDelete);
    				        break;
    		    default  :  return null;
    		}
    	} else {
    		play.Logger.error("Property fap.delete.temporals.old, está mal descrita. Recuerde Xd ó Xh ó Xm, con X de 1 a 2 digitos");
    		return null;
    	}
    	return cal;
    }
    
}