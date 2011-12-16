
package security;

import java.util.Map;

import models.*;
import controllers.fap.AgenteController;

public class SecureFapGen extends Secure {

	public SecureFapGen(Secure next) {
		super(next);
	}

	@Override
	public boolean check(String id, String action, Map<String, Long> ids, Map<String, Object> vars) {

		if("administrador".equals(id))
			return administrador(action, ids, vars);

		else if("usuario".equals(id))
			return usuario(action, ids, vars);
	  
		else if("noUsuario".equals(id))
			return noUsuario(action, ids, vars);
	  
		else if("logeado".equals(id))
			return logeado(action, ids, vars);
	  
		else if("presentacionPrepararParaFirmar".equals(id))
			return presentacionPrepararParaFirmar(action, ids, vars);
	  
		else if("presentacionModificar".equals(id))
			return presentacionModificar(action, ids, vars);
	  
		else if("presentacionObtenerBorrador".equals(id))
			return presentacionObtenerBorrador(action, ids, vars);
	  
		else if("presentacionFirmar".equals(id))
			return presentacionFirmar(action, ids, vars);
	  
		else if("presentacionRegistrar".equals(id))
			return presentacionRegistrar(action, ids, vars);
	  
		else if("presentacionRecibo".equals(id))
			return presentacionRecibo(action, ids, vars);
	  
		else if("instruccion".equals(id))
			return instruccion(action, ids, vars);
	  
		else if("solicitudPreparadaFirmarYPresentar".equals(id))
			return solicitudPreparadaFirmarYPresentar(action, ids, vars);
	  
		else if("editableSiSolicitudIniciada".equals(id))
			return editableSiSolicitudIniciada(action, ids, vars);
	  
		else if("solicitudPreparadaFirmar".equals(id))
			return solicitudPreparadaFirmar(action, ids, vars);
	  
		else if("solicitudPreparadaPresentar".equals(id))
			return solicitudPreparadaPresentar(action, ids, vars);
	  
		else if("listaSolicitudes".equals(id))
			return listaSolicitudes(action, ids, vars);
	  
		else if("adminGestorRevisor".equals(id))
			return adminGestorRevisor(action, ids, vars);
	  
		else if("aportacion".equals(id))
			return aportacion(action, ids, vars);
	  
		else if("aportacionDocumentos".equals(id))
			return aportacionDocumentos(action, ids, vars);
	  
		else if("noEditable".equals(id))
			return noEditable(action, ids, vars);
	  
		else if("noVisibleUsuario".equals(id))
			return noVisibleUsuario(action, ids, vars);
	  
		else if("solicitudEnBorrador".equals(id))
			return solicitudEnBorrador(action, ids, vars);
	  
		else if("solicitudEditable".equals(id))
			return solicitudEditable(action, ids, vars);
	  
		else if("solicitudEditableDocumentacion".equals(id))
			return solicitudEditableDocumentacion(action, ids, vars);
	  
		else if("visibleSiAccesoCertificado".equals(id))
			return visibleSiAccesoCertificado(action, ids, vars);
	  
		else if("visibleSiAccesoContrasena".equals(id))
			return visibleSiAccesoContrasena(action, ids, vars);
	  
		else if("documentoAutorizacionGenerado".equals(id))
			return documentoAutorizacionGenerado(action, ids, vars);
	  
		else if("visibleFuncionarioAutorizado".equals(id))
			return visibleFuncionarioAutorizado(action, ids, vars);
	  
		else if("noVisibleFuncionarioAutorizado".equals(id))
			return noVisibleFuncionarioAutorizado(action, ids, vars);
	  
		else if("aportacionModificar".equals(id))
			return aportacionModificar(action, ids, vars);
	  
		else if("aportacionMensajeIntermedio".equals(id))
			return aportacionMensajeIntermedio(action, ids, vars);
	  
		else if("mensajeVerificacion".equals(id))
			return mensajeVerificacion(action, ids, vars);
	  
		else if("iniciarVerificacion".equals(id))
			return iniciarVerificacion(action, ids, vars);
	  
		else if("verificarDocumentos".equals(id))
			return verificarDocumentos(action, ids, vars);
	  
		else if("verificacionRequerimientos".equals(id))
			return verificacionRequerimientos(action, ids, vars);
	  
		else if("nuevoRequerimiento".equals(id))
			return nuevoRequerimiento(action, ids, vars);
	  
		else if("firmarRequerimiento".equals(id))
			return firmarRequerimiento(action, ids, vars);
	  
		else if("finalizarRequerimiento".equals(id))
			return finalizarRequerimiento(action, ids, vars);
	  
		else if("aportacionNoNull".equals(id))
			return aportacionNoNull(action, ids, vars);
	  
		else if("nuevaSolicitud".equals(id))
			return nuevaSolicitud(action, ids, vars);
	  
		else if("tableKeyOnlyEstadosSolicitud".equals(id))
			return tableKeyOnlyEstadosSolicitud(action, ids, vars);
	  
		else if("tableKeyOnlyEstadosSolicitudUsuario".equals(id))
			return tableKeyOnlyEstadosSolicitudUsuario(action, ids, vars);
	  		
		return nextCheck(id, action, ids, vars);
	}
	
		
	private boolean administrador (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = agente.rolActivo.toString().equals("administrador".toString());
		return resultado;
	}
	
