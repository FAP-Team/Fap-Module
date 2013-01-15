package controllers;

import models.Resolucion;
import resolucion.ResolucionApp;
import resolucion.ResolucionBase;
import controllers.fap.ResolucionControllerFAP;

public class ResolucionAppController extends ResolucionControllerFAP {

	public static ResolucionApp getResolucionObject(Long idResolucion) {
		Resolucion resolucion = Resolucion.findById(idResolucion);
		return new ResolucionApp(resolucion);
	}
	
}
