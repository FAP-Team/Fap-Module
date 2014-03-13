package utils;

import play.mvc.Scope;
import play.mvc.Scope.Flash;

/**
 * Clase de Utils para los controladores.
 *
 */
public class ControllersUtils {
	/**
	 * Guarda una serie de mensajes para mostrar en el navegador a través de la flash
	 * 
	 * Se utiliza para mostrar info del estado y errores de los servicios web.
	 * Si son varios los mensajes, en lugar de sobreescribirlo crea nuevas claves
	 * para poder mostrarlos todos
	 * 
	 * @param mensaje 
	 */
	public static void flashServicesFail(String mensaje) {
		int contador = 0;
		Flash flash = Scope.Flash.current();
		if (flash.contains("servicesFailCounter")) {
			contador = 1+Integer.parseInt(flash.get("servicesFailCounter"));			
		}
		flash.put("servicesFail"+contador, mensaje);
		flash.put("servicesFailCounter", contador);
	}

}
