package tables;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import play.db.jpa.Model;
import play.mvc.Util;

import flexjson.JSONSerializer;

import messages.Messages;
import messages.Messages.MessageType;
import models.Firmante;
import models.TableKeyValue;
import tags.ReflectionUtils;
import validation.ValueFromTable;

public class TableRenderResponse<T> {
	public List<TableRecord<T>> rows;
	public class Mensajes {
		
		public List <String> error;
		public List <String> fatal;
		public List <String> info;
		public List <String> ok;
		public List <String> warning;
	}
	
	public Mensajes mensajes = new Mensajes();
	
	public class Obj {
		public List<TableRecord<T>> rows;
		public Mensajes mensajes;
	}
	
	public Obj obj;
	
	// Constructor con Permisos
	public TableRenderResponse(List<T> rows) {
		if (rows != null){
			List<TableRecord<T>> rowsPermisos = tablaPermisos(rows);
			this.rows = rowsPermisos;
		} else{
			this.rows = null;
		}
		this.obj = new Obj();
		obj.rows = this.rows;
		this.mensajes.error = Messages.messages(MessageType.ERROR);
		this.mensajes.warning = Messages.messages(MessageType.WARNING);
		this.mensajes.fatal = Messages.messages(MessageType.FATAL);
		this.mensajes.ok = Messages.messages(MessageType.OK);
		this.mensajes.info = Messages.messages(MessageType.INFO);
	}
	
	// Constructor sin Permisos
	public TableRenderResponse(List<TableRecord<T>> rows, boolean SinPermisos) {
		this.rows = rows;
		this.obj = new Obj();
		obj.rows = this.rows;
		this.mensajes.error = Messages.messages(MessageType.ERROR);
		this.mensajes.warning = Messages.messages(MessageType.WARNING);
		this.mensajes.fatal = Messages.messages(MessageType.FATAL);
		this.mensajes.ok = Messages.messages(MessageType.OK);
		this.mensajes.info = Messages.messages(MessageType.INFO);
	}
	
	public static <T> TableRenderResponse<T> sinPermisos(List<T> rows) {
		List<TableRecord<T>> result = new ArrayList<TableRecord<T>>();
		for (T row: rows){
			TableRecord<T> record = new TableRecord<T>();
			result.add(record);
			record.objeto = row;
			record.permisoLeer = true;
			record.permisoEditar = true;
			record.permisoBorrar = true;
		}
		return new TableRenderResponse<T>(result, true);
	}
	
	public String toJSON(String ... fields){
		Set<String> fieldsSet = new HashSet<String>(Arrays.asList(fields));
		obj.mensajes = this.mensajes;
		String[] includeParams = new String[fields.length + 8];
		for(int i = 0; i < fields.length; i++)
			includeParams[i] = "obj.rows.objeto." + fields[i];
		includeParams[fields.length] = "obj.rows.permisoLeer";
		includeParams[fields.length + 1] = "obj.rows.permisoEditar";
		includeParams[fields.length + 2] = "obj.rows.permisoBorrar";
		includeParams[fields.length + 3] = "obj.mensajes.error";
		includeParams[fields.length + 4] = "obj.mensajes.warning";
		includeParams[fields.length + 5] = "obj.mensajes.fatal";
		includeParams[fields.length + 6] = "obj.mensajes.ok";
		includeParams[fields.length + 7] = "obj.mensajes.info";
		
		Map<String,List<String>> valueFromTable = getValueFromTableField();
		JSONSerializer flex = new JSONSerializer()
			.include(includeParams)
			.transform(new serializer.DateTimeTransformer(), org.joda.time.DateTime.class);
		
		for (String table : valueFromTable.keySet())
			for (String field : valueFromTable.get(table))
				if (fieldsSet.contains(field))
					flex = flex.transform(new serializer.ValueFromTableTransformer(table), "obj.rows.objeto." + field);
					
		flex = flex.exclude("*");
		String serialize = flex.serialize(this);
		return serialize;
	}
	
	public HashMap<String,List<String>> getValueFromTableField() {
		HashMap<String,List<String>> valueFromTable = new HashMap<String,List<String>>();
		if ((rows != null) && (!rows.isEmpty())) {
			T row = rows.get(0).objeto;
			java.util.List<String> fields = ReflectionUtils.getFieldsNamesForClass(row.getClass());
			for (String field : fields) {
				Field f = null;
				try { f = row.getClass().getField(field);} 
				catch (Exception e) {e.printStackTrace();}
				if (f != null) {
					ValueFromTable annotation = f.getAnnotation(ValueFromTable.class);
					if(annotation != null){
						if (!valueFromTable.containsKey(annotation.value()))
							valueFromTable.put(annotation.value(), new ArrayList<String>());
						valueFromTable.get(annotation.value()).add(field);
					}
				}
			}
		}
		return valueFromTable;
	}
	
	@Util
	public static <T> List<TableRecord<T>> tablaPermisos(List<T> rowsFiltered) {
		List<TableRecord<T>> records = new ArrayList<TableRecord<T>>();
		Map<String, Object> vars = new HashMap<String, Object>();
		for (T tablaTipo : rowsFiltered) {
			TableRecord<T> record = new TableRecord<T>();
			records.add(record);
			record.objeto = tablaTipo;
			String[] nombre = tablaTipo.getClass().getName().split("\\.");
			vars.put(nombre[nombre.length-1], tablaTipo);
			record.permisoLeer = true;
			record.permisoEditar = true;
			record.permisoBorrar = true;
		}
		return records;
	}
	
	
}
