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

import models.Firmante;
import models.TableKeyValue;
import tags.ReflectionUtils;
import validation.ValueFromTable;

public class TableRenderNoPermisos<T> {
	public List<T> rows;
	
	public TableRenderNoPermisos(List<T> rows) {
		this.rows = rows;
	}
	
	public String toJSON(String ... fields){
		Set<String> fieldsSet = new HashSet<String>(Arrays.asList(fields));
		
		String[] includeParams = new String[fields.length];
		for(int i = 0; i < fields.length; i++)
			includeParams[i] = "rows." + fields[i];
		
		Map<String,List<String>> valueFromTable = getValueFromTableField(fieldsSet);
		JSONSerializer flex = new JSONSerializer()
			.include(includeParams)
			.transform(new serializer.DateTimeTransformer(), org.joda.time.DateTime.class);
											
		for (String table : valueFromTable.keySet())
			for (String field : valueFromTable.get(table))
				if (fieldsSet.contains(field))
					flex = flex.transform(new serializer.ValueFromTableTransformer(table), "rows." + field);
					
		flex = flex.exclude("*");

		String serialize = flex.serialize(this);
		return serialize;
	}
	
	public HashMap<String,List<String>> getValueFromTableField(Set<String> fieldsSet) {
		HashMap<String,List<String>> valueFromTable = new HashMap<String,List<String>>();
		if ((rows != null) && (!rows.isEmpty())) {
			T row = rows.get(0);
			 Iterator it = fieldsSet.iterator();
	            while(it.hasNext()) {
	            	String _it = (String) it.next();
				Field f = null;
				try { f = ReflectionUtils.getFieldRecursivelyFromClass(row.getClass(), _it);} 
				catch (Exception e) {e.printStackTrace();}
				if (f != null) {
					ValueFromTable annotation = f.getAnnotation(ValueFromTable.class);
					if(annotation != null){
						if (!valueFromTable.containsKey(annotation.value()))
							valueFromTable.put(annotation.value(), new ArrayList<String>());
						valueFromTable.get(annotation.value()).add(_it);
					}
				}
			}
		}
		return valueFromTable;
	}
	
	
}
