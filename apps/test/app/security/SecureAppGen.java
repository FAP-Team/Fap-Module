
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

		if("paginaTablaVer".equals(id))
			return paginaTablaVer(action, ids, vars);
		
		return nextCheck(id, action, ids, vars);
	}
	
		
	private boolean paginaTablaVer (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = action.toString().equals("crear".toString());
		return !resultado;
	}

}
