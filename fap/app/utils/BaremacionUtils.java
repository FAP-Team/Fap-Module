package utils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.mapping.Collection;

import play.Logger;

import com.google.gson.reflect.TypeToken;

import services.BaremacionService;

import models.CEconomico;
import models.Criterio;
import models.Evaluacion;
import models.SolicitudGenerica;
import models.TipoCEconomico;
import models.TipoCriterio;
import models.TipoDatoAdicional;
import models.TipoEvaluacion;
import models.ValoresCEconomico;

public class BaremacionUtils {

	public static void calcularTotales (SolicitudGenerica solicitud){
		if(solicitud.ceconomicos != null && solicitud.ceconomicos.size() > 0){
			List<List<CEconomico>> sortedCEconomicos = BaremacionService.sortByProfundidad(solicitud.ceconomicos);
			for(int i = sortedCEconomicos.size() -2; i >= 0; i--){
				for(CEconomico ceconomico : sortedCEconomicos.get(i)){
					play.Logger.info("Calculando automático en la solicitud "+ceconomico.tipo.jerarquia);
					if(ceconomico.tipo.clase.equals("auto") && (!ceconomico.tipo.tipoOtro)){
						List<CEconomico> childs = BaremacionService.getChilds(ceconomico, sortedCEconomicos.get(i + 1));
						BaremacionService.invokeEval(ceconomico.tipo.jerarquia, ceconomico, childs);
					}
				}
			}
		}
		solicitud.save();
	}
	
	public static void calcularTotalesCEconomicosFichaEvaluacion (Evaluacion evaluacion){
		if(evaluacion.ceconomicos != null && evaluacion.ceconomicos.size() > 0){
			List<List<CEconomico>> sortedCEconomicos = BaremacionService.sortByProfundidad(evaluacion.ceconomicos);
			for(int i = sortedCEconomicos.size() -2; i >= 0; i--){
				for(CEconomico ceconomico : sortedCEconomicos.get(i)){
					play.Logger.info("Calculando automático en la solicitud "+ceconomico.tipo.jerarquia);
					if(ceconomico.tipo.clase.equals("auto")){
						List<CEconomico> childs = BaremacionService.getChilds(ceconomico, sortedCEconomicos.get(i + 1));
						BaremacionService.invokeEval(ceconomico.tipo.jerarquia, ceconomico, childs);
					}
				}
			}
		}
		evaluacion.save();
	}

	public static void actualizarTiposCriterios (TipoEvaluacion tipoEvaluacion, List<TipoCriterio> tiposCriterios){
		for (TipoCriterio tCriterio: tiposCriterios){
			int resultadoBusqueda = tCriterio.estoyContenido(tipoEvaluacion.criterios);
			if (resultadoBusqueda == -1){ // Si no esta insertado, lo inserta
				tCriterio.esNuevo=true;
				tipoEvaluacion.criterios.add(tCriterio);
			} else { // Si está insertado, lo actualiza
				tipoEvaluacion.criterios.get(resultadoBusqueda).actualizar(tCriterio);
			}
		}
		tipoEvaluacion.save();
	}
	
	public static void actualizarTiposCEconomicos(TipoEvaluacion tipoEvaluacion, List<TipoCEconomico> tiposCEconomicos){
		for (TipoCEconomico tCEconomico: tiposCEconomicos){
			int resultadoBusqueda = tCEconomico.estoyContenido(tipoEvaluacion.ceconomicos);
			if (resultadoBusqueda == -1) { // Si no esta insertado, lo inserta
				tCEconomico.esNuevo=true;
				tipoEvaluacion.ceconomicos.add(tCEconomico);
			} else { // Si está insertado, lo actualiza
				tipoEvaluacion.ceconomicos.get(resultadoBusqueda).actualizar(tCEconomico);
			}
		}
		tipoEvaluacion.save();
	}
	
	public static void actualizarTiposDatosAdicionales (TipoEvaluacion tipoEvaluacion, List<TipoDatoAdicional> tiposDatosAdicionales){
		for (TipoDatoAdicional tDatoAdicional: tiposDatosAdicionales){
			int resultadoBusqueda = tDatoAdicional.estoyContenido(tipoEvaluacion.datosAdicionales);
			if (resultadoBusqueda == -1) {// Si no esta insertado, lo insert
				tipoEvaluacion.datosAdicionales.add(tDatoAdicional);
			} else { // Si está insertado, lo actualiza
				tipoEvaluacion.datosAdicionales.get(resultadoBusqueda).actualizar(tDatoAdicional);
			}
		}
		tipoEvaluacion.save();
	}
	
