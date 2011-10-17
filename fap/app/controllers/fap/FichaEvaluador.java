package controllers.fap;

import java.util.List;

import messages.Messages;
import models.CEconomico;
import models.Criterio;
import models.CriterioListaValores;
import models.Evaluacion;
import models.TipoCriterio;
import models.TipoEvaluacion;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.Scope.Flash;
import play.mvc.With;

@With(SecureController.class)
public class FichaEvaluador extends Controller {

	public static void fichaEvaluador(){
		
		play.Logger.info("FLASH %s", flash);
		play.Logger.info("FLASH[34] %s", flash.get("criterio[34].valor"));
		
		for(play.data.validation.Error e : Validation.errors()){
			play.Logger.info("Key %s, Message %s", e.getKey(), e.message());
		}
		
		if(Validation.hasErrors()){
			play.Logger.info("Validation has errors");
		}
		
		if(Evaluacion.count() == 0){
			initEvaluacion();
		}
		
		Evaluacion evaluacion = Evaluacion.all().first();
		render(evaluacion);
	}

	public static void save(){
		
		Evaluacion evaluacion = Evaluacion.findById(params.get("evaluacion.id", Long.class));
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
				
				//Validaciones
				validation.required(key, valor);
				//TODO validaciones de tamaño máximo
				
				
				criterio.valor = valor;
			}else if(criterio.tipo.claseCriterio.equals("automod")){
				//TODO criterio automático modificable
			}
			
			//Comentarios
			if(criterio.tipo.comentariosAdministracion){
				criterio.comentariosAdministracion = params.get(param + "comentariosAdministracion");
			}
			
			if(criterio.tipo.comentariosSolicitante){
				criterio.comentariosSolicitante = params.get(param + "comentariosSolicitante");
			}
		}

		if(validation.hasErrors()){
			play.Logger.info("Errores!");
			Messages.keep();
		}else{
			//Calcular totales
			evaluacion.save();
		}
		
		fichaEvaluador();
	}
	
	private static void addLValores(TipoCriterio tc, Double valor, String descripcion){
		CriterioListaValores criterioListaValores = new CriterioListaValores();
		criterioListaValores.valor = valor;
		criterioListaValores.descripcion = descripcion;
		tc.listaValores.add(criterioListaValores);
	}
	
	private static void initEvaluacion() {
		TipoCriterio t1 = new TipoCriterio();
		t1.nombre = "Calidad y viabilidad";
		t1.claseCriterio = "auto";
		t1.jerarquia = "1";
		t1.tipoValor = "cantidad";
		
		TipoCriterio t1_1 = new TipoCriterio();
		t1_1.nombre = "Duracion";
		t1_1.claseCriterio = "manual";
		t1_1.jerarquia = "1.1";
		t1_1.tipoValor = "lista";
		addLValores(t1_1, 10D, "Si");
		addLValores(t1_1, 0D, "No");
		
		TipoCriterio t1_2 = new TipoCriterio();
		t1_2.nombre = "Sustancia de la solicitud";
		t1_2.claseCriterio = "auto";
		t1_2.jerarquia = "1.2";
		t1_2.tipoValor = "cantidad";
		
		TipoCriterio t1_2_1 = new TipoCriterio();
		t1_2_1.nombre = "Grado de definición del proyecto";
		t1_2_1.claseCriterio = "manual";
		t1_2_1.jerarquia = "1.2.1";
		t1_2_1.tipoValor = "lista";
		addLValores(t1_2_1, 8D, "Alto");
		addLValores(t1_2_1, 5D, "Medio");
		addLValores(t1_2_1, 2D, "Bajo");
		addLValores(t1_2_1, 0D, "Nulo");
		
		TipoCriterio t1_2_2 = new TipoCriterio();
		t1_2_2.nombre = "Nivel del grupo de investigación";
		t1_2_2.claseCriterio = "manual";
		t1_2_2.jerarquia = "1.2.2";
		t1_2_2.tipoValor = "lista";
		addLValores(t1_2_2, 8D, "Alto");
		addLValores(t1_2_2, 5D, "Medio");
		addLValores(t1_2_2, 2D, "Bajo");
		
		TipoCriterio t1_2_3 = new TipoCriterio();
		t1_2_3.nombre = "Carácter del proyecto";
		t1_2_3.claseCriterio = "manual";
		t1_2_3.jerarquia = "1.2.3";
		t1_2_3.tipoValor  = "lista";
		addLValores(t1_2_3, 8D, "Alto. Spinoff o nuevo proyecto");
		addLValores(t1_2_3, 5D, "Medio. Proyecto iniciado");
		addLValores(t1_2_3, 2D, "Bajo. Proyecto iniciado y subvencionado");
		
		TipoCriterio t1_2_4 = new TipoCriterio();
		t1_2_4.nombre = "Coherencia";
		t1_2_4.claseCriterio = "manual";
		t1_2_4.jerarquia = "1.2.4";
		t1_2_4.tipoValor = "lista";
		addLValores(t1_2_4, 8D, "Si");
		addLValores(t1_2_4, 0D, "No");
		
		
		TipoCriterio t1_2_5 = new TipoCriterio();
		t1_2_5.nombre = "Existencia de base endógena";
		t1_2_5.claseCriterio = "manual";
		t1_2_5.jerarquia = "1.2.5";
		t1_2_5.tipoValor = "lista";
		addLValores(t1_2_5, 8D, "Si");
		addLValores(t1_2_5, 0D, "No");
		
		TipoCriterio t1_3 =  new TipoCriterio();
		t1_3.nombre = "Cooperación";
		t1_3.claseCriterio = "manual";
		t1_3.jerarquia = "1.3";
		t1_3.tipoValor = "lista";
		addLValores(t1_3, 5D, "Si");
		addLValores(t1_3, 0D, "No");
		
		
		TipoEvaluacion tEvaluacion = new TipoEvaluacion();
		tEvaluacion.criterios.add(t1);
		tEvaluacion.criterios.add(t1_1);
		tEvaluacion.criterios.add(t1_2);
		tEvaluacion.criterios.add(t1_2_1);
		tEvaluacion.criterios.add(t1_2_3);
		tEvaluacion.criterios.add(t1_2_4);
		tEvaluacion.criterios.add(t1_2_5);
		tEvaluacion.criterios.add(t1_3);
		tEvaluacion.comentariosAdministracion = true;
		tEvaluacion.comentariosSolicitante = true;
		
		//Inicializa una evaluación a partir de un tipo
		Evaluacion evaluacion = Evaluacion.init(tEvaluacion);
		evaluacion.save();
	}
	
}
