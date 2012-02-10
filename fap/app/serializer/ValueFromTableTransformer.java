package serializer;

import java.util.List;
import java.util.Set;

import models.TableKeyValue;
import validation.ValueFromTable;

import flexjson.transformer.AbstractTransformer;

public class ValueFromTableTransformer extends AbstractTransformer {

    String tabla = "";

    public ValueFromTableTransformer(String tabla) {
        super();
        this.tabla = tabla;
    }

    public void transform(Object object) {
        String result = "";
        if (object instanceof java.util.Set) {
            for (String key : (Set<String>) object) {
                if (result.equals("")) {
                    result = TableKeyValue.getValue(tabla, key);
                } else {
                    result += ", " + TableKeyValue.getValue(tabla, key);
                }
            }
        } else {
            result = TableKeyValue.getValue(tabla, object.toString());
        }
        if (result == null){
            getContext().write("null");
        }else{
            getContext().writeQuoted(result);
        }
        System.out.println("table" + tabla + " key " + object  + " result " + result);
    }

}
