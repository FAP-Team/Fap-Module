
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
import models.Registro;
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

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && solicitud.estado.toString().equals("modificacion".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("true".toString()) && registro != null && registro.fasesRegistro.borrador.toString().equals("false".toString())) {
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
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		if (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor"))
			return new ResultadoPermiso(Accion.Editar);

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && !solicitud.estado.toString().equals("borrador".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("false".toString()))
			return new ResultadoPermiso(Accion.Leer);

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && solicitud.estado.toString().equals("modificacion".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("true".toString()) && registro != null && registro.fasesRegistro.borrador.toString().equals("true".toString()))
			return new ResultadoPermiso(Accion.Leer);

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && solicitud.estado.toString().equals("modificacion".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("true".toString()) && registro != null && registro.fasesRegistro.borrador.toString().equals("false".toString()))
			return new ResultadoPermiso(Accion.Editar);

		return null;
	}
}