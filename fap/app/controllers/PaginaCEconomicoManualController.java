package controllers;

import java.util.ArrayList;
import java.util.Map;

import play.mvc.Util;
import utils.ModelUtils;
import validation.CustomValidation;
import messages.Messages;
import models.CEconomico;
import models.CEconomicosManuales;
import models.SolicitudGenerica;
import models.TipoCEconomico;
import models.TipoEvaluacion;
import models.ValoresCEconomico;
import controllers.gen.PaginaCEconomicoManualControllerGen;

public class PaginaCEconomicoManualController extends PaginaCEconomicoManualControllerGen {
	
	public static void index(String accion, Long idSolicitud, Long idCEconomico, Long idCEconomicosManuales) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/PaginaCEconomicoManual/PaginaCEconomicoManual.html");
		}

		SolicitudGenerica solicitud = PaginaCEconomicoManualController.getSolicitudGenerica(idSolicitud);
		CEconomico cEconomico = PaginaCEconomicoManualController.getCEconomico(idSolicitud, idCEconomico);

		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		Integer duracion = tipoEvaluacion.duracion-1;
		
		CEconomicosManuales cEconomicosManuales = null;
		if ("crear".equals(accion)) {
			cEconomicosManuales = PaginaCEconomicoManualController.getCEconomicosManuales();
			cEconomicosManuales.valores = new ArrayList<ValoresCEconomico>();
			for (int i=0; i <= duracion; i++) {
				ValoresCEconomico vCE = new ValoresCEconomico(i);
				cEconomicosManuales.valores.add(vCE);
			}
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				cEconomicosManuales.save();
				idCEconomicosManuales = cEconomicosManuales.id;
				cEconomico.otros.add(cEconomicosManuales);
				cEconomico.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion)){
				cEconomicosManuales = PaginaCEconomicoManualController.getCEconomicosManuales(idCEconomico, idCEconomicosManuales);
				if (cEconomicosManuales == null){
					cEconomicosManuales = getFlashCEconomicosManuales();
					accion= "crear";
					Messages.clear();
				}
		}

		log.info("Visitando página: " + "fap/PaginaCEconomicoManual/PaginaCEconomicoManual.html");
		renderTemplate("fap/PaginaCEconomicoManual/PaginaCEconomicoManual.html", accion, idSolicitud, idCEconomico, idCEconomicosManuales, solicitud, cEconomico, cEconomicosManuales, duracion);
		
	}
	
	@Util
	public static void guardarPCEValidateCopy(String accion, CEconomicosManuales dbCEconomicosManuales, CEconomicosManuales cEconomicosManuales) {
		CustomValidation.clearValidadas();
		if (secure.checkGrafico("iniciarBaremacion", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("cEconomicosManuales.tipo", cEconomicosManuales.tipo);
			CustomValidation.valid("cEconomicosManuales", cEconomicosManuales);
			CustomValidation.required("cEconomicosManuales.tipo.nombre", cEconomicosManuales.tipo.nombre);
			dbCEconomicosManuales.tipo.nombre = cEconomicosManuales.tipo.nombre;
			dbCEconomicosManuales.tipo.descripcion = cEconomicosManuales.tipo.descripcion;
			if ((dbCEconomicosManuales.valores == null) || (dbCEconomicosManuales.valores.size() == 0))
				dbCEconomicosManuales.valores = cEconomicosManuales.valores;
			else {
				for (int i=0; i < cEconomicosManuales.valores.size(); i++) {
					dbCEconomicosManuales.valores.get(i).valorSolicitado = cEconomicosManuales.valores.get(i).valorSolicitado;
				}
			}
			//dbCEconomicosManuales.valores=cEconomicosManuales.valores;
		}

	}
	
	
	
	@Util
	public static void guardarPCERender(Long idSolicitud, Long idCEconomico, Long idCEconomicosManuales) {
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		Integer duracion = tipoEvaluacion.duracion-1;
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("PaginaCEconomicosController.index", controllers.PaginaCEconomicosController.getAccion(), idSolicitud, idCEconomico, duracion);
		}
		Messages.keep();
		redirect("PaginaCEconomicoManualController.index", "editar", idSolicitud, idCEconomico, idCEconomicosManuales);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void guardarPCE(Long idSolicitud, Long idCEconomico, Long idCEconomicosManuales, CEconomicosManuales cEconomicosManuales, String bGuardarPCE) {
		checkAuthenticity();
		if (!permisoGuardarPCE("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		CEconomico dbCEconomico = PaginaCEconomicoManualController.getCEconomico(idSolicitud, idCEconomico);
		CEconomicosManuales dbCEconomicosManuales;
		if (idCEconomicosManuales == null) {
			dbCEconomicosManuales = PaginaCEconomicoManualController.getCEconomicosManuales();
			CustomValidation.required("cEconomicosManuales.tipo.nombre", cEconomicosManuales.tipo.nombre);
			if (!Messages.hasErrors()){
				// Creamos el Tipo ConceptoEconomico por defecto (el solicitante solo podrá cambiar el atributo nombre)
				TipoCEconomico tipoCEconomico = new TipoCEconomico();
				tipoCEconomico.clase="manual";
				tipoCEconomico.comentariosAdministracion=true;
				tipoCEconomico.comentariosSolicitante=true;
				tipoCEconomico.descripcion=cEconomicosManuales.tipo.descripcion;
				tipoCEconomico.nombre=cEconomicosManuales.tipo.nombre;
				tipoCEconomico.instrucciones=null; //"Instrucciones";
				tipoCEconomico.tipoOtro=false;
				tipoCEconomico.creadoUsuario=true;
				tipoCEconomico.jerarquia=dbCEconomico.tipo.jerarquia+"."+(dbCEconomico.otros.size()+1);
				tipoCEconomico.save();
				TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
				tipoEvaluacion.ceconomicos.add(tipoCEconomico);
				tipoEvaluacion.save();
				
				dbCEconomicosManuales.tipo = tipoCEconomico;
				dbCEconomicosManuales.save();
				idCEconomicosManuales = dbCEconomicosManuales.id;
				dbCEconomico.otros.add(dbCEconomicosManuales);
			}
		} else {
			dbCEconomicosManuales = PaginaCEconomicoManualController.getCEconomicosManuales(idCEconomico, idCEconomicosManuales);
		}
		PaginaCEconomicoManualController.guardarPCEBindReferences(cEconomicosManuales);

		if (!Messages.hasErrors()) {

			
			PaginaCEconomicoManualController.guardarPCEValidateCopy("editar", dbCEconomicosManuales, cEconomicosManuales);

			

		}

		if (!Messages.hasErrors()) {
			PaginaCEconomicoManualController.guardarPCEValidateRules(dbCEconomicosManuales, cEconomicosManuales);
		}
		if (!Messages.hasErrors()) {
			SolicitudGenerica solicitud = PaginaCEconomicosController.getSolicitudGenerica(idSolicitud);
			Object miSavePages = ModelUtils.invokeMethodClass(SolicitudGenerica.class, solicitud, "getSavePages");
			ModelUtils.invokeMethodClass(miSavePages.getClass(), miSavePages, "setPaginaPCEconomicos", false);
			ModelUtils.invokeMethodClass(miSavePages.getClass(), miSavePages, "save");
			dbCEconomico.save();
			dbCEconomicosManuales.save();
			idCEconomicosManuales = dbCEconomicosManuales.id;
			log.info("Acción Editar de página: " + "gen/PaginaCEconomicoManual/PaginaCEconomicoManual.html" + " , intentada con éxito");
		} else{
			flash(cEconomicosManuales);
			log.info("Acción Editar de página: " + "gen/PaginaCEconomicoManual/PaginaCEconomicoManual.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		PaginaCEconomicoManualController.guardarPCERender(idSolicitud, idCEconomico, idCEconomicosManuales);
	}
	
	
	@Util
	public static CEconomicosManuales getCEconomicosManuales() {
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		return new CEconomicosManuales(tipoEvaluacion.duracion);
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
}
