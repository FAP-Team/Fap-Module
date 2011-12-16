
package security;

import java.util.Map;

import models.*;
import controllers.fap.AgenteController;

public class SecureAppGen extends Secure {

	public SecureAppGen(Secure next) {
		super(next);
	}

	@Override
	public boolean check(String id, String action, Map<String, Long> ids, Map<String, Object> vars) {
		
		return nextCheck(id, action, ids, vars);
	}
	
	
}
