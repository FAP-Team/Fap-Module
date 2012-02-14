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
	public ResultadoPermiso check(String id, String grafico, String action,
			Map<String, Long> ids, Map<String, Object> vars) {

		if ("paginaTablaVer".equals(id))
			return paginaTablaVer(grafico, action, ids, vars);

		return nextCheck(id, grafico, action, ids, vars);
	}

	@Override
	public ResultadoPermiso accion(String id, Map<String, Long> ids,
			Map<String, Object> vars) {

		if ("paginaTablaVer".equals(id))
			return paginaTablaVerAccion(ids, vars);

		return nextAccion(id, ids, vars);
	}

	private ResultadoPermiso paginaTablaVer(String grafico, String accion,
			Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		Secure secure = config.InjectorConfig.getInjector().getInstance(
				security.Secure.class);

		if (!accion.toString().equals("crear".toString())) {
			return new ResultadoPermiso(Grafico.Editable);

		}

		return null;
	}

	private ResultadoPermiso paginaTablaVerAccion(Map<String, Long> ids,
			Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		Secure secure = config.InjectorConfig.getInjector().getInstance(
				security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if (!accion.toString().equals("crear".toString()))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}

}
