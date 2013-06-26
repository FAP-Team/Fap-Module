
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
import models.Registro;
import models.RegistroModificacion;

import models.SolicitudGenerica;
import models.Verificacion;
import controllers.SolicitudesController;
import controllers.fap.AgenteController;
import enumerado.fap.gen.AccesoAgenteEnum;
import enumerado.fap.gen.EstadosModificacionEnum;
import enumerado.fap.gen.EstadosPeticionEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;
import enumerado.fap.gen.ListaCesionesEnum;
import enumerado.fap.gen.ListaEstadosEnum;
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
		else if ("verificarObtenerNoProcede".equals(id))
			return verificarObtenerNoProcede(_permiso, action, ids, vars);
		else if ("prepararSolicitudModificacion".equals(id))
			return prepararSolicitudModificacion(_permiso, action, ids, vars);
		else if ("enBorradorSolicitudModificada".equals(id))
			return enBorradorSolicitudModificada(_permiso, action, ids, vars);
		else if ("mensajeIntermedioSolicitudModificada".equals(id))
			return mensajeIntermedioSolicitudModificada(_permiso, action, ids, vars);
		else if ("habilitarFHPresentacionModificada".equals(id))
			return habilitarFHPresentacionModificada(_permiso, action, ids, vars);
		else if ("firmarRegistrarSolicitudModificadaFH".equals(id))
			return firmarRegistrarSolicitudModificadaFH(_permiso, action, ids, vars);
		else if ("firmarRegistrarSolicitudModificada".equals(id))
			return firmarRegistrarSolicitudModificada(_permiso, action, ids, vars);
		else if ("firmarSolicitudModificada".equals(id))
			return firmarSolicitudModificada(_permiso, action, ids, vars);
		else if ("registrarSolicitudModificada".equals(id))
			return registrarSolicitudModificada(_permiso, action, ids, vars);
		else if ("modificarSolicitudModificada".equals(id))
			return modificarSolicitudModificada(_permiso, action, ids, vars);
		else if ("modificacionTrasPresentacionDeSolicitud".equals(id))
			return modificacionTrasPresentacionDeSolicitud(_permiso, action, ids, vars);
		else if ("menuConModificacion".equals(id))
			return menuConModificacion(_permiso, action, ids, vars);
		
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
		else if ("prepararSolicitudModificacion".equals(id))
			return prepararSolicitudModificacionAccion(ids, vars);
		else if ("enBorradorSolicitudModificada".equals(id))
			return enBorradorSolicitudModificadaAccion(ids, vars);
		else if ("mensajeIntermedioSolicitudModificada".equals(id))
			return mensajeIntermedioSolicitudModificadaAccion(ids, vars);
		else if ("clasificadaSolicitudModificada".equals(id))
			return clasificadaSolicitudModificadaAccion(ids, vars);
		else if ("habilitarFHPresentacionModificada".equals(id))
			return habilitarFHPresentacionModificadaAccion(ids, vars);
		else if ("firmarRegistrarSolicitudModificadaFH".equals(id))
			return firmarRegistrarSolicitudModificadaFHAccion(ids, vars);
		else if ("firmarRegistrarSolicitudModificada".equals(id))
			return firmarRegistrarSolicitudModificadaAccion(ids, vars);
		else if ("firmarSolicitudModificada".equals(id))
			return firmarSolicitudModificadaAccion(ids, vars);
		else if ("registrarSolicitudModificada".equals(id))
			return registrarSolicitudModificadaAccion(ids, vars);
		else if ("modificarSolicitudModificada".equals(id))
			return modificarSolicitudModificadaAccion(ids, vars);
		else if ("modificacionTrasPresentacionDeSolicitud".equals(id))
			return modificacionTrasPresentacionDeSolicitudAccion(ids, vars);
		
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
		if ((FapProperties.getBoolean("fap.login.type.user")) && ((agente.acceso == null) || (!agente.acceso.equals(AccesoAgenteEnum.certificado.name())))) 
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
	
	private ResultadoPermiso prepararSolicitudModificacion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("editar".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.borrador.toString().equals("false".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Grafico.Editable);

		}

		if ((accion.toString().equals("editar".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.borrador.toString().equals("true".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Grafico.Visible);

		}

		return null;
	}

	private ResultadoPermiso prepararSolicitudModificacionAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("editar".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.borrador.toString().equals("false".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("editar".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.borrador.toString().equals("true".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}
	
	private ResultadoPermiso enBorradorSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString())) || ((registro != null && registro.fasesRegistro != null && registro.fasesRegistro.borrador.toString().equals("true".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString())))) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}

	private ResultadoPermiso enBorradorSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;
		
		
		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("leer".toString())) || ((registro != null && registro.fasesRegistro != null && registro.fasesRegistro.borrador.toString().equals("true".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}
	
	private ResultadoPermiso mensajeIntermedioSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((registro != null && registro.fasesRegistro != null && registro.fasesRegistro.firmada.toString().equals("true".toString()) || registro != null && registro.fasesRegistro != null && registro.fasesRegistro.registro.toString().equals("true".toString()) || registro != null && registro.fasesRegistro != null && registro.fasesRegistro.expedienteAed.toString().equals("true".toString())) && registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString())) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}

	private ResultadoPermiso mensajeIntermedioSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		if ((registro != null && registro.fasesRegistro != null && registro.fasesRegistro.firmada.toString().equals("true".toString()) || registro != null && registro.fasesRegistro != null && registro.fasesRegistro.registro.toString().equals("true".toString()) || registro != null && registro.fasesRegistro != null && registro.fasesRegistro.expedienteAed.toString().equals("true".toString())) && registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))
			return new ResultadoPermiso(Accion.Editar);

		return null;
	}
	
