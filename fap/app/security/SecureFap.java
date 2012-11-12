
package security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import properties.FapProperties;

import verificacion.VerificacionUtils;

import models.Agente;
import models.AutorizacionesFAP;
import models.Busqueda;
import models.Documento;
import models.Participacion;
import models.PeticionCesiones;
import models.SolicitudGenerica;
import controllers.SolicitudesController;
import controllers.fap.AgenteController;
import enumerado.fap.gen.EstadosPeticionEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;
import enumerado.fap.gen.ListaCesionesEnum;
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
		else if ("ultimaEditable".equals(id))
			return ultimaEditable(_permiso, action, ids, vars);
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
		if ((documentosNuevos.isEmpty()) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.enVerificacionNuevosDoc.name())) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.iniciada.name())))
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
	
	private ResultadoPermiso ultimaEditable(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		List<PeticionCesiones> petCesiones = PeticionCesiones.findAll();
		DateTime atcT = new DateTime(1970-01-01), aeatT= new DateTime(1970-01-01);
		DateTime inssA008T= new DateTime(1970-01-01), inssR001T= new DateTime(1970-01-01);
		DateTime prueba= new DateTime(1970-01-01);
		
		if ((petCesiones != null) && (!petCesiones.isEmpty()))
		for (PeticionCesiones pC : petCesiones) {
			if(pC.tipo != null){
				if((pC.tipo.equals(ListaCesionesEnum.atc.name())) && (pC.fechaGen.isAfter(atcT))){
					atcT = pC.fechaGen; 
					System.out.println("ATCT: "+atcT);
				}
				if ((pC.tipo.equals(ListaCesionesEnum.aeat.name())) && (pC.fechaGen.isAfter(aeatT))){
					aeatT = pC.fechaGen; 
				}
				if ((pC.tipo.equals(ListaCesionesEnum.inssA008.name())) && (pC.fechaGen.isAfter(inssA008T))){
					inssA008T = pC.fechaGen;
				}
				if ((pC.tipo.equals(ListaCesionesEnum.inssR001.name())) && (pC.fechaGen.isAfter(inssR001T))){
					inssR001T = pC.fechaGen; 
				}
			}
		}

		PeticionCesiones actual = getPeticionCesiones(ids, vars);
		if ((actual != null) && (actual.estado != null)){
			if ((actual.estado.equals(EstadosPeticionEnum.sinTipo.name()))){
				return new ResultadoPermiso(Accion.All);
			}	
			if ((actual.tipo.equals(ListaCesionesEnum.atc.name())) && (actual.fechaGen.equals(atcT))){
				return new ResultadoPermiso(Accion.All);
			}
			if ((actual.tipo.equals(ListaCesionesEnum.aeat.name())) && (actual.fechaGen.equals(aeatT))){
				return new ResultadoPermiso(Accion.All);
			}
			if ((actual.tipo.equals(ListaCesionesEnum.inssA008.name())) && (actual.fechaGen.equals(inssA008T))){
				return new ResultadoPermiso(Accion.All);
			}
			if ((actual.tipo.equals(ListaCesionesEnum.inssR001.name())) && (actual.fechaGen.equals(inssR001T))){
				return new ResultadoPermiso(Accion.All);
			}
		}
		return new ResultadoPermiso(Accion.Leer);
	}
	
	public PeticionCesiones getPeticionCesiones(Map<String, Long> ids, Map<String, Object> vars) {
		if (vars != null && vars.containsKey("peticionCesiones"))
			return (PeticionCesiones) vars.get("peticionCesiones");
		else if (ids != null && ids.containsKey("idPeticionCesiones"))
			return PeticionCesiones.findById(ids.get("idPeticionCesiones"));
		return null;
	}
}
