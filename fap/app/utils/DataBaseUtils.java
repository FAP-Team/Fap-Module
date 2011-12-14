package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import security.Secure;

import config.InjectorConfig;

import models.TableKeyValue;

public class DataBaseUtils {

	private static String USER_TABLE_NAME = "estadosSolicitudUsuario";
	
	public static void updateEstadosSolicitudUsuario () {
		java.util.List<TableKeyValue> rows = TableKeyValue.find( "select tableKeyValue from TableKeyValue tableKeyValue" ).fetch();

		Secure secure = InjectorConfig.getInjector().getInstance(Secure.class);
		
		Map<String, Long> ids = new HashMap<String, Long>();

		List<TableKeyValue> rowsFiltered = new ArrayList<TableKeyValue>();
		for(TableKeyValue tableKeyValue: rows){
			Map<String, Object> vars = new HashMap<String, Object>();
			vars.put("tableKeyValue", tableKeyValue);
			if (secure.check("tableKeyOnlyEstadosSolicitud","read", ids, vars)) {
				rowsFiltered.add(tableKeyValue);
			}
		}	
		
		// Ahora en "rowsFiltered" tenemos los estados de la solicitud original
		// Si no existe alguno de los estados originales en la lista de los usuarios, lo creamos
		for(TableKeyValue tableKeyValue : rowsFiltered) {
			if (!TableKeyValue.contains(USER_TABLE_NAME, tableKeyValue.key)) {
				play.Logger.info("Creamos el estado \""+tableKeyValue.key+"\" para la visibilidad del estado del usuario");
				TableKeyValue.setValue(USER_TABLE_NAME, tableKeyValue.key, tableKeyValue.key);
			
			}
		}
	}
}
