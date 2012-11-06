package security;

import java.util.Map;

import javax.persistence.Query;

import models.Agente;
import models.Singleton;
import models.Solicitud;
import play.db.jpa.JPA;
import controllers.fap.AgenteController;

import utils.DateTimeUtils;

public class SecureApp extends Secure {

	public SecureApp(Secure next) {
		super(next);
	}

	@Override
	public ResultadoPermiso check(String id, String grafico, String action, Map<String, Long> ids, Map<String, Object> vars) {
		if ("mensajeFirmarRegistrarSolicitud".equals(id))
			return mensajeFirmarRegistrarSolicitud(grafico, action, ids, vars);
		return nextCheck(id, grafico, action, ids, vars);
	}

	@Override
	public ResultadoPermiso accion(String id, Map<String, Long> ids, Map<String, Object> vars) {
		return nextAccion(id, ids, vars);
	}
	
	private ResultadoPermiso mensajeFirmarRegistrarSolicitud(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		if (!DateTimeUtils.cumplidaFechaCierreSolicitud()) {
			return new ResultadoPermiso(Grafico.Editable);
		}
		return null;
	}

}
