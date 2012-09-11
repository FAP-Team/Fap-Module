package controllers.popups;

import play.mvc.Finally;
import play.mvc.Util;
import validation.CustomValidation;
import messages.Messages;
import models.CEconomico;
import models.CEconomicosManuales;
import models.SolicitudGenerica;
import models.TipoCEconomico;
import models.TipoEvaluacion;
import controllers.PaginaCEconomicoManualController;
import controllers.gen.popups.PopupCEconomicoManualControllerGen;

public class PopupCEconomicoManualController extends PopupCEconomicoManualControllerGen {
	
	public static void index(String accion, Long idSolicitud, Long idCEconomico, Long idCEconomicosManuales) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/PaginaCEconomicoManual/PaginaCEconomicoManual.html");
		}

		SolicitudGenerica solicitud = PaginaCEconomicoManualController.getSolicitudGenerica(idSolicitud);
		CEconomico cEconomico = PaginaCEconomicoManualController.getCEconomico(idSolicitud, idCEconomico);

		CEconomicosManuales cEconomicosManuales = null;
		if ("crear".equals(accion)) {
			cEconomicosManuales = PaginaCEconomicoManualController.getCEconomicosManuales();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				cEconomicosManuales.save();
				idCEconomicosManuales = cEconomicosManuales.id;
				cEconomico.otros.add(cEconomicosManuales);
				cEconomico.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			cEconomicosManuales = PaginaCEconomicoManualController.getCEconomicosManuales(idCEconomico, idCEconomicosManuales);

		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		Integer duracion = tipoEvaluacion.duracion-1;
		log.info("Visitando página: " + "gen/PaginaCEconomicoManual/PaginaCEconomicoManual.html");
		renderTemplate("gen/PaginaCEconomicoManual/PaginaCEconomicoManual.html", accion, idSolicitud, idCEconomico, idCEconomicosManuales, solicitud, cEconomico, cEconomicosManuales);
	}

	@Util
	public static CEconomicosManuales getCEconomicosManuales() {
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		return new CEconomicosManuales(tipoEvaluacion.duracion);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(Long idCEconomico, Long idCEconomicosManuales, CEconomicosManuales cEconomicosManuales) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}

		if (!Messages.hasErrors()) {
			PopupCEconomicoManualController.editarValidateRules();
		}
		if (!Messages.hasErrors()) {
			CustomValidation.clearValidadas();
			CustomValidation.required("cEconomicosManuales.tipo.nombre", cEconomicosManuales.tipo.nombre);
		}
		if (!Messages.hasErrors()) {
			CEconomicosManuales dbCEconomicosManuales = CEconomicosManuales.findById(idCEconomicosManuales);
			dbCEconomicosManuales.tipo.nombre = cEconomicosManuales.tipo.nombre;
			dbCEconomicosManuales.tipo.descripcion = cEconomicosManuales.tipo.descripcion;
			dbCEconomicosManuales.valores=cEconomicosManuales.valores;
			dbCEconomicosManuales.save();
			log.info("Acción Editar de página: " + "fap/Baremacion/PopupCEconomicoManual.html" + " , intentada con éxito");
		} else {
			flash();
			log.info("Acción Editar de página: " + "fap/Baremacion/PopupCEconomicoManual.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		PopupCEconomicoManualController.editarRender(idCEconomico, idCEconomicosManuales);
	}
	
	@Util
	public static void crearRender(Long idCEconomico, Long idCEconomicosManuales) {
		if (!Messages.hasMessages()) {
			renderJSON(utils.RestResponse.ok("Registro creado correctamente"));
		}
		Messages.keep();
		redirect("popups.PopupCEconomicoManualController.index", "crear", idCEconomico, idCEconomicosManuales);
	}
	
	@Util
	public static void editarRender(Long idCEconomico, Long idCEconomicosManuales) {
		if (!Messages.hasMessages()) {
			renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
			Messages.keep();
			redirect("PaginaCEconomicosController.index", controllers.PaginaCEconomicosController.getAccion(), idCEconomico, idCEconomicosManuales);
		}
		Messages.keep();
		redirect("popups.PopupCEconomicoManualController.index", "editar", idCEconomico, idCEconomicosManuales);
	}
	
	public static void crear(Long idCEconomico, Long idCEconomicosManuales, CEconomicosManuales cEconomicosManuales) {

		if (idCEconomicosManuales != null)
			PopupCEconomicoManualController.editar(idCEconomico, idCEconomicosManuales);
		else {
			idCEconomicosManuales = PopupCEconomicoManualController.crearLogica(idCEconomico, cEconomicosManuales);
			PopupCEconomicoManualController.crearRender(idCEconomico, idCEconomicosManuales);
		}
	}

	@Util
	public static Long crearLogica(Long idCEconomico, CEconomicosManuales cEconomicosManuales) {
		checkAuthenticity();
		if (!permiso("crear")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		CEconomicosManuales dbCEconomicosManuales = PopupCEconomicoManualController.getCEconomicosManuales();
		CEconomico dbCEconomico = PopupCEconomicoManualController.getCEconomico(idCEconomico);

		if (!Messages.hasErrors()) {
			PopupCEconomicoManualController.crearValidateRules();
			CustomValidation.clearValidadas();
			CustomValidation.required("cEconomicosManuales.tipo.nombre", cEconomicosManuales.tipo.nombre);
		}
		Long idCEconomicosManuales = null;
		if (!Messages.hasErrors()) {
			// Creamos el Tipo ConceptoEconomico por defecto (el solicitante solo podrá cambiar el atributo nombre)
			TipoCEconomico tipoCEconomico = new TipoCEconomico();
			tipoCEconomico.clase="manual";
			tipoCEconomico.comentariosAdministracion=true;
			tipoCEconomico.comentariosSolicitante=true;
			tipoCEconomico.descripcion=cEconomicosManuales.tipo.descripcion;
			tipoCEconomico.nombre=cEconomicosManuales.tipo.nombre;
			tipoCEconomico.instrucciones=null; //"Instrucciones";
			tipoCEconomico.tipoOtro=false;
			tipoCEconomico.jerarquia=dbCEconomico.tipo.jerarquia+"."+(dbCEconomico.otros.size()+1);
			tipoCEconomico.save();
			TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
			tipoEvaluacion.ceconomicos.add(tipoCEconomico);
			tipoEvaluacion.save();
			
			dbCEconomicosManuales.tipo = tipoCEconomico;
			dbCEconomicosManuales.valores=cEconomicosManuales.valores;
			dbCEconomicosManuales.save();
			idCEconomicosManuales = dbCEconomicosManuales.id;
			dbCEconomico.otros.add(dbCEconomicosManuales);
			
			dbCEconomico.save();

			log.info("Acción Crear de página: " + "gen/popups/PopupCEconomicoManual.html" + " , intentada con éxito");
		} else {
			flash();
			log.info("Acción Crear de página: " + "gen/popups/PopupCEconomicoManual.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		return idCEconomicosManuales;
	}
	
	@Util
	private static void flash(){
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		String param = "cEconomicosManuales";
		System.out.println(params);
		for (int i = 0; i < tipoEvaluacion.duracion; i++){
			Messages.setFlash(param + ".valores["+i+"].valorEstimado", params.get(param + ".valores["+i+"].valorEstimado", String.class));
			Messages.setFlash(param + ".valores["+i+"].valorSolicitado", params.get(param + ".valores["+i+"].valorSolicitado", String.class));
			Messages.setFlash(param + ".valores["+i+"].valorPropuesto", params.get(param + ".valores["+i+"].valorPropuesto", String.class));
			Messages.setFlash(param + ".valores["+i+"].valorConcedido", params.get(param + ".valores["+i+"].valorConcedido", String.class));
		}
		Messages.setFlash(param + ".comentariosAdministracion", params.get(param + ".comentariosAdministracion", String.class));
		Messages.setFlash(param + ".comentariosSolicitante", params.get(param + ".comentariosSolicitante", String.class));
	}
	
	@Finally(only="index")
	public static void end(){
		Messages.deleteFlash();
	}
}
