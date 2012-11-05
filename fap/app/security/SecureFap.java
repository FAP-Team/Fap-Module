
package security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import properties.FapProperties;

import verificacion.VerificacionUtils;

import models.Agente;
import models.AutorizacionesFAP;
import models.Busqueda;
import models.Documento;
import models.Participacion;
import models.SolicitudGenerica;
import controllers.SolicitudesController;
import controllers.fap.AgenteController;
import enumerado.fap.gen.EstadosVerificacionEnum;
import enumerado.fap.gen.TiposParticipacionEnum;

public class SecureFap extends Secure {
	
	public SecureFap(Secure next) {
		super(next);
	}

	@Override
	public ResultadoPermiso check(String id, String _permiso, String action, Map<String, Long> ids, Map<String, Object> vars) {	
		if ("hayNuevaDocumentacionVerificacion".equals(id))
			return hayNuevaDocumentacionVerificacion(_permiso, action, ids, vars);
		else if ("loginTipoUser".equals(id))
			return loginTipoUser(_permiso, action, ids, vars);
		else if ("listaSolicitudesConBusqueda".equals(id))
			return listaSolicitudesConBusqueda(_permiso, action, ids, vars);
		else if ("listaSolicitudesSinBusqueda".equals(id))
			return listaSolicitudesSinBusqueda(_permiso, action, ids, vars);
		else if ("mostrarResultadoBusqueda".equals(id))
			return mostrarResultadoBusqueda(_permiso, action, ids, vars);
		else if ("esFuncionarioHabilitadoYActivadaProperty".equals(id))
			return esFuncionarioHabilitadoYActivadaProperty(_permiso, action, ids, vars);
		
		return nextCheck(id, _permiso, action, ids, vars);
	}

	@Override
	public ResultadoPermiso accion(String id, Map<String, Long> ids, Map<String, Object> vars) {
		if ("hayNuevaDocumentacionVerificacion".equals(id))
			return hayNuevaDocumentacionVerificacionAccion(ids, vars);
		else if ("loginTipoUser".equals(id))
			return loginTipoUserAccion(ids, vars);
		else if ("listaSolicitudesConBusqueda".equals(id))
			return listaSolicitudesConBusquedaAccion(ids, vars);
		else if ("listaSolicitudesSinBusqueda".equals(id))
			return listaSolicitudesSinBusquedaAccion(ids, vars);
		else if ("mostrarResultadoBusqueda".equals(id))
			return mostrarResultadoBusquedaAccion(ids, vars);
		else if ("esFuncionarioHabilitadoYActivadaProperty".equals(id))
			return esFuncionarioHabilitadoYActivadaPropertyAccion(ids, vars);
		
		return nextAccion(id, ids, vars);
	}

	
	private ResultadoPermiso hayNuevaDocumentacionVerificacionAccion(Map<String, Long> ids, Map<String, Object> vars) {
		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);
		if (solicitud == null)
			return new ResultadoPermiso(Accion.Denegar);
		
