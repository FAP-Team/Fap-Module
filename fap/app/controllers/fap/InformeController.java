
package controllers.fap;

import play.*;
import play.modules.pdf.PDF;
import play.mvc.*;
import reports.Documentos;
import controllers.fap.*;
import tags.ReflectionUtils;
import validation.*;
import models.*;

import java.text.DateFormatSymbols;
import java.util.*;

import messages.Messages;

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
import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import emails.Mails;
import es.gobcan.eadmon.procedimientos.ws.dominio.AportadoPorEnum;


public class InformeController extends GenericController {

	public static void index(){
		Map<String, Integer> valores = new HashMap<String, Integer>();
		List<String> labels;
		
		
		getSolicitudesAportaciones(valores);		
		
		
		labels = new ArrayList<String>(valores.keySet());
		
		renderTemplate( "fap/Admin/informe.html", valores, labels);
	}
	
	
	@Util
	public static void getSolicitudesAportaciones(Map<String, Integer> arg) {
		List<SolicitudGenerica> solicitudes = SolicitudGenerica.find("SELECT solicitud FROM Solicitud solicitud WHERE solicitud.registro.fasesRegistro.registro = ?", true).fetch();

		String key = "Numero total de Solicitudes Registro Electrónico: ";
		int value = solicitudes != null ? solicitudes.size() : 0; 
		arg.put(key, value);
				
		for(SolicitudGenerica sol : solicitudes) {
			DateTime fecha = sol.registro.informacionRegistro.fechaRegistro;
			String mesSol = new DateFormatSymbols().getMonths()[fecha.getMonthOfYear()-1];
			int anioSol = fecha.getYear();

			key = "Solicitudes registro electrónico "+anioSol+":";
			value = arg.get(key) != null ? arg.get(key).intValue()+1 : 1; 
			arg.put(key, value);

			key = "Solicitudes registro electrónico "+mesSol+"-"+anioSol+":";
			value = arg.get(key) != null ? arg.get(key).intValue()+1 : 1; 
			arg.put(key, value);	
			
			for (Aportacion aportacion : sol.aportaciones.registradas) {
				for (Documento doc : aportacion.documentos){
					DateTime fechaDoc = doc.fechaRegistro;
					String mesDoc = new DateFormatSymbols().getMonths()[fecha.getMonthOfYear()-1];
					int anioDoc = fecha.getYear();

					key = "Documentación total aportada :";
					value = arg.get(key) != null ? arg.get(key).intValue()+1 : 1; 
					arg.put(key, value);
					
					key = "Documentación aportada "+anioDoc+":";
					value = arg.get(key) != null ? arg.get(key).intValue()+1 : 1; 
					arg.put(key, value);

					key = "Documentación aportada "+mesDoc+"-"+anioDoc+":";
					value = arg.get(key) != null ? arg.get(key).intValue()+1 : 1; 
					arg.put(key, value);	
				}
			}
		}

	}
	
	
	public static void exportarPdf(List<String> campos,List<Integer> valores){
		Documentos.enviarDocumentoSimple("reports/informe.html", "Informe", campos, valores);
	}
	
}
