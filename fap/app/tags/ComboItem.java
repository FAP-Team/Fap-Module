package tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import play.Logger;
import models.TableKeyValue;
import models.TableKeyValueDependency;

public class ComboItem {
	Object key;
	String value;
	
	public ComboItem(Object key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public ComboItem(Object key) {
		super();
		this.key = key;
		this.value = key.toString();
	}
	
	public ComboItem(TableKeyValue tkv){
		this.key = tkv.key;
		this.value = tkv.value;
	}
	
	public static List<ComboItem> listFromTableOfTable(String table){
		if(table == null) return null;
		List<TableKeyValue> entries = TableKeyValue.findByTable(table);
		List<ComboItem> items = new ArrayList<ComboItem>();
		for(TableKeyValue entry : entries){
			items.add(new ComboItem(entry));
		}
		return items;
	}
	
	public static List<ComboItem> listFromTableOfTableKeyInValue(String table){
		if(table == null) return null;
		List<TableKeyValue> entries = TableKeyValue.findByTable(table);
		List<ComboItem> items = new ArrayList<ComboItem>();
		for(TableKeyValue entry : entries){
			items.add(new ComboItem(entry.key, entry.key + " - " + entry.value));
		}
		return items;
	}
	
	public static List<ComboItem> listFromEnumerado(String enumName) {
	    List<ComboItem> items = new ArrayList<ComboItem>();
	    try {
	        Class<?> clazz = Class.forName(enumName);
	        List<Enum> enumValues = (List<Enum>)Arrays.asList(clazz.getEnumConstants());

	        for (Enum enumValue : enumValues) {
	            items.add(new ComboItem(enumValue.name(), enumValue.name()));
	        }
	    } catch (ClassNotFoundException ex) {
	        throw new IllegalArgumentException("Nombre de enumerado no encontrado.");
	    }
	    
	    return items;
	}
	
	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
