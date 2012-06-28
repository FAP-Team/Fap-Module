
package controllers.fap;

import java.util.Map;

import javax.inject.Inject;

import messages.Messages;
import messages.Messages.MessageType;
import models.CEconomico;
import models.Criterio;
import models.Evaluacion;
import models.SolicitudGenerica;
import models.TipoEvaluacion;
import models.ValoresCEconomico;

import org.apache.log4j.Logger;

import controllers.fap.AgenteController;
import controllers.fap.GenericController;
import controllers.fap.SecureController;

import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Util;
import play.mvc.With;
import security.Accion;
import security.Secure;
import validation.CustomValidation;

@With({SecureController.class, AgenteController.class})
public class PaginaCEConomicosController extends Controller{
	
	@Inject
	protected static Secure secure;
	
	private static Logger log = Logger.getLogger("Paginas");
	
	@Finally(only="index")
	public static void end(){
		Messages.deleteFlash();
	}

	public static void index(String accion, Long idSolicitud, Long idCEconomico, Integer duracion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/PCEconomicos/PopupCEConomicos.html");
		}

		SolicitudGenerica solicitud = PaginaCEConomicosController.getSolicitudGenerica(idSolicitud);

		CEconomico cEconomico = PaginaCEConomicosController.getCEconomico(idSolicitud, idCEconomico);
		log.info("Visitando página: " + "fap/PCEconomico/PopupCEConomicos.html");
		renderTemplate("fap/PCEconomicos/PopupCEConomicos.html", accion, idSolicitud,
				solicitud, idCEconomico, cEconomico, duracion);
	}

	public static void editar(Long idSolicitud, Long idCEconomico, CEconomico cEconomico, Integer duracion) {

		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}

		CEconomico dbCEconomico = PaginaCEConomicosController.getCEconomico(idSolicitud, idCEconomico);

		PaginaCEConomicosController.PopupCEConomicosBindReferences(cEconomico);

		if (!Messages.hasErrors()) {
			PaginaCEConomicosController.PopupCEConomicosValidateCopy("editar", dbCEconomico, cEconomico);
		}

		if (!Messages.hasErrors()) {
			PaginaCEConomicosController.editarValidateRules(dbCEconomico, cEconomico);
		}

		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: "
					+ "fap/PCEconomico/PopupCEConomicos.html"
					+ " , intentada con éxito");
			dbCEconomico.save();

		} else {
			flash();
			log.info("Acción Editar de página: "
					+ "fap/PCEconomico/PopupCEConomicos.html"
					+ " , intentada sin éxito (Problemas de Validación)");
		}

		PaginaCEConomicosController.editarRender(idSolicitud, idCEconomico, duracion);
	}

	@Util
	public static void PopupCEConomicosValidateCopy(String accion, CEconomico dbCEconomico, CEconomico cEconomico) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("cEconomico", cEconomico);
		int anios=0;
		for (ValoresCEconomico valor: dbCEconomico.valores){
			valor.valorSolicitado = cEconomico.valores.get(anios++).valorSolicitado;
		}
	}

	@Util
	public static boolean permiso(String accion) {

		if (Accion.parse(accion) == null)
			return false;
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack
				.top("idParams");
		return secure.checkAcceso("solicitudes", accion, ids, null);

	}

	@Util
	public static String getAccion() {

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack
				.top("idParams");
		return secure.getPrimeraAccion("solicitudes", ids, null);

	}

	@Util
	public static void editarRender(Long idSolicitud, Long idCEconomico, Integer duracion) {
		if (!Messages.hasMessages()) {
			renderJSON(utils.RestResponse
					.ok("Registro actualizado correctamente"));
			Messages.keep();
			redirect("popups.PopupCEConomicosController.index", "editar",
					idSolicitud, idCEconomico, duracion);
		}
		Messages.keep();
		redirect("popups.PopupCEConomicosController.index", "editar", idSolicitud, idCEconomico, duracion);
	}

	@Util
	public static void editarValidateRules(CEconomico dbCEconomico,
			CEconomico cEconomico) {
		//Sobreescribir para validar las reglas de negocio
	}

	@Util
	public static void PopupCEConomicosBindReferences(CEconomico cEconomico) {

	}

	@Util
	public static CEconomico getCEconomico(Long idSolicitud, Long idCEconomico) {
		CEconomico cEconomico = null;

		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains(
					"Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		}
		if (idCEconomico == null) {
			if (!Messages.messages(MessageType.FATAL).contains(
					"Falta parámetro idCEconomico"))
				Messages.fatal("Falta parámetro idCEconomico");
		}
		if (idSolicitud != null && idCEconomico != null) {
			cEconomico = CEconomico
					.find("select cEconomico from SolicitudGenerica solicitud join solicitud.ceconomicos cEconomico where solicitud.id=? and cEconomico.id=?",
							idSolicitud, idCEconomico).first();
			if (cEconomico == null) {
				Messages.fatal("Error al recuperar CEconomico");
			}
		}
		return cEconomico;
	}

	@Util
	public static SolicitudGenerica getSolicitudGenerica(Long idSolicitud) {
		SolicitudGenerica solicitud = null;
		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains(
					"Falta parámetro idSolicitud"))
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
	public static CEconomico getCEconomico() {
		return new CEconomico();
	}

	@Before
	static void beforeMethod() {
		renderArgs.put("controllerName", "PopupCEConomicosController");
	}
	
	@Util
	private static void flash(){
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		String param = "cEconomico";
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
		