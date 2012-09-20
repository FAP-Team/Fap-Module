package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import messages.Messages;
import models.TableKeyValue;
import controllers.gen.GraficaSolicitudesControllerGen;

public class GraficaSolicitudesController extends GraficaSolicitudesControllerGen {

	public static void index(String accion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/Graficas/GraficaSolicitudes.html");
		}


		Map<String, Integer> datos = new HashMap<String, Integer>();
		java.util.List<models.SolicitudGenerica> lista = models.SolicitudGenerica.findAll();
		
		for (models.SolicitudGenerica sol: lista) {
			if (datos.containsKey(sol.estado)) {
				Integer value = datos.get(sol.estado);
				datos.remove(sol.estado);
				datos.put(sol.estado, value + 1);
			} else {
				datos.put(sol.estado, 1);
			}
		}
		System.out.println("Datos: "+datos.toString());
		
		Iterator it = datos.entrySet().iterator();
		String jsData = "[";
		while (it.hasNext()) {
			Map.Entry e = (Map.Entry)it.next();
			jsData += "[ '" + TableKeyValue.getValue("estadosSolicitud" ,e.getKey().toString()) + "', "+ e.getValue()+"]";
			if (it.hasNext())
				jsData += ", ";
		}
		jsData += "]";
		
		System.out.println("JS: "+jsData);

		log.info("Visitando página: " + "fap/Graficas/GraficaSolicitudes.html");
		renderTemplate("fap/Graficas/GraficaSolicitudes.html", accion, jsData);
	}
	
}
