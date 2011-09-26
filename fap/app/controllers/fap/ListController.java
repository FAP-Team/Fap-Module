package controllers.fap;

import java.util.List;
import java.util.Map;

import models.TableKeyValue;
import play.mvc.Controller;
import utils.RestResponse;

public class ListController extends Controller {

	/**
	 * Lista todo el contenido de una tabla
	 * @param table
	 */
	public static void list(String table){
		Map<String, String> items = TableKeyValue.findByTableAsMap(table);
		RestResponse response = new RestResponse();
		if(items == null){
			response.success = false;
			response.message = "Tabla no encontrada";
		}else{
			response.success = true;
			response.data = items;
		}
		renderJSON(response);
	}
	
	/**
	 * Devuelve el valor específico para una clave de una tabla
	 * @param table
	 * @param key
	 */
	public static void value(String table, String key){
		String value = TableKeyValue.getValue(table, key);
		RestResponse response = new RestResponse();
		if(value == null){
			response.success = false;
			response.message = "Valor no encontrado";
		}else{
			response.success = true;
			response.data = value;
		}
		renderJSON(response);
	}
	
}
