
package security;

import java.util.Map;

public class SecureApp extends Secure {
	
	public SecureApp(Secure next) {
		super(next);
	}

	@Override
	public boolean check(String id, String action, Map<String, Long> ids, Map<String, Object> vars) {		
		return nextCheck(id, action, ids, vars);
	}
}