	public static void actualizarParametrosVariables (TipoEvaluacion tipoEvaluacion){
		Type type;
		if (new File("conf/initial-data/criterios.json").exists()){
			// Actualizamos en BBDD los Tipos de Criterios, a través del fichero .json que los define. La actualización simplemente inserta en BBDD si no está metido, no hace nada más.
			type = new TypeToken<ArrayList<TipoCriterio>>(){}.getType();
			List<TipoCriterio> tiposCriterios = JsonUtils.loadObjectFromJsonFile("conf/initial-data/criterios.json", type);
			actualizarTiposCriterios(tipoEvaluacion, tiposCriterios);
		} else {
			Logger.info("No se puede leer el fichero que contiene los parámetros de los Criterios (/conf/initial-data/criterios.json)");
		}
		if (new File("conf/initial-data/conceptosEconomicos.json").exists()) {
			// Actualizamos en BBDD los Tipos de CEconomicos, a través del fichero .json que los define. La actualización simplemente inserta en BBDD si no está metido, no hace nada más.
			type = new TypeToken<ArrayList<TipoCEconomico>>(){}.getType();
			List<TipoCEconomico> tiposCEconomicos = JsonUtils.loadObjectFromJsonFile("conf/initial-data/conceptosEconomicos.json", type);
			actualizarTiposCEconomicos(tipoEvaluacion, tiposCEconomicos);
		} else {
			Logger.info("No se puede leer el fichero que contiene los parámetros de los CEconomicos (/conf/initial-data/conceptosEconomicos.json)");
		}
		if (new File("conf/initial-data/datosAdicionales.json").exists()) {
			// Actualizamos en BBDD los Tipos de Datos Adicionales, a través del fichero .json que los define. La actualización simplemente inserta en BBDD si no está metido, no hace nada más.
			type = new TypeToken<ArrayList<TipoDatoAdicional>>(){}.getType();
			List<TipoDatoAdicional> tiposDatosAdicionales = JsonUtils.loadObjectFromJsonFile("conf/initial-data/datosAdicionales.json", type);
			actualizarTiposDatosAdicionales(tipoEvaluacion, tiposDatosAdicionales);
		} else {
			Logger.info("No se puede leer el fichero que contiene los parámetros de los Datos Adicionales (/conf/initial-data/datosAdicionales.json)");
		}
	}
	
	public static void actualizarTipoEvaluacion(){
		//Inicializa o recupera el tipo de evaluacion
		TipoEvaluacion tipoEvaluacion = null;
		if (TipoEvaluacion.count() > 0){
			tipoEvaluacion = (TipoEvaluacion) TipoEvaluacion.findAll().get(0);
			actualizarParametrosVariables(tipoEvaluacion);
			Logger.info("Tipo de Baremación actualizada correctamente desde fichero");
		}
	}
	
	public static class CEconomicoComparator implements Comparator {
		  public int compare(Object o1, Object o2) {
		    CEconomico u1 = (CEconomico) o1;
		    CEconomico u2 = (CEconomico) o2;
		    return u1.tipo.jerarquia.compareTo(u2.tipo.jerarquia);
		  }
		  public boolean equals(Object o) {
		    return this == o;
		  }
	}
	
	public static void ordenarCEconomicos (List<CEconomico> lista){
		Collections.sort(lista, new CEconomicoComparator());
	}
	
	public static class CriterioComparator implements Comparator {
		  public int compare(Object o1, Object o2) {
		    Criterio u1 = (Criterio) o1;
		    Criterio u2 = (Criterio) o2;
		    return u1.tipo.jerarquia.compareTo(u2.tipo.jerarquia);
		  }
		  public boolean equals(Object o) {
		    return this == o;
		  }
	}
	
	public static void ordenarCriterios (List<Criterio> lista){
		Collections.sort(lista, new CriterioComparator());
	}
	
	public static class TipoCEconomicoComparator implements Comparator {
		  public int compare(Object o1, Object o2) {
		    TipoCEconomico u1 = (TipoCEconomico) o1;
		    TipoCEconomico u2 = (TipoCEconomico) o2;
		    return u1.jerarquia.compareTo(u2.jerarquia);
		  }
		  public boolean equals(Object o) {
		    return this == o;
		  }
	}
	
	public static void ordenarTiposCEconomicos (List<TipoCEconomico> lista){
		Collections.sort(lista, new TipoCEconomicoComparator());
	}
	
	public static class TipoCriterioComparator implements Comparator {
		  public int compare(Object o1, Object o2) {
		    TipoCriterio u1 = (TipoCriterio) o1;
		    TipoCriterio u2 = (TipoCriterio) o2;
		    return u1.jerarquia.compareTo(u2.jerarquia);
		  }
		  public boolean equals(Object o) {
		    return this == o;
		  }
	}
	
	public static void ordenarTiposCriterios (List<TipoCriterio> lista){
		Collections.sort(lista, new TipoCriterioComparator());
	}
}
