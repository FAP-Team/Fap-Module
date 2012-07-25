package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import controllers.gen.TablaDeTablasControllerGen;
import messages.Messages;
import models.TableKeyValue;
import models.TableKeyValueDependency;
import play.mvc.Util;

public class TablaDeTablasController extends TablaDeTablasControllerGen {

    public static void actualizarDesdeFichero() {
        TableKeyValue.deleteAll();
        TableKeyValueDependency.deleteAll();
        long count = TableKeyValue.loadFromFiles(false);
        utils.DataBaseUtils.updateEstadosSolicitudUsuario();
        
        Messages.ok("Se cargaron desde fichero " + count + " registros, para la tabla de tablas");
        count = TableKeyValueDependency.loadFromFiles(true);
        Messages.ok("Se cargaron desde fichero " + count + " registros, para la tabla de tablas de dependencias");
        Messages.keep();
        redirect("TablaDeTablasController.index", "editar");
    }
    
    public static void tablatabladetablas() {

		java.util.List<TableKeyValue> rows = TableKeyValue.find("select tableKeyValue from TableKeyValue tableKeyValue").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<TableKeyValue> rowsFiltered =  new ArrayList<TableKeyValue>();
		for (TableKeyValue fila: rows){
			if (!fila.noVisible)
				rowsFiltered.add(fila);
		}

		tables.TableRenderResponse<TableKeyValue> response = new tables.TableRenderResponse<TableKeyValue>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("table", "key", "value", "id"));
	}

	public static void tablatabladetablasdependency() {

		java.util.List<TableKeyValueDependency> rows = TableKeyValueDependency.find("select tableKeyValueDependency from TableKeyValueDependency tableKeyValueDependency").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<TableKeyValueDependency> rowsFiltered =  new ArrayList<TableKeyValueDependency>();
		for (TableKeyValueDependency fila: rows){
			if (!fila.noVisible)
				rowsFiltered.add(fila);
		}

		tables.TableRenderResponse<TableKeyValueDependency> response = new tables.TableRenderResponse<TableKeyValueDependency>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("table", "dependency", "key", "id"));
	}

}
