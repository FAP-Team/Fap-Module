package controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import controllers.gen.TablaDeTablasControllerGen;
import messages.Messages;
import models.AdministracionFapJobs;
import models.TableKeyValue;
import models.TableKeyValueDependency;
import play.mvc.Util;
import play.test.Fixtures;

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
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void actualizarDesdeFicheroSoloCreando(String botonCargarTablaDeTablasSoloCreando, String botonCargarTablaDeTablasSoloCreandoSinMunicipios) {
		checkAuthenticity();
		if (!permisoActualizarDesdeFicheroSoloCreando("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			if (botonCargarTablaDeTablasSoloCreandoSinMunicipios != null)
				utils.Fixtures.updateTKVAndDependencyFromAppAndFap("app/listas/gen/", 1, true);
			else
				utils.Fixtures.updateTKVAndDependencyFromAppAndFap("app/listas/gen/", 1, false);
			actualizarCacheTablaDeTablas();
		}

		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/TablaDeTablas/TablaDeTablas.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/TablaDeTablas/TablaDeTablas.html" + " , intentada sin éxito (Problemas de Validación)");
		TablaDeTablasController.actualizarDesdeFicheroSoloCreandoRender();
	}
	
	public static void actualizarCacheTablaDeTablas(){
		List<TableKeyValue> all = TableKeyValue.findAll();
		HashSet<String> tables = new HashSet<String>();
		for (TableKeyValue keyValue : all) {
			tables.add(keyValue.table);
		}
		for (String table : tables) {
			TableKeyValue.renewCache(table);
		}
		
		List<TableKeyValueDependency> allD = TableKeyValueDependency.findAll();
		HashSet<String> tablesD = new HashSet<String>();
		for (TableKeyValueDependency keyDependency : allD) {
			tablesD.add(keyDependency.table);
		}
		for (String table : tablesD) {
			TableKeyValueDependency.renewCache(table);
		}
		utils.DataBaseUtils.updateEstadosSolicitudUsuario();
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void actualizarDesdeFicheroModicando(String botonCargarTablaDeTablasModificando, String botonCargarTablaDeTablasModificandoSinMunicipios) {
		checkAuthenticity();
		if (!permisoActualizarDesdeFicheroModicando("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			if (botonCargarTablaDeTablasModificandoSinMunicipios != null)
				utils.Fixtures.updateTKVAndDependencyFromAppAndFap("app/listas/gen/", 0, true);
			else
				utils.Fixtures.updateTKVAndDependencyFromAppAndFap("app/listas/gen/", 0, false);
			actualizarCacheTablaDeTablas();
		}

		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/TablaDeTablas/TablaDeTablas.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/TablaDeTablas/TablaDeTablas.html" + " , intentada sin éxito (Problemas de Validación)");
		TablaDeTablasController.actualizarDesdeFicheroModicandoRender();
	}

}
