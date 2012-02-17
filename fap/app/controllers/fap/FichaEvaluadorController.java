package controllers.fap;


import java.util.HashMap;

import static play.modules.pdf.PDF.renderPDF;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import messages.Messages;
import models.CEconomico;
import models.Criterio;
import models.CriterioListaValores;
import models.Documento;
import models.Evaluacion;
import models.TipoCEconomico;
import models.TipoCriterio;
import models.TipoEvaluacion;
import play.data.validation.Validation;
import play.db.jpa.JPABase;
import play.modules.pdf.PDF.Options;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Router;
import play.mvc.Router.ActionDefinition;
import play.mvc.Scope.Flash;
import play.mvc.Scope.Params;
import play.mvc.With;
import properties.FapProperties;
import reports.Report;
import security.Secure;
import services.BaremacionService;

@With({SecureController.class, AgenteController.class})
public class FichaEvaluadorController extends Controller {
	
	@Inject
	protected static Secure secure;

	@Finally(only="index")
	public static void end(){
		Messages.deleteFlash();
	}
	
	public static void index(long idEvaluacion){
		if(secure.checkGrafico("evaluacion", "visible", "leer", null, null)){
			Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
			notFoundIfNull(evaluacion);
			String expedienteUrl = redirectToFirstPage(evaluacion.solicitud.id);
			List<Documento> documentos = evaluacion.getDocumentosAccesibles();
			renderTemplate("fap/Baremacion/fichaEvaluador.html", evaluacion, documentos, expedienteUrl);
		}else{
			forbidden();
		}
	}

	private static String redirectToFirstPage(Long idSolicitud) {
		//Url Primera paǵina de la solicitud
		String firstPage = FapProperties.get("fap.app.firstPage"); 
		if(firstPage == null){
			play.Logger.error("No está definida la property fap.app.firstPage que identifica la primera página.");
			Messages.fatal("Se ha producido un error");
		}
		String expedienteUrl = Router.reverse(firstPage + "Controller.index").add("idSolicitud", idSolicitud).url;
		return expedienteUrl;
	}
	
	public static void generaPDF(Long idEvaluacion){
		Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
		if(evaluacion == null){
			notFound("Evaluación no encontrada");
		}
		try {
			new Report("app/views/reports/baremacion/Borrador.html").header("reports/header.html").footer("reports/footer-borrador.html").renderResponse(evaluacion);
		} catch (Exception e) {
			play.Logger.error("Error al generar el borrador del documento %s", e.getMessage());
			Messages.error("Error al generar el borrador del documento");
		}
	}

	public static void save(){
		if(secure.checkGrafico("evaluacion", "none", "editar", null, null)){
			boolean actionSave = params.get("save") != null;
			boolean actionPdf = params.get("pdf") != null;
			boolean actionEnd = params.get("end") != null;
			if(!(actionSave || actionPdf || actionEnd)){
				//No se específico una acción
				notFound("Acción no especificada");
			}
			Evaluacion evaluacion = Evaluacion.findById(params.get("evaluacion.id", Long.class));
			if(evaluacion == null){
				notFound("Evaluación no encontrada");
			}
			
			//Comentarios
			if(evaluacion.tipo.comentariosAdministracion){
				evaluacion.comentariosAdministracion = params.get("evaluacion.comentariosAdministracion");
			}
			
			if(evaluacion.tipo.comentariosSolicitante){
				evaluacion.comentariosSolicitante = params.get("evaluacion.comentariosSolicitante");
			}
				
			//Criterios de evaluacion
			for(Criterio criterio : evaluacion.criterios){
				String param = "criterio[" + criterio.id + "]";
	
				if(criterio.tipo.claseCriterio.equals("manual")){
					String key = param + ".valor";
					Double valor = params.get(key, Double.class);
					
					// Únicamente valida cuando se va a finalizar
					// la verificación
					if(actionEnd){
						validation.required(key, valor);
						//TODO validaciones de tamaño máximo
					}
					
					criterio.valor = valor;
				}else if(criterio.tipo.claseCriterio.equals("automod")){
					//TODO criterio automático modificable
				}
				
				//Comentarios
				if(criterio.tipo.comentariosAdministracion){				
					criterio.comentariosAdministracion = params.get(param + ".comentariosAdministracion");
				}
				
				if(criterio.tipo.comentariosSolicitante){
					criterio.comentariosSolicitante = params.get(param + ".comentariosSolicitante");
				}
			}
	
			for(CEconomico ceconomico : evaluacion.ceconomicos){
				String param = "ceconomico[" + ceconomico.id + "]";

				String key = param + ".valorPropuesto";
				Double valor = params.get(key, Double.class);
				if(actionEnd){
					validation.required(key, valor);
				}
				
//				ceconomico.valorPropuesto = valor;
	
				//Comentarios
				if(ceconomico.tipo.comentariosAdministracion){				
					ceconomico.comentariosAdministracion = params.get(param + ".comentariosAdministracion");
				}
				
				if(ceconomico.tipo.comentariosSolicitante){
					ceconomico.comentariosSolicitante = params.get(param + ".comentariosSolicitante");
				}			
			}
			BaremacionService.calcularTotales(evaluacion);
			evaluacion.save();
			if(validation.hasErrors()){
				flash(evaluacion);
			}
			
			if(actionSave || actionEnd){
				if(actionEnd && !validation.hasErrors()){
					evaluacion.estado = "evaluada";
					evaluacion.save();
					Messages.ok("La evaluación del expediente " + evaluacion.solicitud.expedienteAed.idAed + " finalizó correctamente");
					ConsultarEvaluacionesController.index();
				}
				
				if(actionSave){
					Messages.ok("La evaluación del expediente " + evaluacion.solicitud.expedienteAed.idAed + " se guardó correctamente");
				}
				
				Messages.keep();
				index(evaluacion.id);
			}
		}else{
			forbidden();
		}
	}
	
	
	private static void flash(Evaluacion evaluacion){
		Messages.setFlash("evaluacion.id", evaluacion.id);
		Messages.setFlash("evaluacion.comentariosAdministracion", evaluacion.comentariosAdministracion);
		Messages.setFlash("evaluacion.comentariosSolicitante", evaluacion.comentariosSolicitante);
		
		for(Criterio c : evaluacion.criterios){
			String param = "criterio[" + c.id + "]";
			Messages.setFlash(param + ".valor", c.valor);
			Messages.setFlash(param + ".comentariosAdministracion", c.comentariosAdministracion);
			Messages.setFlash(param + ".comentariosSolicitante", c.comentariosSolicitante);
		}
		
		for(CEconomico ce : evaluacion.ceconomicos){
			String param = "ceconomico[" + ce.id + "]";
//			Messages.setFlash(param + ".valorEstimado", ce.valorEstimado);
			Messages.setFlash(param + ".comentariosAdministracion", ce.comentariosAdministracion);
			Messages.setFlash(param + ".comentariosSolicitante", ce.comentariosSolicitante);
		}
	}
		
}
