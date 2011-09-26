package tags;

import java.util.*;


/**
 * 
 * Pilas definidas:
 * 	* agruparCampos
 *
 */
public class TagMapStack {
	static ThreadLocal<Map<String, Stack<Object>>> currentTagMapStack = new ThreadLocal<Map<String, Stack<Object>>>();
	
	public static Map<String, Stack<Object>> getCurrent(){
		Map<String, Stack<Object>> current = currentTagMapStack.get();
		if(current == null){
			current = new HashMap<String, Stack<Object>>();
			currentTagMapStack.set(current);
		}
		return current;
	}
	
	public static void push(String stackName, Object o){
		Stack<Object> stack = getCurrent().get(stackName);
		if(stack == null){
			stack = new Stack<Object>();
			currentTagMapStack.get().put(stackName, stack);
		}
		stack.push(o);
	}
	
	public static void clear(){
		getCurrent().clear();
	}
	
	public static Object pop(String stackName){
		Stack<Object> stack = getCurrent().get(stackName);
		if(stack != null && stack.size() > 0){
			return stack.pop();
		}
		return null;
	}
	
	public static Object top(String stackName){
		Stack<Object> stack = getCurrent().get(stackName);
		if(stack != null && stack.size() > 0 ){
			return stack.peek();
		}
		return null;
	}
	
}
