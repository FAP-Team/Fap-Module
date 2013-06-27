package controllers;

import java.util.List;
import java.util.Map;

import messages.Messages;
import models.*;
import play.Logger;
import play.mvc.Finally;
import play.mvc.Util;
import security.Secure;
import tables.TableRecord;
import utils.BaremacionUtils;
import validation.CustomValidation;

import com.google.inject.Inject;

import controllers.gen.PaginaCEconomicosControllerGen;

public class PaginaCEconomicosController extends PaginaCEconomicosControllerGen {
	
	@Inject
	protected static Secure secure;
	
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

		//if (cEconomico.tipo.tipoOtro)
		//	calcularValoresAuto(cEconomico);
		
		log.info("Visitando página: " + "fap/PaginaCEconomicos/PaginaCEconomicos.html");
		
		renderTemplate("fap/PaginaCEconomicos/PaginaCEconomicos.html", accion, idSolicitud, idCEconomico, solicitud, cEconomico, duracion);
	}
	
	public static void calcularValoresAuto(CEconomico cEconomico){
		for (ValoresCEconomico valor: cEconomico.valores){
			valor.valorSolicitado = sumarValoresHijosOtro(cEconomico.otros, valor.anio);
		}
		cEconomico.save();
	}
	
	private static Double sumarValoresHijosOtro(List<CEconomicosManuales> listaHijosOtro, Integer anio){
		Double suma=0.0;
		for (CEconomicosManuales cEconomicoManual: listaHijosOtro){
			if (!cEconomicoManual.valores.isEmpty())
				if (cEconomicoManual.valores.get(anio).valorSolicitado != null)
					suma += cEconomicoManual.valores.get(anio).valorSolicitado;
		}
		return suma;
	}
	
	@Util
	public static void guardarValidateCopy(String accion, CEconomico dbCEconomico, CEconomico cEconomico) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("cEconomico.tipo", cEconomico.tipo);
		CustomValidation.valid("cEconomico", cEconomico);
		if (!Messages.hasErrors()){
			if (!dbCEconomico.tipo.tipoOtro){
				int anios=0;
				for (ValoresCEconomico valor: dbCEconomico.valores){
					if (cEconomico.valores.get(anios).valorSolicitado == null)
						valor.valorSolicitado = 0.0;
					else
						valor.valorSolicitado = cEconomico.valores.get(anios).valorSolicitado;
					anios++;
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
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void guardar(Long idSolicitud, Long idCEconomico, CEconomico cEconomico, String botonGuardar, Integer duracion) {
		checkAuthenticity();
		if (!permisoGuardar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		CEconomico dbCEconomico = PaginaCEconomicosController.getCEconomico(idSolicitud, idCEconomico);

		PaginaCEconomicosController.guardarBindReferences(cEconomico);

		if (!Messages.hasErrors()) {
			PaginaCEconomicosController.guardarValidateCopy("editar", dbCEconomico, cEconomico);
			SolicitudGenerica solicitud = PaginaCEconomicosController.getSolicitudGenerica(idSolicitud);
			BaremacionUtils.calcularTotales(solicitud);
		}

		if (!Messages.hasErrors()) {
			PaginaCEconomicosController.guardarValidateRules(dbCEconomico, cEconomico);
		}
		if (!Messages.hasErrors()) {		
			dbCEconomico.save();
			log.info("Acción Editar de página: " + "fap/PaginaCEconomicos/PaginaCEconomicos.html" + " , intentada con éxito");
		} else {
			flash(cEconomico);
			log.info("Acción Editar de página: " + "fap/PaginaCEconomicos/PaginaCEconomicos.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		PaginaCEconomicosController.guardarRender(idSolicitud, idCEconomico, duracion);
	}
	
	@Util
	public static void guardarRender(Long idSolicitud, Long idCEconomico, Integer duracion) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("PCEconomicosController.index", "editar", idSolicitud);
		}
		Messages.keep();
		redirect("PaginaCEconomicosController.index", "editar", idSolicitud, idCEconomico, duracion);
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
	
	public static void tablaceconomicosManuales(Long idCEconomico) {

		java.util.List<CEconomicosManuales> rows = CEconomicosManuales.find("select cEconomicosManuales from CEconomico cEconomico join cEconomico.otros cEconomicosManuales where cEconomico.id=?", idCEconomico).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		
		List<CEconomicosManuales> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<CEconomicosManuales> response = new tables.TableRenderResponse<CEconomicosManuales>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		
		for (TableRecord<CEconomicosManuales> filaCEconomico: response.rows){
			if (tipoEvaluacion.estado.equals("iniciada"))
				filaCEconomico.permisoEditar = false;
			else if (!permiso("editar")){
				filaCEconomico.permisoEditar = false;
				filaCEconomico.permisoLeer = true;
			}
		}
		
		renderJSON(response.toJSON("tipo.jerarquia", "tipo.nombre", "id", "valores.valorSolicitado_formatFapTabla"));
	}
}
