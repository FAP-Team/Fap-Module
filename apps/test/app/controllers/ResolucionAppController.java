package controllers;

import models.ResolucionFAP;
import resolucion.ResolucionBase;
import resolucion.ResolucionMultipleTotal;
import controllers.fap.ResolucionControllerFAP;

public class ResolucionAppController extends ResolucionControllerFAP {

	public static ResolucionBase getResolucionObject(Long idResolucion) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		return new ResolucionMultipleTotal(resolucion);
	}
	
}
