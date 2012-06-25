package services;

import models.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import baremacion.Evaluador;

import play.Logger;
import play.Play;
import play.vfs.VirtualFile;
import utils.JsonUtils;

public class BaremacionService {

	/**
	 * Calcula los totales de los criterios de evalaución y los conceptos económicos
	 * @param evaluacion
	 */
	public static void calcularTotales(Evaluacion evaluacion){
		//Ordena los elementos para calcularlos teniendo en cuenta las dependencias		
		//Criterios y conceptos se calculan en orden inverso para tener en cuenta las dependencias
		
		if(evaluacion.criterios != null && evaluacion.criterios.size() > 0){
			List<List<Criterio>> sortedCriterios = sortByProfundidad(evaluacion.criterios);
			List<Criterio> totales=sortedCriterios.get(0);
			for(int i = sortedCriterios.size() - 2; i >= 0; i--){
				for(Criterio criterio : sortedCriterios.get(i)){
					//TODO revisar código para automod
					if(criterio.tipo.claseCriterio.equals("auto") || criterio.tipo.claseCriterio.equals("automod")){
						List<Criterio> childs = getChilds(criterio, sortedCriterios.get(i + 1));
						invokeEval(criterio.tipo.jerarquia, criterio, childs);
					}
				}
			}
			// Para calcular la puntuacion total
			invokeEvalTotal(evaluacion, totales, "Criterios");
		}

		if(evaluacion.ceconomicos != null && evaluacion.ceconomicos.size() > 0){
			List<List<CEconomico>> sortedCEconomicos = sortByProfundidad(evaluacion.ceconomicos);
			List<CEconomico> totales=sortedCEconomicos.get(0);
			for(int i = sortedCEconomicos.size() -2; i >= 0; i--){
				for(CEconomico ceconomico : sortedCEconomicos.get(i)){
					play.Logger.info("Calculando automático %", ceconomico.tipo.jerarquia);
					if(ceconomico.tipo.clase.equals("auto")){
						List<CEconomico> childs = getChilds(ceconomico, sortedCEconomicos.get(i + 1));
						invokeEval(ceconomico.tipo.jerarquia, ceconomico, childs);
					}
				}
			}
			invokeEvalTotal(evaluacion, totales, "CEconomicos");
		}
		
	}
	
	private static <T> void invokeEvalTotal(Evaluacion parent, List<T> childs, String tipo){
		Class invokedClass = getEvaluadorClass();
		
		String methodName = "evalTotal"+tipo;
        Method method = null;
        
        if(invokedClass != null){
	        //Método de la clase evaluador
	        try {
	        	method = invokedClass.getDeclaredMethod(methodName, parent.getClass(), List.class, String.class);
			} catch (Exception e) {
			}
	        
	        //Default de la clase evaluador
	        if(method == null){
	        	try {
	        		method = invokedClass.getDeclaredMethod("evalDefault", parent.getClass(), List.class, String.class);
	        	}catch(Exception e){}
	        }
        }
        
        //Método por defecto de la clase generica
        if(method == null){
        	try {
        		method = Evaluador.class.getDeclaredMethod("evalDefault", parent.getClass(), List.class, String.class);
        	}catch(Exception e){}
        }
        
        //Llama al método
        if(method != null){
        	try {
        		method.invoke(null, parent, childs, tipo);
			} catch (Exception e) {}
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
	public static <T> void invokeEval(String jerarquia, T parent, List<T> childs){
		Class invokedClass = getEvaluadorClass();
        
        String methodName = "eval" + jerarquia.replaceAll("\\.", "_");
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
	
	
	public static <T> List<T> getChilds(T parent, List<T> posibleChilds){
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
	public static <T> List<List<T>> sortByProfundidad(List<T> elementos) {
		if (elementos.isEmpty())
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
			profundidad = getProfundidad(o);
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
	
	/**
	 * Carga un tipo de evaluacion a partir del fichero de definición
	 * en json
	 * @param path
	 * @return
	 */
	public static TipoEvaluacion loadTipoEvaluacionFromJson(String path){
		return JsonUtils.loadObjectFromJsonFile(path, TipoEvaluacion.class);
	}
	
	/**
	 * Se encarga de buscar una clase evaluador si existe y si existe el 
	 * método getDatoAdicional lo llama.
	 * 
	 * @param dato
	 * @param evaluacion
	 * @return
	 */
	public static Object getDatoAdicional(String dato, Evaluacion evaluacion){
		Class invokedClass = getEvaluadorClass();
		Object result = null;
        if(invokedClass != null){
	        try {
	        	Method method = invokedClass.getDeclaredMethod("getDatoAdicional", String.class, Evaluacion.class);
	        	result = method.invoke(null, dato, evaluacion);
			} catch (Exception e) {
				result = "";
			}
		}
		return result;
	}
                
	private static Class getEvaluadorClass() {
		Class invokedClass = null;
		//Busca una clase que herede del evaluador
        List<Class> assignableClasses = Play.classloader.getAssignableClasses(Evaluador.class);
        if(assignableClasses.size() > 0){
            invokedClass = assignableClasses.get(0);
        }
		return invokedClass;
	}
}