//	private ResultadoPermiso clasificadaSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
//		//Variables
//		Agente agente = AgenteController.getAgente();
//
//		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);
//
//		Registro registro=null;
//		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
//			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
//		else
//			return null;
//
//		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
//
//		if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("true".toString()))) {
//			return new ResultadoPermiso(Accion.All);
//
//		}
//
//		return null;
//	}

	private ResultadoPermiso clasificadaSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("true".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}

	private ResultadoPermiso habilitarFHPresentacionModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((registro != null && registro.habilitaFuncionario.toString().equals("true".toString()))) {
			return new ResultadoPermiso(Grafico.Visible);

		}

		if ((registro != null && registro.habilitaFuncionario == null) || (registro != null && registro.habilitaFuncionario.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}

	private ResultadoPermiso habilitarFHPresentacionModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		if ((registro != null && registro.habilitaFuncionario.toString().equals("true".toString())))
			return new ResultadoPermiso(Accion.Editar);

		if ((registro != null && registro.habilitaFuncionario == null) || (registro != null && registro.habilitaFuncionario.toString().equals("false".toString())))
			return new ResultadoPermiso(Accion.Editar);

		return null;
	}
	
	private ResultadoPermiso firmarRegistrarSolicitudModificadaFH(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if (((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) && (agente.funcionario.toString().equals("true".toString()))) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}

	private ResultadoPermiso firmarRegistrarSolicitudModificadaFHAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if (((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) && (agente.funcionario.toString().equals("true".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}
	
	private ResultadoPermiso firmarRegistrarSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}

	private ResultadoPermiso firmarRegistrarSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}
	
	private ResultadoPermiso firmarSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.firmada.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}

	private ResultadoPermiso firmarSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.firmada.toString().equals("false".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}
	
	private ResultadoPermiso registrarSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.firmada.toString().equals("true".toString()) && registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}

	private ResultadoPermiso registrarSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.firmada.toString().equals("true".toString()) && registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}
	
	private ResultadoPermiso modificarSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.registro.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}

	private ResultadoPermiso modificarSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.registro.toString().equals("false".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}
	
	private ResultadoPermiso modificacionTrasPresentacionDeSolicitud(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor")) {
			return new ResultadoPermiso(Accion.All);

		}

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && !solicitud.estado.toString().equals("borrador".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("false".toString())) {
			return new ResultadoPermiso(Grafico.Visible);

		}

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && solicitud.estado.toString().equals("modificacion".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("true".toString()) && registro != null && registro.fasesRegistro.borrador.toString().equals("true".toString())) {
			return new ResultadoPermiso(Grafico.Visible);

		}
		
		
		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && solicitud.estado.toString().equals("modificacion".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("true".toString()) && registro != null && registro.fasesRegistro.borrador.toString().equals("false".toString()) && solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).getEstado().equals("Expirada".toString())) {
			if (!accion.equals("crear"))
				return new ResultadoPermiso(Grafico.Visible);

		}

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && solicitud.estado.toString().equals("modificacion".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("true".toString()) && registro != null && registro.fasesRegistro.borrador.toString().equals("false".toString()) && !solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).getEstado().equals("Expirada".toString())) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}

	private ResultadoPermiso modificacionTrasPresentacionDeSolicitudAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		RegistroModificacion registroModificacion= null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty())){
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
			registroModificacion = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1);
		}else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		if (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor"))
			return new ResultadoPermiso(Accion.Editar);

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && !solicitud.estado.toString().equals("borrador".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("false".toString()))
			return new ResultadoPermiso(Accion.Leer);

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && solicitud.estado.toString().equals("modificacion".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("true".toString()) && registro != null && registro.fasesRegistro.borrador.toString().equals("true".toString()))
			return new ResultadoPermiso(Accion.Leer);

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && solicitud.estado.toString().equals("modificacion".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("true".toString()) && registro != null && registro.fasesRegistro.borrador.toString().equals("false".toString()) 
				&& (registroModificacion.estado.equals(EstadosModificacionEnum.enCurso.value())))
			return new ResultadoPermiso(Accion.Editar);

		return null;
	}

		/**
	 * Si no tiene verificaciones anteriores, no se cumple el permiso.
	 * @param grafico
	 * @param accion
	 * @param ids
	 * @param vars
	 * @return
	 */
	private ResultadoPermiso verificarObtenerNoProcede(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);
		if (solicitud.verificaciones.size() == 0)
			return null;
		Verificacion verificacion = getVerificacion(ids, vars);

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor")) && ((utils.StringUtils.in(accion.toString(), "leer", "editar")) && verificacion != null && utils.StringUtils.in(verificacion.estado.toString(), "obtenerNoProcede"))) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}
	
	public Verificacion getVerificacion(Map<String, Long> ids, Map<String, Object> vars) {
		if (vars != null && vars.containsKey("verificacion"))
			return (Verificacion) vars.get("verificacion");
		else if (ids != null && ids.containsKey("idVerificacion"))
			return Verificacion.findById(ids.get("idVerificacion"));
		return null;
	}

	public PeticionCesiones getPeticionCesiones(Map<String, Long> ids, Map<String, Object> vars) {
		if (vars != null && vars.containsKey("peticionCesiones"))
			return (PeticionCesiones) vars.get("peticionCesiones");
		else if (ids != null && ids.containsKey("idPeticionCesiones"))
			return PeticionCesiones.findById(ids.get("idPeticionCesiones"));
		return null;
	}
	
	
	private ResultadoPermiso menuConModificacion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);
		
		RegistroModificacion registroModificacion = null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registroModificacion = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1);
		else
			return null;
		
		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "revisor", "gestor") && (solicitud.estado.toString().equals("modificacion".toString()))) {
			return new ResultadoPermiso(Accion.All);

		}

		if (agente.rolActivo.toString().equals("usuario".toString()) && (solicitud.estado.toString().equals("modificacion".toString()))
				&& (registroModificacion != null) && (registroModificacion.estado.equals(EstadosModificacionEnum.enCurso.name()))) {
			return new ResultadoPermiso(Accion.All);

		} 
		return new ResultadoPermiso(Accion.Denegar);
	}
	
}