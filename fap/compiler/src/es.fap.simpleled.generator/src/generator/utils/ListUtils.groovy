package generator.utils;

public class ListUtils {

	public static String list2GroovyListString(lista){
		return "[" + lista.collect { "'$it'" }.join(', ') + "]";
	}
	
}
