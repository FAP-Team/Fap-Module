
package secure.gen;

import java.util.*;
import models.*;
import controllers.fap.AgenteController;
import secure.*;
		
public class PermissionFapGen  {	
	
	public static boolean administrador (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = agente.rolActivo.toString().equals("administrador".toString());
		return resultado;
	}
	
	public static boolean usuario (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = agente.rolActivo.toString().equals("usuario".toString());
		return resultado;
	}
	
	public static boolean noUsuario (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = agente.rolActivo.toString().equals("usuario".toString());
		return !resultado;
	}
	
	public static boolean logeado (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = agente != null;
		return resultado;
	}
	
	public static boolean presentacionPrepararParaFirmar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (action.toString().equals("update".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.registro.fasesRegistro.borrador.toString().equals("false".toString())) || (action.toString().equals("read".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")));
		return resultado;
	}
	
	public static boolean presentacionModificar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = action.toString().equals("read".toString()) || (action.toString().equals("update".toString()) && solicitud.registro.fasesRegistro.registro.toString().equals("false".toString()));
		return resultado;
	}
	
	public static boolean presentacionObtenerBorrador (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.registro.fasesRegistro.borrador.toString().equals("true".toString());
		return resultado;
	}
	
	public static boolean presentacionFirmar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.registro.fasesRegistro.borrador.toString().equals("true".toString()) && ((action.toString().equals("update".toString()) && solicitud.registro.fasesRegistro.firmada.toString().equals("false".toString())) || (action.toString().equals("read".toString())));
		return resultado;
	}
	
