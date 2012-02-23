package tables;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import play.db.jpa.Model;

import flexjson.JSONSerializer;

import messages.Messages;
import messages.Messages.MessageType;
import models.Firmante;
import models.TableKeyValue;
import tags.ReflectionUtils;
import validation.ValueFromTable;

public class TableRenderResponse<T> {

    public List<TableRecord<T>> rows;

    public Mensajes mensajes = Mensajes.fromGlobalMessages();

    public Obj obj;

    public TableRenderResponse(List<TableRecord<T>> rows) {
        this.rows = rows;
        this.obj = new Obj();
        obj.rows = this.rows;
    }

    public static <T> TableRenderResponse<T> sinPermisos(List<T> rows) {
        List<TableRecord<T>> result = new ArrayList<TableRecord<T>>();
        for (T row : rows) {
            TableRecord<T> record = new TableRecord<T>();
            result.add(record);
            record.objeto = row;
            play.Logger.info("Objeto: "+row);
            record.permisoLeer = true;
            record.permisoEditar = true;
            record.permisoBorrar = true;
        }
        return new TableRenderResponse<T>(result);
    }

    public String toJSON(String... fields) {
        Set<String> fieldsSet = new HashSet<String>(Arrays.asList(fields));
        obj.mensajes = this.mensajes;
        String[] includeParams = new String[fields.length + 8];
        for (int i = 0; i < fields.length; i++)
            includeParams[i] = "obj.rows.objeto." + fields[i];
        includeParams[fields.length] = "obj.rows.permisoLeer";
        includeParams[fields.length + 1] = "obj.rows.permisoEditar";
        includeParams[fields.length + 2] = "obj.rows.permisoBorrar";
        includeParams[fields.length + 3] = "obj.mensajes.error";
        includeParams[fields.length + 4] = "obj.mensajes.warning";
        includeParams[fields.length + 5] = "obj.mensajes.fatal";
        includeParams[fields.length + 6] = "obj.mensajes.ok";
        includeParams[fields.length + 7] = "obj.mensajes.info";

        Map<String, List<String>> valueFromTable = getValueFromTableField(fieldsSet);
        JSONSerializer flex = new JSONSerializer().include(includeParams).transform(
                new serializer.DateTimeTransformer(), org.joda.time.DateTime.class);

        for (String table : valueFromTable.keySet()){
            for (String field : valueFromTable.get(table)){
                if (fieldsSet.contains(field)) {
                    flex = flex.transform(new serializer.ValueFromTableTransformer(table), "obj.rows.objeto." + field);
                }
            }
        }

        flex = flex.exclude("*");

        String serialize = flex.serialize(this);
        return serialize;
    }
    
    public HashMap<String, List<String>> getValueFromTableField(Set<String> fieldsSet) {
    	HashMap<String, List<String>> valueFromTable = new HashMap<String, List<String>>();
    	
        if ((rows != null) && (!rows.isEmpty())) {
            T row = rows.get(0).objeto;
            
            Iterator it = fieldsSet.iterator();
            while(it.hasNext()) {
            	String _it = (String) it.next();
            
            	Field f = null;
            	try {
            		f = ReflectionUtils.getFieldRecursivelyFromClass(row.getClass(), _it);
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            	if (f != null) {
            		ValueFromTable annotation = f.getAnnotation(ValueFromTable.class);
            		if (annotation != null) {
            			if (!valueFromTable.containsKey(annotation.value()))
            				valueFromTable.put(annotation.value(), new ArrayList<String>());
            			valueFromTable.get(annotation.value()).add(_it);
            		}
            	}
            }
        }
    	return valueFromTable;
    }

    public class Obj {
        public List<TableRecord<T>> rows;
        public Mensajes mensajes;
    }
    
    public static class Mensajes {
        public List<String> error;
        public List<String> fatal;
        public List<String> info;
        public List<String> ok;
        public List<String> warning;
        
        public static Mensajes fromGlobalMessages(){
            Mensajes mensajes = new Mensajes();
            mensajes.error=Messages.messages(MessageType.ERROR);
            mensajes.warning=Messages.messages(MessageType.WARNING);
            mensajes.fatal=Messages.messages(MessageType.FATAL);
            mensajes.ok=Messages.messages(MessageType.OK);
            mensajes.info=Messages.messages(MessageType.INFO);
            return mensajes;
        }
    }
}