		List<Documento> documentosNuevos = VerificacionUtils.existDocumentosNuevosVerificacionTipos(solicitud.verificacion, solicitud.verificaciones, solicitud.documentacion.documentos, solicitud.id);
		if ((documentosNuevos == null) || (documentosNuevos.isEmpty()) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.enVerificacionNuevosDoc.name())) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.iniciada.name())))
			return new ResultadoPermiso(Accion.Denegar);
		return new ResultadoPermiso(Accion.All);
	}
	
	private ResultadoPermiso hayNuevaDocumentacionVerificacion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);
		if (solicitud == null)
			return new ResultadoPermiso(Accion.Denegar);
		
		List<Documento> documentosNuevos = VerificacionUtils.existDocumentosNuevosVerificacionTipos(solicitud.verificacion, solicitud.verificaciones, solicitud.documentacion.documentos, solicitud.id);
		if ((documentosNuevos.isEmpty()) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.enVerificacionNuevosDoc.name())) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.iniciada.name())) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.enRequerimiento)) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.enRequerido)) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.enRequerimientoFirmaSolicitada)) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.verificacionNegativa)) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.verificacionPositiva))|| (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.plazoVencido)))
			return new ResultadoPermiso(Accion.Denegar);
		return new ResultadoPermiso(Accion.All);
	}
	
	public SolicitudGenerica getSolicitudGenerica(Map<String, Long> ids, Map<String, Object> vars) {
		if (vars != null && vars.containsKey("solicitud"))
			return (SolicitudGenerica) vars.get("solicitud");
		else if (ids != null && ids.containsKey("idSolicitud"))
			return SolicitudGenerica.findById(ids.get("idSolicitud"));
		return null;
	}
	
	private ResultadoPermiso loginTipoUser(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		Agente agente = AgenteController.getAgente();
		if ((FapProperties.getBoolean("fap.login.type.user")) && ((agente.acceso == null) || (!agente.acceso.toString().equals("certificado".toString())))) 
			return new ResultadoPermiso(Accion.All); 
		return new ResultadoPermiso(Accion.Denegar);
	}
	
	public ResultadoPermiso loginTipoUserAccion(Map<String, Long> ids, Map<String, Object> vars) {
		if (FapProperties.getBoolean("fap.login.type.user")) 
			return new ResultadoPermiso(Accion.All); 
		return new ResultadoPermiso(Accion.Denegar);
	}
	
	public ResultadoPermiso listaSolicitudesConBusqueda(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		Agente agente = AgenteController.getAgente();
		if (!agente.rolActivo.toString().equals("usuario".toString()) 
				&& FapProperties.getBoolean("fap.index.search")) {
			return new ResultadoPermiso(Accion.All);
		}
		return new ResultadoPermiso(Accion.Denegar);
	}

	public ResultadoPermiso listaSolicitudesConBusquedaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		Agente agente = AgenteController.getAgente();
		if (!agente.rolActivo.toString().equals("usuario".toString()) 
				&& FapProperties.getBoolean("fap.index.search")) {
			return new ResultadoPermiso(Accion.All);
		}
		return new ResultadoPermiso(Accion.Denegar);
	}

	public ResultadoPermiso listaSolicitudesSinBusqueda(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		Agente agente = AgenteController.getAgente();
		if (agente.rolActivo.toString().equals("usuario".toString())
				|| !FapProperties.getBoolean("fap.index.search")) {
			return new ResultadoPermiso(Accion.All);
		}
		return new ResultadoPermiso(Accion.Denegar);
	}

	public ResultadoPermiso listaSolicitudesSinBusquedaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		Agente agente = AgenteController.getAgente();
		if (agente.rolActivo.toString().equals("usuario".toString())
				|| !FapProperties.getBoolean("fap.index.search")) {
			return new ResultadoPermiso(Accion.All);
		}
		return new ResultadoPermiso(Accion.Denegar);
	}

	public ResultadoPermiso mostrarResultadoBusqueda(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		Busqueda busqueda = SolicitudesController.getBusqueda();
		if ( (busqueda.mostrarTabla != null) && (busqueda.mostrarTabla) ) 
			return new ResultadoPermiso(Accion.All); 
		return new ResultadoPermiso(Accion.Denegar);
	}

	public ResultadoPermiso mostrarResultadoBusquedaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		Busqueda busqueda = SolicitudesController. getBusqueda(); 
		if ( (busqueda.mostrarTabla != null) && (busqueda.mostrarTabla) )
			return new ResultadoPermiso(Accion.All); 
		return new ResultadoPermiso(Accion.Denegar);
	}
	
	private ResultadoPermiso esFuncionarioHabilitadoYActivadaProperty(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		if ((agente.funcionario.toString().equals("true".toString())) && (properties.FapProperties.getBoolean("fap.firmaYRegistro.funcionarioHabilitado"))) {
			return new ResultadoPermiso(Accion.All);
		}

		return null;
	}

	private ResultadoPermiso esFuncionarioHabilitadoYActivadaPropertyAccion(Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		if ((agente.funcionario.toString().equals("true".toString())) && (properties.FapProperties.getBoolean("fap.firmaYRegistro.funcionarioHabilitado")))
			return new ResultadoPermiso(Accion.Editar);

		return null;
	}

}