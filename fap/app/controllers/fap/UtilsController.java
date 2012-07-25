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
	    	Map<String, String> mapa = TableKeyValueDependency.findByTableAsMap(tabla);
	    	System.out.println(mapa);
			Iterator<String> iterator = mapa.keySet().iterator();
			String js = "{";
			while(iterator.hasNext()){
				String key = iterator.next();
				if (dependencia.equals(mapa.get(key))){
					js += key + ":" + mapa.get(key);
					if(iterator.hasNext()){
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

    	Properties props = new Properties();
    	props.load(new StringReader(elementos.substring(1, elementos.length() - 1).replace("%", "\n")));       
    	List<String> mapaFilter = new ArrayList<String>();
    	for (Map.Entry<Object, Object> e : props.entrySet()) {
    	    mapaFilter.add((String)e.getKey());
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