	public static boolean presentacionRegistrar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = solicitud.registro.fasesRegistro.firmada.toString().equals("true".toString()) && ((action.toString().equals("update".toString()) && ((agente.rolActivo.toString().equals("usuario".toString()) && solicitud.registro.fasesRegistro.registro.toString().equals("false".toString())) || (agente.rolActivo.toString().equals("administrador".toString()) && solicitud.registro.fasesRegistro.clasificarAed.toString().equals("false".toString())))) || action.toString().equals("read".toString()));
		return resultado;
	}
	
	public static boolean presentacionRecibo (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.registro.fasesRegistro.registro.toString().equals("true".toString());
		return resultado;
	}
	
	public static boolean instruccion (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = solicitud.estado.toString().equals("iniciada".toString());
		return resultado;
	}
	
	public static boolean solicitudPreparadaFirmarYPresentar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (action.toString().equals("update".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.estado.toString().equals("borrador".toString()) && solicitud.registro.fasesRegistro.borrador.toString().equals("true".toString())) || (action.toString().equals("read".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")));
		return resultado;
	}
	
	public static boolean editableSiSolicitudIniciada (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = action.toString().equals("update".toString()) && utils.StringUtils.in(solicitud.estado.toString(), "Iniciada", "Requerida", "Requerida plazo vencido", "En verificación", "Pendiente requerimiento", "Excluido", "Plazo vencido", "Verificado");
		return resultado;
	}
	
	public static boolean solicitudPreparadaFirmar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (action.toString().equals("update".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.estado.toString().equals("borrador".toString()) && solicitud.registro.fasesRegistro.borrador != null && solicitud.registro.fasesRegistro.borrador.toString().equals("true".toString())) || (action.toString().equals("read".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")));
		return resultado;
	}
	
	public static boolean solicitudPreparadaPresentar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (action.toString().equals("update".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && solicitud.estado.toString().equals("borrador".toString()) && solicitud.registro.fasesRegistro.borrador != null && solicitud.registro.fasesRegistro.borrador.toString().equals("true".toString()) && solicitud.registro.fasesRegistro.firmada.toString().equals("true".toString())) || (action.toString().equals("read".toString()) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")));
		return resultado;
	}
	
	public static boolean listaSolicitudes (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		//participacion
		Participacion participacion = Participacion.find("select p from Participacion p where p.agente=? AND p.solicitud=?", agente,solicitud).first();
				
		boolean resultado = (action.toString().equals("read".toString()) && agente.rolActivo.toString().equals("administrador".toString())) || (action.toString().equals("read".toString()) && participacion != null);
		return resultado;
	}
	
	public static boolean adminGestorRevisor (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = (utils.StringUtils.in(action.toString(), "read", "write")) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor"));
		return resultado;
	}
	
	public static boolean aportacion (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario")) && (utils.StringUtils.in(solicitud.estado.toString(), "iniciada", "requerida"));
		return resultado;
	}
	
	public static boolean aportacionDocumentos (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (action.toString().equals("read".toString())) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "usuario", "gestor", "revisor")) && (utils.StringUtils.in(solicitud.estado.toString(), "iniciada", "requrida", "requerida plazo vencido"));
		return resultado;
	}
	
	public static boolean noEditable (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = (action.toString().equals("update".toString()));
		return !resultado;
	}
	
	public static boolean noVisibleUsuario (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = (action.toString().equals("read".toString())) && agente.rolActivo.toString().equals("usuario".toString());
		return !resultado;
	}
	
	public static boolean solicitudEnBorrador (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = solicitud.estado.toString().equals("borrador".toString());
		return resultado;
	}
	
	public static boolean solicitudEditable (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (action.toString().equals("read".toString())) || (agente.rolActivo.toString().equals("administrador".toString())) || (agente.rolActivo.toString().equals("usuario".toString()) && solicitud.estado.toString().equals("borrador".toString()) && solicitud.registro.fasesRegistro.borrador.toString().equals("false".toString()));
		return resultado;
	}
	
	public static boolean solicitudEditableDocumentacion (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (agente.rolActivo.toString().equals("administrador".toString())) || (secure.PermissionFap.usuario(action, ids, vars) && solicitud.estado.toString().equals("borrador".toString()) && solicitud.registro.fasesRegistro.borrador.toString().equals("false".toString()));
		return resultado;
	}
	
	public static boolean visibleSiAccesoCertificado (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = agente.acceso.toString().equals("certificado".toString());
		return resultado;
	}
	
	public static boolean visibleSiAccesoContrasena (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		boolean resultado = (agente.acceso.toString().equals("usuario".toString()));
		return resultado;
	}
	
	public static boolean documentoAutorizacionGenerado (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (solicitud.registro.autorizacionFuncionario.urlDescarga != null) && (!agente.funcionario.toString().equals("true".toString()));
		return resultado;
	}
	
	public static boolean visibleFuncionarioAutorizado (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (agente.funcionario.toString().equals("true".toString())) && (solicitud.solicitante.autorizaFuncionario.toString().equals("true".toString()));
		return resultado;
	}
	
	public static boolean noVisibleFuncionarioAutorizado (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (agente.funcionario.toString().equals("true".toString())) && (solicitud.solicitante.autorizaFuncionario.toString().equals("true".toString()));
		return !resultado;
	}
	
	public static boolean aportacionModificar (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = utils.StringUtils.in(solicitud.aportaciones.actual.estado.toString(), "borrador");
		return resultado;
	}
	
	public static boolean aportacionMensajeIntermedio (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = utils.StringUtils.in(solicitud.aportaciones.actual.estado.toString(), "firmada", "registrada", "clasificada", "finalizada");
		return resultado;
	}
	
	public static boolean mensajeVerificacion (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (action.toString().equals("read".toString())) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor")) && (solicitud.estado.toString().equals("requerida plazo vencido".toString()));
		return resultado;
	}
	
	public static boolean iniciarVerificacion (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (utils.StringUtils.in(action.toString(), "read", "update")) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor")) && (solicitud.estado.toString().equals("iniciada".toString()));
		return resultado;
	}
	
	public static boolean verificarDocumentos (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor")) && (((action.toString().equals("read".toString())) && (utils.StringUtils.in(solicitud.estado.toString(), "requerida", "pendiente requerimiento", "requerida plazo vencido", "en verificacion"))) || ((action.toString().equals("update".toString())) && (solicitud.estado.toString().equals("en verificacion".toString()))));
		return resultado;
	}
	
	public static boolean verificacionRequerimientos (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (utils.StringUtils.in(action.toString(), "read", "update")) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor")) && (utils.StringUtils.in(solicitud.estado.toString(), "en verificacion", "pendiente requerimiento", "requerida", "requerida plazo vencido", "verificada", "excluido", "plazo vencido"));
		return resultado;
	}
	
	public static boolean nuevoRequerimiento (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (utils.StringUtils.in(action.toString(), "read", "update")) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor")) && (solicitud.estado.toString().equals("pendiente requerimiento".toString()));
		return resultado;
	}
	
	public static boolean firmarRequerimiento (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (solicitud.estado.toString().equals("pendiente requerimiento".toString())) && (((action.toString().equals("read".toString())) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor"))) || ((action.toString().equals("update".toString())) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor"))));
		return resultado;
	}
	
	public static boolean finalizarRequerimiento (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		
		//solicitud
		SolicitudGenerica solicitud = null;
		if((vars != null) && (vars.containsKey("solicitud"))){
			solicitud = (SolicitudGenerica) vars.get("solicitud");
		}else if((ids != null) && (ids.containsKey("idSolicitud"))){
			solicitud = SolicitudGenerica.findById(ids.get("idSolicitud"));
		}else if(Singleton.class.isAssignableFrom(SolicitudGenerica.class)){
			solicitud = SolicitudGenerica.all().first();
		}
		
		if (solicitud == null)
			return false;

		boolean resultado = (utils.StringUtils.in(action.toString(), "read", "update")) && (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "revisor")) && (utils.StringUtils.in(solicitud.estado.toString(), "requerida", "requerida plazo vencido"));
		return resultado;
	}

}
