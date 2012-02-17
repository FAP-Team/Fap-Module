package baremacion;

import java.lang.reflect.Field;
import java.util.List;

import models.CEconomico;
import models.Criterio;
import models.Evaluacion;

public class Evaluador {

	public static void evalDefault(Criterio criterio, List<Criterio> childs) {
		criterio.valor = sumatorio("valor", childs);
	}
	
	public static <T> void evalDefault(Evaluacion evaluacion, List<T> childs, String tipo){
		if (tipo.equals("Criterios"))
			evaluacion.totalCriterios = sumatorio("valor", childs);
		else if(tipo.equals("CEconomicos"))
			evaluacion.inversionTotalAprobada = sumatorio("valorPropuesto", childs);
	}

	public static void evalDefault(CEconomico ceconomico, List<CEconomico> childs) {
//		ceconomico.valorConcedido = sumatorio("valorConcedido", childs);
//		ceconomico.valorEstimado = sumatorio("valorEstimado", childs);
//		ceconomico.valorPropuesto = sumatorio("valorPropuesto", childs);
//		ceconomico.valorSolicitado = sumatorio("valorSolicitado", childs);
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

}
