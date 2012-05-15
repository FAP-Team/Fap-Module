
package controllers.popups;

import java.util.ArrayList;
import java.util.List;

import messages.Messages;
import models.TableKeyValue;

import tags.ComboItem;

import controllers.gen.popups.PopupVisibilidadDeSolicitudesControllerGen;
			
public class PopupVisibilidadDeSolicitudesController extends PopupVisibilidadDeSolicitudesControllerGen {

	public static List<ComboItem> comboEstadoInterno() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		
		java.util.List<TableKeyValue> rows = TableKeyValue.find( "select tableKeyValue from TableKeyValue tableKeyValue where tableKeyValue.table = ?", "estadosSolicitud" ).fetch();
		for(TableKeyValue tableKeyValue: rows){
			result.add(new ComboItem(tableKeyValue.key, tableKeyValue.value));
		}
		return result;
	}
}
		