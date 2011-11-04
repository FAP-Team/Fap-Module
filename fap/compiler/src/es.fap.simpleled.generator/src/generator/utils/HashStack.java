package generator.utils;

import java.util.*;

public class HashStack {
	
	/**
	 * 
	 * SUBIR_ARCHIVO - Almacena los elementos SubirArchivo y SubirArchivoAED para generar código
	 *                 específico en el controlador
	 *                 
	 * SAVE_PARAM    - Parámetros que se van a añadir al método save, se utiliza en casos como
	 *                 el subirArchivo o el subirArchivoAED
	 */
	public enum HashStackName {
		FORMULARIO, CONTROLLER, ROUTES, PAGE_NAME, CONTAINER,
		SUBIR_ARCHIVO,
		SAVE_ENTITY,
		INDEX_ENTITY,
		SAVE_EXTRA,
		SAVE_CODE,
		PERMISSION,
		SAVE_PARAM,
		SAVE_BOTON,
		FIRST_PAGE,
		SOLICITUD
	}
	
	private static HashMap<HashStackName, Stack<Object>> hash = new HashMap<HashStackName, Stack<Object>>();

	public static void push(HashStackName stack, Object value) {
		if (!hash.containsKey(stack)) {
			hash.put(stack, new Stack<Object>());
		}

		Stack<Object> s = hash.get(stack);
		s.push(value);
		
	}
	
	public static void pop(HashStackName stack) {
		if (hash.containsKey(stack)) {
			Stack<Object> s = hash.get(stack);
			s.pop();
		}
	}

	public static Object top(HashStackName stack) {
		Object res = null;

		if (hash.containsKey(stack)) {
			Stack<Object> s = hash.get(stack);
			if (s.size() == 0)
				return null;
			res = s.lastElement();
		}

		return res;
	}

	public static Object first(HashStackName stack) {
		Object res = null;

		if (hash.containsKey(stack)) {
			Stack<Object> s = hash.get(stack);
			if (s.size() == 0)
				return null;
			res = s.firstElement();
		}

		return res;
	}

	public static List<Object> allElements(HashStackName stack) {
		if (hash.containsKey(stack)) {
			return new ArrayList<Object>(hash.get(stack));
		}
		return new ArrayList<Object>();
	}
	
	public static void remove(HashStackName stack){
		if (hash.containsKey(stack)){
			hash.remove(stack);
		}
	}
	
	
	public static boolean contains(HashStackName stack, Object str){
		boolean res = false;

		if (hash.containsKey(stack)) {
			Stack<Object> s = hash.get(stack);
			res = s.contains(str);
		}

		return res;
	}
	
	public static int size (HashStackName stack) {
		if (hash.containsKey(stack)) {
			return hash.get(stack).size();
		}
		return 0;
	}
	
	public static List<Object> getUntil (HashStackName stack, int index) {
		if (hash.containsKey(stack)) {
			Stack<Object> s = hash.get(stack);
			if (s.size() > index){
				return new ArrayList<Object>(s.subList(index, s.size()));
			}
		}
		return new ArrayList<Object>();
	}
	
	public static List<Object> popUntil (HashStackName stack, int index) {
		if (hash.containsKey(stack)) {
			Stack<Object> s = hash.get(stack);
			if (s.size() > index){
				ArrayList<Object> tmp = new ArrayList<Object>(s.subList(index, s.size()));
				while(s.size() > index)
					s.pop();
				return tmp;
			}	
		}
		return new ArrayList<Object>();
	}
}
