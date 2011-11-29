package controllers.fap;

import java.util.List;

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
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Scope.Flash;
import play.mvc.Scope.Params;
import play.mvc.With;
import secure.PermissionFap;
import services.BaremacionService;

public class FichaEvaluadorController extends Controller {
	
	@Finally(only="index")
	public static void removeFlash(){
		Messages.deleteFlash();
	}
	
	public static void index(long idEvaluacion){
		if(PermissionFap.evaluacion("read", null, null)){
			Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
			notFoundIfNull(evaluacion);
			List<Documento> documentos = evaluacion.getDocumentosAccesibles();
			renderTemplate("fap/Baremacion/fichaEvaluador.html", evaluacion, documentos);
		}else{
			forbidden();
		}
	}
	
	public static void save(){
		if(PermissionFap.evaluacion("update", null, null)){
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
				ceconomico.valorEstimado = params.get(param + ".valorEstimado", Double.class);
	
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
				Validation.keep();
			}
			
			if(actionSave || actionEnd){
				if(actionEnd && !validation.hasErrors()){
					Messages.ok("La verificación de la solicitud " + evaluacion.solicitud.id + " finalizó correctamente");
				}
				index(evaluacion.id);
			}else if(actionPdf){	
				renderText("renderizar PDF!");			
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
			Messages.setFlash(param + ".valorEstimado", ce.valorEstimado);
			Messages.setFlash(param + ".comentariosAdministracion", ce.comentariosAdministracion);
			Messages.setFlash(param + ".comentariosSolicitante", ce.comentariosSolicitante);
		}
	}
		
}
