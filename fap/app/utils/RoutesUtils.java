package utils;

import java.util.HashMap;
import java.util.Map;

import play.Play;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Router;
import play.mvc.Router.ActionDefinition;
import properties.FapProperties;

public class RoutesUtils {

    public static String getDefaultRoute(){
    	String httpPath = Play.configuration.getProperty("http.path", null);
    	if (httpPath != null)
    		return httpPath + "/";
    	else
    		return "/";
    }
    
    public static String getPrimeraPaginaSolicitud(Long idSolicitud){
    	String action = FapProperties.get("fap.app.firstPage") + "Controller.index";
    	Map<String, Object> params = new HashMap<String, Object>();
    	params.put("accion", "editar");
    	params.put("idSolicitud", idSolicitud);
    	ActionDefinition reverse = Router.reverse(action, params);
    	return reverse.url;
    }
    
    
    /**
     * Comprueba si la petición actual es a la página de inicio
     * @return
     */
    public static boolean isDefaultRoute(){
    	String defaultRoute = getDefaultRoute();
    	return isRoute(defaultRoute);
    }
	
    /**
     * Comprueba si la petición actual es a una ruta en concreto
     * @param path
     * @return
     */
    public static boolean isRoute(String path){
    	Request request = Http.Request.current();
    	return request.path.equals(path);
    }
    
}
