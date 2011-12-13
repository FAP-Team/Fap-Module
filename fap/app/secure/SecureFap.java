
package secure;

import java.util.Map;

public class SecureFap extends Secure {
	
	public SecureFap(Secure next) {
		super(next);
	}

	@Override
	public boolean check(String id, String action, Map<String, Long> ids, Map<String, Object> vars) {		
		return nextCheck(id, action, ids, vars);
	}
}