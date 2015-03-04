
package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tags.ComboItem;
import controllers.gen.CombosControllerGen;

public class CombosController extends CombosControllerGen {

    public static Object solicitud_comboTest_parametros(Map<String, Object> args) {
        
        List<ComboItem> lst = new ArrayList<ComboItem>();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            ComboItem cmb = new ComboItem(entry.getKey(), String.format("Key: %s, Value: %s.", entry.getKey(), entry.getValue()));
            lst.add(cmb);
            log.info(String.format("Key: %s, Value: %s.", entry.getKey(), entry.getValue()));
        }
        
        return (Object) lst;
    }
    
    private static Long cuadrado(Long val) {
        return val * val;
        
    }

}
		