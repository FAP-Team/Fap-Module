package baremacion;

import java.lang.reflect.Field;
import java.util.*;

import org.hibernate.collection.PersistentBag;

import models.CEconomico;
import models.Criterio;
import models.Evaluacion;
import models.TipoEvaluacion;
import models.CEconomicosManuales;

public class Evaluador {

	
	public static Evaluacion getEvaluacion(CEconomico ceconomico){
		Evaluacion evaluacion = Evaluacion.find("select evaluacion from Evaluacion evaluacion join evaluacion.ceconomicos ceconomico where ceconomico.id=?", ceconomico.id).first();
		return evaluacion;
	}
	
	public static Evaluacion getEvaluacion(Criterio criterio){
		Evaluacion evaluacion = Evaluacion.find("select evaluacion from Evaluacion evaluacion join evaluacion.criterios criterio where criterio.id=?", criterio.id).first();
		return evaluacion;
	}
	
	public static void evalDefault(Criterio criterio, List<Criterio> childs) {
		criterio.valor = sumatorio("valor", childs);
	}
	
	public static <T> void evalDefault(Evaluacion evaluacion, List<T> childs, String tipo){
		if (tipo.equals("Criterios"))
			evaluacion.totalCriterios = sumatorio("valor", childs);
		else if(tipo.equals("CEconomicos")){
			double sumaParcial = 0.0;
			TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
			for (int i = 0; i < tipoEvaluacion.duracion; i++){
				sumaParcial += sumatorio("valores",i,"valorPropuesto", childs);
			}
			evaluacion.inversionTotalAprobada = sumaParcial;
		}
	}

	public static void evalDefault(CEconomico ceconomico, List<CEconomico> childs) {
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		for (int i = 0; i < tipoEvaluacion.duracion; i++){
			if (!childs.isEmpty()) {
				ceconomico.valores.get(i).valorPropuesto = sumatorio("valores",i,"valorPropuesto", childs);
				ceconomico.valores.get(i).valorConcedido = sumatorio("valores",i,"valorConcedido", childs);
				ceconomico.valores.get(i).valorEstimado = sumatorio("valores",i,"valorEstimado", childs);
				ceconomico.valores.get(i).valorSolicitado = sumatorio("valores",i,"valorSolicitado", childs);
			} else if (ceconomico.tipo.tipoOtro) {
				ceconomico.valores.get(i).valorPropuesto = sumaOtros(ceconomico, i, "valorPropuesto");
				ceconomico.valores.get(i).valorEstimado = sumaOtros(ceconomico, i, "valorEstimado");
				ceconomico.valores.get(i).valorConcedido = sumaOtros(ceconomico, i, "valorConcedido");
				ceconomico.valores.get(i).valorSolicitado = sumaOtros(ceconomico, i, "valorSolicitado");
			}
		}
	}

	private static Double sumaOtros(CEconomico ceconomico, int i, String fieldName) {
		Double sum = 0D;
		for ( CEconomicosManuales c : ceconomico.otros) {
			try {
				Object o = c.valores.get(i);
				Field field = o.getClass().getField(fieldName);
				sum += (Double)field.get(o);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}
		return sum;
	}

	static Double sumatorio(String fieldName, List<?> list) {
		Double sum = 0D;
		for (Object o : list) {
			try {
				Class<? extends Object> clazz = o.getClass();
				Field field;
				field = clazz.getField(fieldName);
				Double valor = (Double) field.get(o);
				if (valor != null) {
					sum += valor;
				}
			} catch (Exception e) {
			}
		}
		return sum;
	}
	
	// Para calcular el sumatorio de valores[INDICE].valorXXXXX 
	static Double sumatorio(String fieldName, Integer id, String fieldNameValue, List<?> list) {
		Double sum = 0D;
		for (Object o : list) {
			try {
				Class<? extends Object> clazz = o.getClass();
				Field field;
				field = clazz.getField(fieldName);
				Object obj = field.get(o);
				PersistentBag p = (PersistentBag)obj;
				Object o1 = p.get(id);
				Class<? extends Object> clazz2 = o1.getClass();
				field = clazz2.getField(fieldNameValue);
				Double valor = (Double) field.get(o1);
				if (valor != null) {
					sum += valor;
				}
			} catch (Exception e) {
			}
		}
		return sum;
	}

}
