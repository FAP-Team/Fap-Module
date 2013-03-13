package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;

import com.google.gson.Gson;

import javassist.tools.reflect.Reflection;

import messages.Messages;
import models.ServicioWebInfo;

public class WSInfoController extends GenericController {
	
	/**
	 * Función que crea el Json con la información de cada servicio web.
	 */
	public static void getInfoAllWS() {
		try {
			List<ServicioWebInfo> listaInfoWS = WSController.invoke("getInfoWS", new Object[] {});	
			Gson gson = new Gson();
			String string_json = gson.toJson(listaInfoWS);
			renderJSON(string_json);
		} catch (Throwable e) {
			play.Logger.error("Hubo un problema al invocar el método getInfoWS: "+e.getMessage());
			Messages.error("Error al obtener las información de los servicios web");
		}
	}
	
}
