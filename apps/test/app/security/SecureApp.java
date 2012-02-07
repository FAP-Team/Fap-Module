
package security;

import java.util.Map;

public class SecureApp extends Secure {
	
	public SecureApp(Secure next) {
		super(next);
	}

	@Override
	public ResultadoPermiso check(String id, String grafico, String action, Map<String, Long> ids, Map<String, Object> vars) {		
		return nextCheck(id, grafico, action, ids, vars);
	}

	@Override
	public ResultadoPermiso accion(String id, Map<String, Long> ids, Map<String, Object> vars) {		
		return nextAccion(id, ids, vars);
	}
}
