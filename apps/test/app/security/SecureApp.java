package security;

import java.util.Map;

/**
 * Se debe implementar esta clase para extender los permisos que no
 * pueden definirse con el lenguaje FAP
 */
public final class SecureApp extends Secure {

	public SecureApp(Secure next) {
		super(next);
	}

	/**
	 * Para que los permisos puedan ser invocados por reflexión deben de
	 * ser implementados siguiendo las siguientes plantillas
	 *
	 * @SuppressWarnings("unused")
	 * private ResultadoPermiso [NombrePermiso](String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars)
	 *
	 * @SuppressWarnings("unused")
	 * private ResultadoPermiso [NombrePermiso]Accion(Map<String, Long> ids, Map<String, Object> vars)
	 */

}
