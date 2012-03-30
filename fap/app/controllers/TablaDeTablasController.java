package controllers;

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
        Messages.ok("Se cargaron desde fichero " + count + " registros, para la tabla de tablas");
        count = TableKeyValueDependency.loadFromFiles(true);
        Messages.ok("Se cargaron desde fichero " + count + " registros, para la tabla de tablas de dependencias");
        Messages.keep();
        redirect("TablaDeTablasController.index", "editar");
    }

}
