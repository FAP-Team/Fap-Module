package baremacion;
import java.util.List;

import models.Criterio;
import baremacion.Evaluador;


public class EvaluadorBaremacion extends Evaluador {

	public static void evalDefault(Criterio c , List<Criterio> childs){
		c.valor = 0D;
	}
	
	public static void eval1(Criterio c, List<Criterio> childs){
		c.valor = 12D;
	}
	
}
