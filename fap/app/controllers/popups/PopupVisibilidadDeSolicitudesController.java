
package controllers.popups;

import java.util.ArrayList;
import java.util.List;

import messages.Messages;
import models.TableKeyValue;

import tags.ComboItem;

import controllers.gen.popups.PopupVisibilidadDeSolicitudesControllerGen;
			
public class PopupVisibilidadDeSolicitudesController extends PopupVisibilidadDeSolicitudesControllerGen {

	public static void editar(Long idTableKeyValue,TableKeyValue tableKeyValue){
		checkAuthenticity();
		if(!permiso("editar")){
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		TableKeyValue dbTableKeyValue = null;
		if(!Messages.hasErrors()){
			dbTableKeyValue = getTableKeyValue(idTableKeyValue);
		}
		
		if(!Messages.hasErrors()){
			TableKeyValue.setValue(dbTableKeyValue.table, dbTableKeyValue.key, tableKeyValue.value);
		}

		if(!Messages.hasErrors()){
			renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
		}else{
			Messages.keep();
			index("editar", idTableKeyValue);
		}
	}
	
	public static List<ComboItem> comboEstadoInterno() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		
		java.util.List<TableKeyValue> rows = TableKeyValue.find( "select tableKeyValue from TableKeyValue tableKeyValue where tableKeyValue.table = ?", "estadosSolicitud" ).fetch();
		for(TableKeyValue tableKeyValue: rows){
			result.add(new ComboItem(tableKeyValue.key, tableKeyValue.value));
		}
		return result;
	}
}
		