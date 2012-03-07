package generator.utils

import es.fap.simpleled.led.*;
import generator.utils.HashStack.HashStackName;

import org.eclipse.emf.ecore.EObject

public class ModelUtils {

	public static String FORM_SOLICITUD = "Solicitud";
		
	/**
	* Devuelve el nombre del formulario actual
	* @return
	*/
   public static Formulario getActualContainer () {
	   return HashStack.top(HashStackName.FORMULARIO);
   }

	/**
	 * Indica si pertenece a el formulario por defecto de la solicitud
	 * FORM_SOLICITUD
	 * @return
	 */
	public static boolean isSolicitudForm () {
		Formulario form = getActualContainer();
		if ((form != null) && (form.name.equals(FORM_SOLICITUD)))
			return true;
		return false;
	}
	
	/**
	 * Indica si la entidad que se le pasa tiene check"Entity", con lo cual
	 * se le deberá realizar el "validate" 
	 * @param entity
	 * @return
	 */
	public static boolean isCheckEntity (EObject entity) {
		if ((entity != null) && (
			(entity instanceof Persona)
			|| (entity instanceof PersonaFisica)
			|| (entity instanceof PersonaJuridica)
			|| (entity instanceof Direccion)
			|| (entity instanceof Solicitante)
			))
			return true;
		return false;
	}
}
