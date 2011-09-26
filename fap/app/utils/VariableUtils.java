package utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesSupport;

public class VariableUtils implements LocalVariablesSupport {

	public Map<String, Object> variablesToMap(Object...args){
        Map<String, Object> variables = new HashMap<String, Object>(16);
        for (Object o : args) {
            List<String> names = LocalVariablesNamesTracer.getAllLocalVariableNames(o);
            for (String name : names) {
                variables.put(name, o);
            }
        }
        return variables;
	}
	
}
