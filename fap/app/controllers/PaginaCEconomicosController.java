
package controllers;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import messages.Messages;
import messages.Messages.MessageType;
import models.CEconomico;
import models.CEconomicoSolicitanteManual;
import models.CEconomicosManuales;
import models.CEconomicosSolicitante;
import models.Criterio;
import models.Evaluacion;
import models.SolicitudGenerica;
import models.TipoEvaluacion;
import models.ValoresCEconomico;

import org.apache.log4j.Logger;

import controllers.fap.AgenteController;
import controllers.fap.GenericController;
import controllers.fap.SecureController;
import controllers.gen.PaginaCEconomicosControllerGen;

import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Util;
import play.mvc.With;
import security.Accion;
import security.Secure;
import utils.BaremacionUtils;
import validation.CustomValidation;

@With({SecureController.class, AgenteController.class})
public class PaginaCEconomicosController extends PaginaCEconomicosControllerGen{
	
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
			renderTemplate("fap/PaginaCEconomicos/PaginaCEconomicos.html");
		}
		
		SolicitudGenerica solicitud = PaginaCEconomicosController.getSolicitudGenerica(idSolicitud);

		CEconomico cEconomico = null;
		if ("crear".equals(accion))
			cEconomico = PaginaCEconomicosController.getCEconomico();
		else if (!"borrado".equals(accion))
			cEconomico = PaginaCEconomicosController.getCEconomico(idSolicitud, idCEconomico);

		if (cEconomico.tipo.tipoOtro)
			calcularValoresAuto(cEconomico);
		
		log.info("Visitando página: " + "fap/PaginaCEconomicos/PaginaCEconomicos.html");
		renderTemplate("fap/PaginaCEconomicos/PaginaCEconomicos.html", accion, idSolicitud, idCEconomico, solicitud, cEconomico, duracion);
	}
	
	private static void calcularValoresAuto(CEconomico cEconomico){
		for (ValoresCEconomico valor: cEconomico.valores){
			valor.valorSolicitado = sumarValoresHijosOtro(cEconomico.otros, valor.anio);
		}
		cEconomico.save();
	}
	
	private static Double sumarValoresHijosOtro(List<CEconomicosManuales> listaHijosOtro, Integer anio){
		Double suma=0.0;
		for (CEconomicosManuales cEconomicoManual: listaHijosOtro){
			if (!cEconomicoManual.valores.isEmpty())
				suma += cEconomicoManual.valores.get(anio).valorSolicitado;
		}
		return suma;
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(Long idSolicitud, Long idCEconomico, CEconomico cEconomico, Integer duracion) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		CEconomico dbCEconomico = PaginaCEconomicosController.getCEconomico(idSolicitud, idCEconomico);

		PaginaCEconomicosController.PaginaCEconomicosBindReferences(cEconomico);

		if (!Messages.hasErrors()) {
			PaginaCEconomicosController.PaginaCEconomicosValidateCopy("editar", dbCEconomico, cEconomico);
			SolicitudGenerica solicitud = PaginaCEconomicosController.getSolicitudGenerica(idSolicitud);
			BaremacionUtils.calcularTotales(solicitud);
		}

		if (!Messages.hasErrors()) {
			PaginaCEconomicosController.editarValidateRules(dbCEconomico, cEconomico);
		}
		if (!Messages.hasErrors()) {		
			dbCEconomico.save();
			log.info("Acción Editar de página: " + "fap/PaginaCEconomicos/PaginaCEconomicos.html" + " , intentada con éxito");
		} else {
			flash(dbCEconomico);
			log.info("Acción Editar de página: " + "fap/PaginaCEconomicos/PaginaCEconomicos.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		PaginaCEconomicosController.editarRender(idSolicitud, idCEconomico, duracion);
	}

	@Util
	public static void editarRender(Long idSolicitud, Long idCEconomico, Integer duracion) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("PCEconomicosController.index", "editar", idSolicitud);
		}
		Messages.keep();
		redirect("PaginaCEconomicosController.index", "editar", idSolicitud, idCEconomico, duracion);
	}
	
	@Util
	public static void PaginaCEconomicosValidateCopy(String accion, CEconomico dbCEconomico, CEconomico cEconomico) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("cEconomico.tipo", cEconomico.tipo);
		CustomValidation.valid("cEconomico", cEconomico);
		if (!Messages.hasErrors()){
			if (!dbCEconomico.tipo.tipoOtro){
				int anios=0;
				for (ValoresCEconomico valor: dbCEconomico.valores){
					valor.valorSolicitado = cEconomico.valores.get(anios++).valorSolicitado;
				}
			} else {
				CustomValidation.required("cEconomico.tipo.nombre", cEconomico.tipo.nombre);
				if (!Messages.hasErrors())
					dbCEconomico.tipo.nombre = cEconomico.tipo.nombre;
			}
		}
		if (!Messages.hasErrors())
			dbCEconomico.save();
	}
	
	@Util
	private static void flash(CEconomico dbCEconomico){
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		String param = "cEconomico";
		if (dbCEconomico.tipo.tipoOtro){
			for (int i = 0; i < tipoEvaluacion.duracion; i++){
				Messages.setFlash(param + ".valores["+i+"].valorEstimado", dbCEconomico.valores.get(i).valorEstimado);
				Messages.setFlash(param + ".valores["+i+"].valorSolicitado", dbCEconomico.valores.get(i).valorSolicitado);
				Messages.setFlash(param + ".valores["+i+"].valorPropuesto", dbCEconomico.valores.get(i).valorPropuesto);
				Messages.setFlash(param + ".valores["+i+"].valorConcedido", dbCEconomico.valores.get(i).valorConcedido);
			}
			Messages.setFlash(param + ".comentariosAdministracion", dbCEconomico.comentariosAdministracion);
			Messages.setFlash(param + ".comentariosSolicitante", dbCEconomico.comentariosSolicitante);
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
}
		