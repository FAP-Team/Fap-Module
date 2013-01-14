package controllers.fap;

import resolucion.ResolucionBase;
import models.Resolucion;

public class ResolucionControllerFAP extends InvokeClassController {

	public static void getTipoResolucion() {
		
	}
	
	public static ResolucionBase getResolucionObject(Long idResolucion) {
		Resolucion resolucion = Resolucion.findById(idResolucion);
		return new ResolucionBase(resolucion);
	}
}
