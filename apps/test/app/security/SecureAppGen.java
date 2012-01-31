
package security;

import java.util.Map;
import models.*;
import controllers.fap.AgenteController;
import java.util.ArrayList;
import java.util.List;

public class SecureAppGen extends Secure {

	public SecureAppGen(Secure next) {
		super(next);
	}

	@Override
	public boolean check(String id, String _permiso, String action, Map<String, Long> ids, Map<String, Object> vars) {
		
				 if("paginaTablaVer".equals(id))
					return paginaTablaVer(_permiso, action, ids, vars);
					
		return nextCheck(id, _permiso, action, ids, vars);
	}

	@Override
	public String accion(String id, String _permiso, Map<String, Long> ids, Map<String, Object> vars) {
				
		return nextAccion(id, _permiso, ids, vars);
	}
	
	
				
			private boolean paginaTablaVer (String _permiso, String accion, Map<String, Long> ids, Map<String, Object> vars){
				//Variables
				Agente agente = AgenteController.getAgente();
				
				Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
				
				if (!accion.toString().equals("crear".toString())){
					return checkIsEditableOrLess(_permiso);
				}
			
				return false;
			}
		
			
		

	
}
