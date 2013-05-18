package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.CEconomico;
import models.Evaluacion;
import models.LineaResolucionFAP;
import models.ResolucionFAP;
import models.SolicitudGenerica;


public class ResolucionUtils {
	
	public static class LineasResolucionSortComparator implements Comparator {
		static final int BEFORE = -1;
		static final int AFTER = 1;
		
		  public int compare(Object o1, Object o2) {
			  LineaResolucionFAP u1 = (LineaResolucionFAP) o1;
			  LineaResolucionFAP u2 = (LineaResolucionFAP) o2;
			  LineaResolucionFAP aux1 = u1;;
			  LineaResolucionFAP aux2 = u2;
			  if (u1.puntuacionBaremacion == null){
				  return BEFORE;
			  }
			  if (u2.puntuacionBaremacion == null){
				  return AFTER;
			  }
			  
			  int result = u2.puntuacionBaremacion.compareTo(u1.puntuacionBaremacion);
			  
			  if (result == 0){ //Si son iguales, se compara por criterios.
				  return comparacionPorCriterios(u1, u2);
			  }
			  return result;
		  }
		  public boolean equals(Object o) {
			  System.out.println("Notas iguales");
			  return this == o;
		  }
		  
		  public int comparacionPorCriterios(LineaResolucionFAP u1, LineaResolucionFAP u2){
			  //Obtener los criterios
			  List<CEconomico> ce1 = u1.solicitud.ceconomicos;
			  List<CEconomico> ce2 = u2.solicitud.ceconomicos;
			  //Recorrer cada pareja de CEconomicos comparando criterios.
			  for (int i = 0; i < ce1.size(); i++) { //Ambos tienen el mismo tamaño
//				List<CEconomico> aComparar = new ArrayList<CEconomico>();
//				aComparar.add(ce2.get(i));
//				aComparar.add(ce1.get(i));
				if (ce1.get(i).total < ce2.get(i).total){
					return BEFORE;
				} else if (ce1.get(i).total > ce2.get(i).total){
					return AFTER;
				} 
			  }
			  return 0; //Son iguales en todo
		  }
	}
	
	public static void ordenarLineasResolucion (ResolucionFAP resolucion){
		Collections.sort(resolucion.lineasResolucion, new LineasResolucionSortComparator());
	}

}
