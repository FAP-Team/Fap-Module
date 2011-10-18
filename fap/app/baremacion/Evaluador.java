package baremacion;

import java.util.List;

import models.CEconomico;
import models.Criterio;

public class Evaluador {
	
	public static void evalDefault(Criterio criterio, List<Criterio> childs){
		Double r = 0d;
		for(Criterio child : childs){
			if(child.valor != null){
				r += child.valor;
			}
		}
		criterio.valor = r;
	}
	
	public static void evalDefault(CEconomico concepto, List<CEconomico> childs){
		//TODO calculo de los conceptos económicos
	}
}
