package services;

import models.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import baremacion.Evaluador;

import play.Logger;
import play.Play;

public class BaremacionService {

	/**
	 * Calcula los totales de los criterios de evalaución y los conceptos económicos
	 * @param evaluacion
	 */
	public static void calcularTotales(Evaluacion evaluacion){
		//Ordena los elementos para calcularlos teniendo en cuenta las dependencias
		List<List<Criterio>> sortedCriterios = sortByProfundidad(evaluacion.criterios);
		List<List<CEconomico>> sortedCEconomicos = sortByProfundidad(evaluacion.ceconomicos);
		
		//El nivel más profundo no es necesario calcularlo
		for(int i = sortedCriterios.size() - 2; i >= 0; i--){
			for(Criterio criterio : sortedCriterios.get(i)){
				//TODO revisar código para automod
				if(criterio.tipo.claseCriterio.equals("auto") || criterio.tipo.claseCriterio.equals("automod")){
					List<Criterio> childs = getChilds(criterio, sortedCriterios.get(i + 1));
					invokeEval(criterio.tipo.jerarquia, criterio, childs);
				}
			}
		}
	}
	
	/**
	 * Llama al método que debe calcular el valor del criterio o concepto automático.
	 * 
	 * El orden de las llamadas es:
	 *    - Método específico en clase que hereda de Evaluator
	 *    - Método por defecto en clase que hereda de Evaluator
	 *    - Método por defecto en la clase Evaluator
	 *    
	 * @param jerarquia Jerarquía que ocupa el padre, necesaria para saber el nombre del método
	 * @param parent Entidad padre
	 * @param childs Lista de hijos
	 * @return
	 */
	private static <T> void invokeEval(String jerarquia, T parent, List<T> childs){
		Class invokedClass = null;
		//Busca una clase que herede del evaluador
        List<Class> assignableClasses = Play.classloader.getAssignableClasses(Evaluador.class);
        if(assignableClasses.size() > 0){
            invokedClass = assignableClasses.get(0);
        }
        
        String methodName = "eval" + jerarquia.replaceAll("//.", "_");
        Method method = null;

        if(invokedClass != null){
	        //Método de la clase evaluador
	        try {
	        	method = invokedClass.getDeclaredMethod(methodName, parent.getClass(), List.class);
			} catch (Exception e) {
			}
	        
	        //Default de la clase evaluador
	        if(method == null){
	        	try {
	        		method = invokedClass.getDeclaredMethod("evalDefault", parent.getClass(), List.class);
	        	}catch(Exception e){}
	        }
        }
        
        //Método por defecto de la clase generica
        if(method == null){
        	try {
        		method = Evaluador.class.getDeclaredMethod("evalDefault", parent.getClass(), List.class);
        	}catch(Exception e){}
        }
        
        //Llama al método
        if(method != null){
        	try {
        		method.invoke(null, parent, childs);
			} catch (Exception e) {}
        }
	}
	
	
	private static <T> List<T> getChilds(T parent, List<T> posibleChilds){
		List<T> childs = new ArrayList<T>();
		for(T child : posibleChilds){
			if(isChild(parent, child)){
				childs.add(child);
			}
		}
		return childs;
	}
	
	private static <T> boolean isChild(T parent, T child){
		String parentJer = getJerarquia(parent);
		String childJer = getJerarquia(child);
		if(parentJer == null || childJer == null) return false;
		return childJer.startsWith(parentJer);
	}
	
	/**
	 * Ordena los elementos en profundidad para poder recorrerlos
	 * @param elementos
	 * @return
	 */
	private static <T> List<List<T>> sortByProfundidad(List<T> elementos) {
		if (elementos.size() == 0)
			return null;
		
		long max = maxProfundidad(elementos);
		List<List<T>> sorted = new ArrayList<List<T>>();
		for (int i = 0; i <= max; i++) {
			sorted.add(new ArrayList<T>());
		}
		for (T elemento : elementos) {
			sorted.get(getProfundidad(elemento)).add(elemento);
		}
		return sorted;
	}
	
	/**
	 * Calcula la profundidad máxima
	 * @param elementos
	 * @return
	 */
	private static int maxProfundidad(List<?> elementos){
		int max = -1;
		for(Object o : elementos){
			int profundidad = 0;
			profundidad = getProfundidad((Criterio)o);
			if(profundidad > max){
				max = profundidad;
			}
		}
		return max;
	}
	
	private static int getProfundidad(String jerarquia){
		if(jerarquia == null) return 0;
		int profundidad = jerarquia.split("\\.").length - 1;
		return profundidad;
	}
	
	private static int getProfundidad(Object o){
		return getProfundidad(getJerarquia(o));
	}
	
	private static String getJerarquia(Object o){
		if(o instanceof Criterio){
			return ((Criterio)o).tipo.jerarquia;
		}else if(o instanceof CEconomico){
			return ((CEconomico)o).tipo.jerarquia;
		}
		return null;
	}
}
