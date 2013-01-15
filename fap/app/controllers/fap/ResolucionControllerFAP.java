package controllers.fap;

import java.util.ArrayList;
import java.util.List;

import enumerado.fap.gen.ResolucionesDefinidasEnum;

import resolucion.ResolucionBase;
import resolucion.ResolucionParcial;
import resolucion.ResolucionMultipleTotal;
import tags.ComboItem;
import models.ResolucionFAP;

public class ResolucionControllerFAP extends InvokeClassController {

	/**
	 * Devolverá los posibles tipos de resolución que existan en la app.
	 * Para mostrar en el combo (Y seleccionarlos).
	 */
	public static List<ComboItem> getTiposResolucion() {
		return ComboItem.listFromTableOfTable("resolucionesDefinidas");
	}

	public static ResolucionBase getResolucionObject(Long idResolucion) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		/**
		 *  Según el tipo de la entidad resolución, deberíamos devolver un tipo u otro
		 *  de los que extiendan de ResoluciónBase.
		 */
		if (resolucion.tipoDefinidoResolucion.equals(ResolucionesDefinidasEnum.multipleTotal.name())) {
			return new ResolucionMultipleTotal(resolucion);
		} else if (resolucion.tipoDefinidoResolucion.equals(ResolucionesDefinidasEnum.multipleParcialExpedientes.name())) {
			return new ResolucionParcial(resolucion);
		} else if (resolucion.tipoDefinidoResolucion.equals(ResolucionesDefinidasEnum.simpleTotal.name())) {
			return new ResolucionParcial(resolucion);
		}
		return new ResolucionBase(resolucion);
	}
	
	/**
	 * Una vez se haya establecido el tipo de resolución,
	 * se inicializan los datos de la resolución.
	 * @param idResolucion
	 */
	public static void inicializaResolucion (Long idResolucion) {
		getResolucionObject(idResolucion).initResolucion();
	}
}
