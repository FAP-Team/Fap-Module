package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import security.Secure;

import config.InjectorConfig;
import enumerado.fap.gen.EstadosSolicitudEnum;

import models.TableKeyValue;
import models.VisibilidadEstadoUsuario;

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
			if (secure.checkGrafico("tableKeyOnlyEstadosSolicitud","visible", "editar", ids, vars)) {
				rowsFiltered.add(tableKeyValue);
			}
		}	
		
		// Ahora en "rowsFiltered" tenemos los estados de la solicitud original
		// Si no existe alguno de los estados originales en la lista de los usuarios, lo creamos
		for(TableKeyValue tableKeyValue : rowsFiltered) {
			if (!TableKeyValue.contains(USER_TABLE_NAME, tableKeyValue.key)) {
				play.Logger.info("Creamos el estado \""+tableKeyValue.key+"\" para la visibilidad del estado del usuario");
				TableKeyValue.setValue(USER_TABLE_NAME, tableKeyValue.key, tableKeyValue.key, tableKeyValue.noVisible);
			}
			
			VisibilidadEstadoUsuario visibilidadEstado = VisibilidadEstadoUsuario.find( "select visibilidad from VisibilidadEstadoUsuario visibilidad where visibilidad.estadoInterno = ?", tableKeyValue.key).first();
			if (visibilidadEstado == null) {
				play.Logger.info("Creamos el estado visibilidad \""+tableKeyValue.key+"\" para la visibilidad del estado del usuario");
				VisibilidadEstadoUsuario estado = new VisibilidadEstadoUsuario();
				estado.estadoInterno = tableKeyValue.key;
				if (estado.estadoInterno.equals(EstadosSolicitudEnum.borrador.name()) || estado.estadoInterno.equals(EstadosSolicitudEnum.desistido.name()))
					estado.estadoUsuario = tableKeyValue.key;
				else
					estado.estadoUsuario = EstadosSolicitudEnum.iniciada.name();
				estado.save();
			}
			
		}
	}
}
