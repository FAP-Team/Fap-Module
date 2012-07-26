package controllers.fap;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import enumerado.fap.gen.EstadoNotificacionEnum;

import models.Agente;
import models.Interesado;
import models.Notificacion;
import models.TableKeyValue;
import models.TableKeyValueDependency;
import play.mvc.*;
import utils.DocumentosUtils;

public class UtilsController extends Controller {

    public static boolean documentoEsMultiple(String tipoUri) {
    	return DocumentosUtils.esTipoMultiple(tipoUri);
    }
    
    public static Integer getNuevasNotificaciones(String usuario){
    	List<Notificacion> notificaciones = Notificacion.find("select notificacion from Notificacion notificacion where notificacion.estado=?", EstadoNotificacionEnum.puestaadisposicion.name()).fetch();
    	List<Notificacion> misNotificaciones = new ArrayList<Notificacion>();
    	boolean esMiNotificacion=false;
    	if (notificaciones != null){
	    	for (Notificacion notificacion: notificaciones){
	    		for (Interesado interesado: notificacion.interesados){
	    			if ((interesado.persona.getNumeroId() != null) && (interesado.persona.getNumeroId().equals(usuario))){
	    				esMiNotificacion=true;
	    				break;
	    			}
	    		}
	    		if (esMiNotificacion){
	    			misNotificaciones.add(notificacion);
	    			esMiNotificacion=false;
	    		}
	    	}
    	} else {
    		return 0;
    	}
    	return misNotificaciones.size();
    }
    
    public static String filterDependency(String tabla, String dependencia){
    	if (!dependencia.isEmpty()){
	    	List<TableKeyValueDependency> tablaD = TableKeyValueDependency.find("select tkvd from TableKeyValueDependency tkvd where table=?", tabla).fetch(); 
			String js = "{";
			int tamLimite = tablaD.size();
			int contador=1;
			for (TableKeyValueDependency tkvd: tablaD){
				if (dependencia.equals(tkvd.dependency)){
					js += tkvd.key + ":" + tkvd.dependency;
					if(contador++ <= tamLimite){
						js += "%";
					}
				}
			}
			js += "}";
	    	return js;
    	}
    	return "{}";
    }
    
    public static String filterTKV(String tabla, String elementos) throws IOException{

    	elementos = elementos.replaceFirst("\\{", "");
    	elementos = elementos.replaceFirst("\\}", "");
    	String[] parseador = elementos.split(":.*?%");
    	List<String> mapaFilter = new ArrayList<String>();
    	for (String s : parseador) {
    	    mapaFilter.add(s);
    	}
    	if (!mapaFilter.isEmpty()){
    		Map<String, String> mapa = TableKeyValue.findByTableAsMap(tabla);
			String js = "{";
			int tamLimite = mapaFilter.size();
			int contador=1;
			for (String key: mapaFilter){
				if (mapa.containsKey(key)){
					js += key + ":" + mapa.get(key);
					if(contador++ <= tamLimite){
						js += "%";
					}
				}
			}
			js += "}";
	    	return js;
    	}
    	return "{}";
    }

}
