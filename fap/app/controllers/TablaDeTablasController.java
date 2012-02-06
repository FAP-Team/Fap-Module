package controllers;

import controllers.gen.TablaDeTablasControllerGen;
import messages.Messages;
import models.TableKeyValue;
import models.TableKeyValueDependency;
import play.mvc.Util;

public class TablaDeTablasController extends TablaDeTablasControllerGen {

    public static void actualizarDesdeFichero() {
        TableKeyValue.deleteAll();
        long count = TableKeyValue.loadFromFiles();
        Messages.ok("Se cargaron desde fichero " + count + " registros, para la tabla de tablas");
        TableKeyValueDependency.deleteAll();
        count = TableKeyValueDependency.loadFromFiles();
        Messages.ok("Se cargaron desde fichero " + count + " registros, para la tabla de tablas de dependencias");
        Messages.keep();
        redirect("TablaDeTablasController.index", "editar");
    }

}
