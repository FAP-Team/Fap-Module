package generator.utils

import es.fap.simpleled.led.*;
import generator.utils.HashStack.HashStackName;

import org.eclipse.emf.ecore.EObject

public class ModelUtils {

	/**
	* Devuelve el nombre del formulario actual
	* @return
	*/
   public static Formulario getActualContainer () {
	   return HashStack.top(HashStackName.FORMULARIO);
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
			))
			return true;
		return false;
	}
}
