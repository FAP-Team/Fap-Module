
package controllers.popups;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import messages.Messages;
import messages.Messages.MessageType;
import models.CEconomico;
import models.CEconomicosManuales;
import models.SolicitudGenerica;
import models.TipoEvaluacion;

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
public class PopupCEConomicosEvaluadosGestorController extends Controller{
	
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
		
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		
		CEconomico cEconomico = PopupCEConomicosEvaluadosGestorController.getCEconomico(idSolicitud, idCEconomico);
		
		//Inicializar valores de suma???
		if (cEconomico.tipo.tipoOtro) {
			for (int i=0; i<=duracion; i++) {
				cEconomico.valores.get(i).valorConcedido = 0.0;
				cEconomico.valores.get(i).valorPropuesto = 0.0;
				for(int j=0; j<cEconomico.otros.get(i).valores.size(); j++) {
					cEconomico.valores.get(i).valorConcedido += cEconomico.otros.get(j).valores.get(i).valorConcedido != null ? cEconomico.otros.get(j).valores.get(i).valorConcedido : 0.0;
					cEconomico.valores.get(i).valorPropuesto += cEconomico.otros.get(j).valores.get(i).valorPropuesto != null ? cEconomico.otros.get(j).valores.get(i).valorPropuesto : 0.0;
				}
			}
		}
		
		log.info("Visitando página: " + "fap/PCEconomico/PopupCEConomicosEvaluadosGestor.html");
		renderTemplate("fap/Baremacion/PopupCEConomicosEvaluadosGestor.html", accion, idSolicitud, solicitud, idCEconomico, cEconomico, duracion);
	}

	public static void editar(Long idSolicitud, Long idCEconomico, CEconomico cEconomico, Integer duracion) {
		
		checkAuthenticity();

		CEconomico dbCEconomico = PopupCEConomicosEvaluadosGestorController.getCEconomico(idSolicitud, idCEconomico);

		if (!Messages.hasErrors()) {
			PopupCEConomicosEvaluadosGestorController.PopupCEConomicosEvaluadosGestorValidateCopy("editar", dbCEconomico, cEconomico, duracion);
		}


		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: "
					+ "fap/PCEconomico/PopupCEConomicosEvaluadosGestor.html"
					+ " , intentada con éxito");
			dbCEconomico.save();

		} else {
			flash();
			log.info("Acción Editar de página: "
					+ "fap/PCEconomico/PopupCEConomicosEvaluadosGestor.html"
					+ " , intentada sin éxito (Problemas de Validación)");
		}

		PopupCEConomicosEvaluadosGestorController.editarRender(idSolicitud, idCEconomico, duracion);
	}

	@Util
	public static void PopupCEConomicosEvaluadosGestorValidateCopy(String accion, CEconomico dbCEconomico, CEconomico cEconomico, Integer duracion) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("cEconomico", cEconomico);
		if(!dbCEconomico.tipo.tipoOtro){ //TipoOtro tienen el array vacio
			for (int i=0; i<=duracion; i++){
				dbCEconomico.valores.get(i).valorConcedido = cEconomico.valores.get(i).valorConcedido != null ? cEconomico.valores.get(i).valorConcedido : 0.0;
				dbCEconomico.valores.get(i).valorPropuesto = cEconomico.valores.get(i).valorPropuesto != null ? cEconomico.valores.get(i).valorPropuesto : 0.0;
			}
		}
	}

	@Util
	public static String getAccion() {

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		return secure.getPrimeraAccion("solicitudes", ids, null);

	}

	@Util
	public static void editarRender(Long idSolicitud, Long idCEconomico, Integer duracion) {
		if (!Messages.hasMessages()) {
			renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
			Messages.keep();
			redirect("popups.PopupCEConomicosEvaluadosGestorController.index", "editar", idSolicitud, idCEconomico, duracion);
		}
		Messages.keep();
		redirect("popups.PopupCEConomicosEvaluadosGestorController.index", "editar", idSolicitud, idCEconomico, duracion);
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
			cEconomico = CEconomico.find("select cEconomico from Solicitud solicitud join solicitud.ceconomicos cEconomico where solicitud.id=? and cEconomico.id=?",idSolicitud, idCEconomico).first();
			if (cEconomico == null) {
				Messages.fatal("Error al recuperar CEconomico");
			}
		}
		return cEconomico;
	}

	@Before
	static void beforeMethod() {
		renderArgs.put("controllerName", "PopupCEConomicosEvaluadosGestorController");
	}
	
	@Util
	private static void flash(){
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		String param = "cEconomico";
		for (int i = 0; i < tipoEvaluacion.duracion; i++){
			Messages.setFlash(param + ".valores["+i+"].valorPropuesto", params.get(param + ".valores["+i+"].valorPropuesto", String.class));
			Messages.setFlash(param + ".valores["+i+"].valorConcedido", params.get(param + ".valores["+i+"].valorConcedido", String.class));
		}
		Messages.setFlash(param + ".comentariosAdministracion", params.get(param + ".comentariosAdministracion", String.class));
		Messages.setFlash(param + ".comentariosSolicitante", params.get(param + ".comentariosSolicitante", String.class));
	}
	
	public static void tablaCEconomicosManuales(Long idCEconomico) {
		CEconomico ceconomico = CEconomico.findById(idCEconomico);
		
		java.util.List<CEconomicosManuales> rows = ceconomico.otros;

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<CEconomicosManuales> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<CEconomicosManuales> response = new tables.TableRenderResponse<CEconomicosManuales>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("tipo.jerarquia", "tipo.nombre", "id"));
	}
	
	public static void refreshFlash(String campo, String valor) {
		Messages.setFlash(campo, valor);
		log.info(campo.replace("sManuales", "") + " set now to " + valor);
	}
}
		