	private boolean usuario (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = agente.rolActivo.toString().equals("usuario".toString());
		return resultado;
	}
	
	private boolean noUsuario (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = agente.rolActivo.toString().equals("usuario".toString());
		return !resultado;
	}
	
	private boolean logeado (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = agente != null;
		return resultado;
	}
	
	private boolean presentacionPrepararParaFirmar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (action.toString().equals("update".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.registro.fasesRegistro.borrador.toString().equals("false".toString())) || (action.toString().equals("read".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")));
		return resultado;
	}
	
	private boolean presentacionModificar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = action.toString().equals("read".toString()) || (action.toString().equals("update".toString()) && solicitud.registro.fasesRegistro.registro.toString().equals("false".toString()));
		return resultado;
	}
	
	private boolean presentacionObtenerBorrador (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.registro.fasesRegistro.borrador.toString().equals("true".toString());
		return resultado;
	}
	
	private boolean presentacionFirmar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.registro.fasesRegistro.borrador.toString().equals("true".toString()) && ((action.toString().equals("update".toString()) && solicitud.registro.fasesRegistro.firmada.toString().equals("false".toString())) || (action.toString().equals("read".toString())));
		return resultado;
	}
	
	private boolean presentacionRegistrar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = solicitud.registro.fasesRegistro.firmada.toString().equals("true".toString()) && ((action.toString().equals("update".toString()) && ((agente.rolActivo.toString().equals("usuario".toString()) && solicitud.registro.fasesRegistro.registro.toString().equals("false".toString())) || (agente.rolActivo.toString().equals("administrador".toString()) && solicitud.registro.fasesRegistro.clasificarAed.toString().equals("false".toString())))) || action.toString().equals("read".toString()));
		return resultado;
	}
	
	private boolean presentacionRecibo (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.registro.fasesRegistro.registro.toString().equals("true".toString());
		return resultado;
	}
	
	private boolean instruccion (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = solicitud.estado.toString().equals("iniciada".toString());
		return resultado;
	}
	
	private boolean solicitudPreparadaFirmarYPresentar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (action.toString().equals("update".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.estado.toString().equals("borrador".toString()) && solicitud.registro.fasesRegistro.borrador.toString().equals("true".toString())) || (action.toString().equals("read".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")));
		return resultado;
	}
	
	private boolean editableSiSolicitudIniciada (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = action.toString().equals("update".toString()) && utils.StringUtils.in(solicitud.estado.toString(), "Iniciada", "Requerida", "Requerida plazo vencido", "En verificación", "Pendiente requerimiento", "Excluido", "Plazo vencido", "Verificado");
		return resultado;
	}
	
	private boolean solicitudPreparadaFirmar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (action.toString().equals("update".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.estado.toString().equals("borrador".toString()) && solicitud.registro.fasesRegistro.borrador != null && solicitud.registro.fasesRegistro.borrador.toString().equals("true".toString())) || (action.toString().equals("read".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")));
		return resultado;
	}
	
	private boolean solicitudPreparadaPresentar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (action.toString().equals("update".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.estado.toString().equals("borrador".toString()) && solicitud.registro.fasesRegistro.borrador != null && solicitud.registro.fasesRegistro.borrador.toString().equals("true".toString()) && solicitud.registro.fasesRegistro.firmada.toString().equals("true".toString())) || (action.toString().equals("read".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")));
		return resultado;
	}
	
	private boolean listaSolicitudes (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		//participacion
		Participacion participacion = Participacion.find("select p from Participacion p where p.agente=? AND p.solicitud=?", agente,solicitud).first();
				
		boolean resultado = (action.toString().equals("read".toString()) && agente.rolActivo.toString().equals("administrador".toString())) || (action.toString().equals("read".toString()) && participacion != null);
		return resultado;
	}
	
	private boolean adminGestorRevisor (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = (utils.StringUtils.in(action.toString(), "read", "update")) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor"));
		return resultado;
	}
	
	private boolean aportacion (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && (utils.StringUtils.in(solicitud.estado.toString(), "iniciada", "requerida"));
		return resultado;
	}
	
	private boolean aportacionDocumentos (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (action.toString().equals("read".toString())) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario", "gestor", "revisor")) && (utils.StringUtils.in(solicitud.estado.toString(), "iniciada", "requerida", "requerida plazo vencido"));
		return resultado;
	}
	
	private boolean noEditable (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = (action.toString().equals("update".toString()));
		return !resultado;
	}
	
	private boolean noVisibleUsuario (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = (action.toString().equals("read".toString())) && agente.rolActivo.toString().equals("usuario".toString());
		return !resultado;
	}
	
	private boolean solicitudEnBorrador (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = solicitud.estado.toString().equals("borrador".toString());
		return resultado;
	}
	
	private boolean solicitudEditable (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (action.toString().equals("read".toString())) || (agente.rolActivo.toString().equals("administrador".toString())) || (agente.rolActivo.toString().equals("usuario".toString()) && solicitud.estado.toString().equals("borrador".toString()) && solicitud.registro.fasesRegistro.borrador.toString().equals("false".toString()));
		return resultado;
	}
	
	private boolean solicitudEditableDocumentacion (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (agente.rolActivo.toString().equals("administrador".toString())) || (config.InjectorConfig.getInjector().getInstance(security.Secure.class).check("usuario", action, ids, vars) && solicitud.estado.toString().equals("borrador".toString()) && solicitud.registro.fasesRegistro.borrador.toString().equals("false".toString()));
		return resultado;
	}
	
	private boolean visibleSiAccesoCertificado (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = agente.acceso.toString().equals("certificado".toString());
		return resultado;
	}
	
	private boolean visibleSiAccesoContrasena (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = (agente.acceso.toString().equals("usuario".toString()));
		return resultado;
	}
	
	private boolean documentoAutorizacionGenerado (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (solicitud.registro.autorizacionFuncionario.urlDescarga != null) && (!agente.funcionario.toString().equals("true".toString()));
		return resultado;
	}
	
	private boolean visibleFuncionarioAutorizado (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (agente.funcionario.toString().equals("true".toString())) && (solicitud.solicitante.autorizaFuncionario.toString().equals("true".toString()));
		return resultado;
	}
	
	private boolean noVisibleFuncionarioAutorizado (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (agente.funcionario.toString().equals("true".toString())) && (solicitud.solicitante.autorizaFuncionario.toString().equals("true".toString()));
		return !resultado;
	}
	
	private boolean aportacionModificar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = utils.StringUtils.in(solicitud.aportaciones.actual.estado.toString(), "borrador");
		return resultado;
	}
	
	private boolean aportacionMensajeIntermedio (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = utils.StringUtils.in(solicitud.aportaciones.actual.estado.toString(), "firmada", "registrada", "clasificada", "finalizada");
		return resultado;
	}
	
	private boolean mensajeVerificacion (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (action.toString().equals("read".toString())) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor")) && (solicitud.estado.toString().equals("requerida plazo vencido".toString()));
		return resultado;
	}
	
	private boolean iniciarVerificacion (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (utils.StringUtils.in(action.toString(), "read", "update")) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor")) && (solicitud.estado.toString().equals("iniciada".toString()));
		return resultado;
	}
	
	private boolean verificarDocumentos (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor")) && (((action.toString().equals("read".toString())) && (utils.StringUtils.in(solicitud.estado.toString(), "requerida", "pendiente requerimiento", "requerida plazo vencido", "en verificacion"))) || ((action.toString().equals("update".toString())) && (solicitud.estado.toString().equals("en verificacion".toString()))));
		return resultado;
	}
	
	private boolean verificacionRequerimientos (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (utils.StringUtils.in(action.toString(), "read", "update")) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor")) && (utils.StringUtils.in(solicitud.estado.toString(), "en verificacion", "pendiente requerimiento", "requerida", "requerida plazo vencido", "verificada", "excluido", "plazo vencido"));
		return resultado;
	}
	
	private boolean nuevoRequerimiento (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (utils.StringUtils.in(action.toString(), "read", "update")) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor")) && (solicitud.estado.toString().equals("pendiente requerimiento".toString()));
		return resultado;
	}
	
	private boolean firmarRequerimiento (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (solicitud.estado.toString().equals("pendiente requerimiento".toString())) && (((action.toString().equals("read".toString())) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor"))) || ((action.toString().equals("update".toString())) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor"))));
		return resultado;
	}
	
	private boolean finalizarRequerimiento (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
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

		boolean resultado = (utils.StringUtils.in(action.toString(), "read", "update")) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor")) && (utils.StringUtils.in(solicitud.estado.toString(), "requerida", "requerida plazo vencido"));
		return resultado;
	}
	
	private boolean aportacionNoNull (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//doc
		Documento doc = null;
		if((vars != null) && (vars.containsKey("doc"))){
			doc = (Documento) vars.get("doc");
		}else if((ids != null) && (ids.containsKey("idDocumento"))){
			doc = Documento.findById(ids.get("idDocumento"));
		}else if(Singleton.class.isAssignableFrom(Documento.class)){
			try {
				doc = (Documento) Documento.class.getMethod("get", Class.class).invoke(null, Documento.class);
			} catch (Exception e) {}
		}
		
		if (doc == null)
			return false;

		boolean resultado = doc.uri == null;
		return !resultado;
	}
	
	private boolean nuevaSolicitud (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//convocatoria
		Convocatoria convocatoria = null;
		if((vars != null) && (vars.containsKey("convocatoria"))){
			convocatoria = (Convocatoria) vars.get("convocatoria");
		}else if((ids != null) && (ids.containsKey("idConvocatoria"))){
			convocatoria = Convocatoria.findById(ids.get("idConvocatoria"));
		}else if(Singleton.class.isAssignableFrom(Convocatoria.class)){
			try {
				convocatoria = (Convocatoria) Convocatoria.class.getMethod("get", Class.class).invoke(null, Convocatoria.class);
			} catch (Exception e) {}
		}
		
		if (convocatoria == null)
			return false;

		boolean resultado = convocatoria.estado.toString().equals("presentacion".toString());
		return resultado;
	}
	
	private boolean tableKeyOnlyEstadosSolicitud (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//tableKeyValue
		TableKeyValue tableKeyValue = null;
		if((vars != null) && (vars.containsKey("tableKeyValue"))){
			tableKeyValue = (TableKeyValue) vars.get("tableKeyValue");
		}else if((ids != null) && (ids.containsKey("idTableKeyValue"))){
			tableKeyValue = TableKeyValue.findById(ids.get("idTableKeyValue"));
		}else if(Singleton.class.isAssignableFrom(TableKeyValue.class)){
			try {
				tableKeyValue = (TableKeyValue) TableKeyValue.class.getMethod("get", Class.class).invoke(null, TableKeyValue.class);
			} catch (Exception e) {}
		}
		
		if (tableKeyValue == null)
			return false;

		boolean resultado = tableKeyValue.table.toString().equals("estadosSolicitud".toString()) && agente.rolActivo.toString().equals("administrador".toString());
		return resultado;
	}
	
	private boolean tableKeyOnlyEstadosSolicitudUsuario (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//tableKeyValue
		TableKeyValue tableKeyValue = null;
		if((vars != null) && (vars.containsKey("tableKeyValue"))){
			tableKeyValue = (TableKeyValue) vars.get("tableKeyValue");
		}else if((ids != null) && (ids.containsKey("idTableKeyValue"))){
			tableKeyValue = TableKeyValue.findById(ids.get("idTableKeyValue"));
		}else if(Singleton.class.isAssignableFrom(TableKeyValue.class)){
			try {
				tableKeyValue = (TableKeyValue) TableKeyValue.class.getMethod("get", Class.class).invoke(null, TableKeyValue.class);
			} catch (Exception e) {}
		}
		
		if (tableKeyValue == null)
			return false;

		boolean resultado = tableKeyValue.table.toString().equals("estadosSolicitudUsuario".toString()) && agente.rolActivo.toString().equals("administrador".toString());
		return resultado;
	}

}
