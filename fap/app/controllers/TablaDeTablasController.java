package controllers;

import controllers.gen.TablaDeTablasControllerGen;
import messages.Messages;
import models.TableKeyValue;
import play.mvc.Util;

public class TablaDeTablasController extends TablaDeTablasControllerGen {

    public static void actualizarDesdeFichero() {
        TableKeyValue.deleteAll();
        long count = TableKeyValue.loadFromFiles();
        Messages.ok("Se cargaron desde fichero " + count + " registros");
        Messages.keep();
        redirect("TablaDeTablasController.index", "editar");
    }

}
