package baremacion;

import java.lang.reflect.Field;
import java.util.*;

import org.hibernate.collection.PersistentBag;

import models.CEconomico;
import models.Criterio;
import models.Evaluacion;
import models.TipoEvaluacion;

public class Evaluador {

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
			ceconomico.valores.get(i).valorPropuesto = sumatorio("valores",i,"valorPropuesto", childs);
			ceconomico.valores.get(i).valorConcedido = sumatorio("valores",i,"valorConcedido", childs);
			ceconomico.valores.get(i).valorEstimado = sumatorio("valores",i,"valorEstimado", childs);
			ceconomico.valores.get(i).valorSolicitado = sumatorio("valores",i,"valorSolicitado", childs);
		}
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
