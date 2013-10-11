package controllers.popups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import messages.Messages;
import messages.Messages.MessageType;
import models.CEconomico;
import models.CEconomicosManuales;
import models.SolicitudGenerica;
import models.TipoEvaluacion;
import models.ValoresCEconomico;

import org.apache.log4j.Logger;

import play.mvc.After;
import play.mvc.Before;
import play.mvc.Util;
import security.Accion;
import validation.CustomValidation;
import controllers.PaginaCEconomicoManualController;
import controllers.fap.AgenteController;
import controllers.gen.popups.PopUpEditarCEconomicosEvaluadosControllerGen;

public class PopUpEditarCEconomicosEvaluadosController extends PopUpEditarCEconomicosEvaluadosControllerGen {

	protected static Logger log = Logger.getLogger("Paginas");

	public static void index(String accion, Long idSolicitud, Long idCEconomico, Long idCEconomicosManuales, String urlRedirigir) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("gen/popups/PopUpEditarCEconomicosEvaluados.html");
		}

		SolicitudGenerica solicitud = PopUpEditarCEconomicosEvaluadosController.getSolicitudGenerica(idSolicitud);
		CEconomico cEconomico = PopUpEditarCEconomicosEvaluadosController.getCEconomico(idSolicitud, idCEconomico);

		CEconomicosManuales cEconomicosManuales = null;
		if ("crear".equals(accion)) {
			cEconomicosManuales = PopUpEditarCEconomicosEvaluadosController.getCEconomicosManuales();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				cEconomicosManuales.save();
				idCEconomicosManuales = cEconomicosManuales.id;
				cEconomico.otros.add(cEconomicosManuales);
				cEconomico.save();

				accion = "editar";
			}
			String variablesRedirigir = "";
			variablesRedirigir += "&idSolicitud=" + idSolicitud;
			variablesRedirigir += "&idCEconomico=" + idCEconomico;
			urlRedirigir += variablesRedirigir;
		}else if (!"borrado".equals(accion)){
				cEconomicosManuales = PaginaCEconomicoManualController.getCEconomicosManuales(idCEconomico, idCEconomicosManuales);
				if (cEconomicosManuales == null){
					cEconomicosManuales = getFlashCEconomicosManuales();
					accion= "crear";
					Messages.clear();
				}
		}
		log.info("Visitando página: " + "gen/popups/PopUpEditarCEconomicosEvaluados.html" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		renderTemplate("fap/PopUpEditarCEcomicosEvaluados/PopUpEditarCEconomicosEvaluados.html", accion, idSolicitud, idCEconomico, idCEconomicosManuales, solicitud, cEconomico, cEconomicosManuales, urlRedirigir);
	}
	
	@Util
	public static void PopUpEditarCEconomicosEvaluadosValidateCopy(String accion, CEconomicosManuales dbCEconomicosManuales, CEconomicosManuales cEconomicosManuales) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("cEconomico", cEconomicosManuales);
			
		if ((dbCEconomicosManuales.valores == null) || (dbCEconomicosManuales.valores.size() == 0))
			dbCEconomicosManuales.valores = cEconomicosManuales.valores;
		else {
			for (int i=0; i < cEconomicosManuales.valores.size(); i++) {
				dbCEconomicosManuales.valores.get(i).valorPropuesto = cEconomicosManuales.valores.get(i).valorPropuesto;
				dbCEconomicosManuales.valores.get(i).valorConcedido = cEconomicosManuales.valores.get(i).valorConcedido;
			}
		}
		dbCEconomicosManuales.save();
	}
	@Util
	private static void flash(CEconomicosManuales dbCEconomicoManual){
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		String param = "cEconomicosManuales";
		if (dbCEconomicoManual.tipo.tipoOtro){
			for (int i = 0; i < tipoEvaluacion.duracion; i++){
				Messages.setFlash(param + ".valores["+i+"].valorEstimado", dbCEconomicoManual.valores.get(i).valorEstimado);
				Messages.setFlash(param + ".valores["+i+"].valorSolicitado", dbCEconomicoManual.valores.get(i).valorSolicitado);
				Messages.setFlash(param + ".valores["+i+"].valorPropuesto", dbCEconomicoManual.valores.get(i).valorPropuesto);
				Messages.setFlash(param + ".valores["+i+"].valorConcedido", dbCEconomicoManual.valores.get(i).valorConcedido);
			}
			Messages.setFlash(param + ".comentariosAdministracion", dbCEconomicoManual.comentariosAdministracion);
			Messages.setFlash(param + ".comentariosSolicitante", dbCEconomicoManual.comentariosSolicitante);
		} else {
			for (int i = 0; i < tipoEvaluacion.duracion; i++){
				Messages.setFlash(param + ".valores["+i+"].valorEstimado", params.get(param + ".valores["+i+"].valorEstimado", String.class));
				Messages.setFlash(param + ".valores["+i+"].valorSolicitado", params.get(param + ".valores["+i+"].valorSolicitado", String.class));
				Messages.setFlash(param + ".valores["+i+"].valorPropuesto", params.get(param + ".valores["+i+"].valorPropuesto", String.class));
				Messages.setFlash(param + ".valores["+i+"].valorConcedido", params.get(param + ".valores["+i+"].valorConcedido", String.class));
			}
			Messages.setFlash(param + ".comentariosAdministracion", params.get(param + ".comentariosAdministracion", String.class));
			Messages.setFlash(param + ".comentariosSolicitante", params.get(param + ".comentariosSolicitante", String.class));
		}
	}
	
	private static CEconomicosManuales getFlashCEconomicosManuales (){
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		CEconomicosManuales cEconomicosManuales = PaginaCEconomicoManualController.getCEconomicosManuales();
		cEconomicosManuales.valores = new ArrayList<ValoresCEconomico>();
		String param = "cEconomicosManuales";
		for (int i=0; i <= tipoEvaluacion.duracion; i++) {
			ValoresCEconomico vCE = new ValoresCEconomico(i);
			//vCE.valorConcedido = Double.parseDouble(Messages.getFlash(param + ".valores["+i+"].valorConcedido").toString());
			//vCE.valorPropuesto = Double.parseDouble(Messages.getFlash(param + ".valores["+i+"].valorPropuesto").toString());
			// Recupera un 'Y' por ejemplo, y no la puede parsear a DOUBLE!!!!!!!
			try {
				vCE.valorSolicitado = Double.parseDouble(Messages.getFlash(param + ".valores["+i+"].valorSolicitado").toString());
			} catch (Exception e){
				vCE.valorSolicitado = null;
			}
			//vCE.valorEstimado = Double.parseDouble(Messages.getFlash(param + ".valores["+i+"].valorEstimado").toString());
			cEconomicosManuales.valores.add(vCE);
		}
		//cEconomicosManuales.comentariosAdministracion=Messages.getFlash(param + ".comentariosAdministracion").toString();
	    //cEconomicosManuales.comentariosSolicitante=Messages.getFlash(param + ".comentariosSolicitante").toString();
	    return cEconomicosManuales;
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(Long idSolicitud, Long idCEconomico, Long idCEconomicosManuales, CEconomicosManuales cEconomicosManuales) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		CEconomicosManuales dbCEconomicosManuales = PopUpEditarCEconomicosEvaluadosController.getCEconomicosManuales(idCEconomico, idCEconomicosManuales);

		//PopUpEditarCEconomicosEvaluadosController.PopUpEditarCEconomicosEvaluadosBindReferences(cEconomicosManuales);

		if (!Messages.hasErrors()) {
			PopUpEditarCEconomicosEvaluadosController.PopUpEditarCEconomicosEvaluadosValidateCopy("editar", dbCEconomicosManuales, cEconomicosManuales);
		}
//
//		if (!Messages.hasErrors()) {
//			PopUpEditarCEconomicosEvaluadosController.editarValidateRules(dbCEconomicosManuales, cEconomicosManuales);
//		}
		if (!Messages.hasErrors()) {
			dbCEconomicosManuales.save();
			flash(cEconomicosManuales);
			log.info("Acción Editar de página: " + "gen/popups/PopUpEditarCEconomicosEvaluados.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		} else
			log.info("Acción Editar de página: " + "gen/popups/PopUpEditarCEconomicosEvaluados.html" + " , intentada sin éxito (Problemas de Validación)");
		PopUpEditarCEconomicosEvaluadosController.editarRender(idSolicitud, idCEconomico, idCEconomicosManuales);
	}

	@Util
	public static boolean permiso(String accion) {

		if (Accion.parse(accion) == null)
			return false;
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		return secure.checkAcceso("solicitudes", accion, ids, null);

	}

	@Util
	public static String getAccion() {

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		return secure.getPrimeraAccion("solicitudes", ids, null);

	}

	@Util
	public static String getNamePermiso() {
		return "solicitudes";
	}

	@Util
	public static void editarRender(Long idSolicitud, Long idCEconomico, Long idCEconomicosManuales) {
		if (!Messages.hasMessages()) {
			renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
			Messages.keep();
			redirect("popups.PopUpEditarCEconomicosEvaluadosController.index", "editar", idSolicitud, idCEconomico, idCEconomicosManuales);
		}
		Messages.keep();
		redirect("popups.PopUpEditarCEconomicosEvaluadosController.index", "editar", idSolicitud, idCEconomico, idCEconomicosManuales);
	}

	@Util
	public static void editarValidateRules(CEconomicosManuales dbCEconomicosManuales, CEconomicosManuales cEconomicosManuales) {
		//Sobreescribir para validar las reglas de negocio
	}

	@Util
	public static void PopUpEditarCEconomicosEvaluadosBindReferences(CEconomicosManuales cEconomicosManuales) {

	}

	@Util
	public static SolicitudGenerica getSolicitudGenerica(Long idSolicitud) {
		SolicitudGenerica solicitud = null;
		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		} else {
			solicitud = SolicitudGenerica.findById(idSolicitud);
			if (solicitud == null) {
				Messages.fatal("Error al recuperar SolicitudGenerica");
			}
		}
		return solicitud;
	}

	@Util
	public static CEconomico getCEconomico(Long idSolicitud, Long idCEconomico) {
		CEconomico cEconomico = null;

		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		}

		if (idCEconomico == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idCEconomico"))
				Messages.fatal("Falta parámetro idCEconomico");
		}
		if (idSolicitud != null && idCEconomico != null) {
			cEconomico = CEconomico.find("select cEconomico from SolicitudGenerica solicitud join solicitud.ceconomicos cEconomico where solicitud.id=? and cEconomico.id=?", idSolicitud, idCEconomico).first();
			if (cEconomico == null)
				Messages.fatal("Error al recuperar CEconomico");
		}
		return cEconomico;
	}

	@Util
	public static CEconomicosManuales getCEconomicosManuales(Long idCEconomico, Long idCEconomicosManuales) {
		CEconomicosManuales cEconomicosManuales = null;

		if (idCEconomico == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idCEconomico"))
				Messages.fatal("Falta parámetro idCEconomico");
		}

		if (idCEconomicosManuales == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idCEconomicosManuales"))
				Messages.fatal("Falta parámetro idCEconomicosManuales");
		}
		if (idCEconomico != null && idCEconomicosManuales != null) {
			cEconomicosManuales = CEconomicosManuales.find("select cEconomicosManuales from CEconomico cEconomico join cEconomico.otros cEconomicosManuales where cEconomico.id=? and cEconomicosManuales.id=?", idCEconomico, idCEconomicosManuales).first();
			if (cEconomicosManuales == null)
				Messages.fatal("Error al recuperar CEconomicosManuales");
		}
		return cEconomicosManuales;
	}

	@Util
	public static CEconomicosManuales getCEconomicosManuales() {
		return new CEconomicosManuales();
	}

	@Before
	static void beforeMethod() {
		renderArgs.put("controllerName", "PopUpEditarCEconomicosEvaluadosControllerGen");
	}

	@After(only = { "PopUpEditarCEconomicosEvaluadosController.editar", "PopUpEditarCEconomicosEvaluadosControllerGen.editar" })
	protected static void setEntidadesProcesada() {
		unsetEntidadesProcesando();
	}

	@Before(only = { "PopUpEditarCEconomicosEvaluadosController.editar", "PopUpEditarCEconomicosEvaluadosControllerGen.editar" })
	protected static void setEntidadesProcesandose() {
		setEntidadesProcesando();
	}

	@Util
	public static void copyModificacionCamposSimples(String campoStr, Object campo, Object dbCampo, utils.PeticionModificacion peticionModificacion) {
		List<String> valoresNuevos = new ArrayList<String>();
		List<String> valoresAntiguos = new ArrayList<String>();
		if (((dbCampo == null) ^ (campo == null)) || ((campo != null) && (!campo.equals(dbCampo)))) {
			if (dbCampo != null)
				valoresAntiguos.add(dbCampo.toString());
			if ((campo == null))
				valoresNuevos.add("");
			else
				valoresNuevos.add(campo.toString());
			peticionModificacion.setValorModificado(campoStr, valoresAntiguos, valoresNuevos);
		}
	}
	
}
