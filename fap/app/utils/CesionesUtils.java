package utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.Cesiones;

public class CesionesUtils {

	public static class CesionesSortComparator implements Comparator {
		  public int compare(Object o1, Object o2) {
			  Cesiones u1 = (Cesiones) o1;
			  Cesiones u2 = (Cesiones) o2;
			  return u2.fechaValidez.compareTo(u1.fechaValidez);
		  }
		  public boolean equals(Object o) {
			  return this == o;
		  }
	}
	public static void ordenarTiposCesiones (List<Cesiones> lista){
		Collections.sort(lista, new CesionesSortComparator());
	}
	
}
