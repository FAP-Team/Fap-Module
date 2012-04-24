
package security;

import java.util.List;
import java.util.Map;

import verificacion.VerificacionUtils;

import models.Agente;
import models.Documento;
import models.Singleton;
import models.SolicitudGenerica;
import controllers.fap.AgenteController;
import controllers.fap.VerificacionFapController;
import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;

public class SecureFap extends Secure {
	
	public SecureFap(Secure next) {
		super(next);
	}

	@Override
	public boolean check(String id, String action, Map<String, Long> ids, Map<String, Object> vars) {		
		if("hayNuevaDocumentacionVerificacion".equals(id))
			return hayNuevaDocumentacionVerificacion(action, ids, vars);
		return nextCheck(id, action, ids, vars);
	}
	
	public boolean hayNuevaDocumentacionVerificacion (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			try {
				solicitud = (SolicitudGenerica) SolicitudGenerica.class.getMethod("get", Class.class).invoke(null, SolicitudGenerica.class);
			} catch (Exception e) {}
		}
		if (solicitud == null)
			return false;

		List<Documento> documentosNuevos = VerificacionUtils.existDocumentosNuevosVerificacionTipos(solicitud.verificacion, solicitud.verificaciones, solicitud.documentacion.documentos);
		if ((documentosNuevos.isEmpty()) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.enVerificacionNuevosDoc.name())) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.iniciada.name())))
    		return false;
		return true;
	}
}