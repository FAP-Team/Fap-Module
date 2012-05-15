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
        utils.DataBaseUtils.updateEstadosSolicitudUsuario();
        Messages.ok("Se cargaron los estados de visibilidad de la Solicitud");
        Messages.keep();
        redirect("TablaDeTablasController.index");
    }

}
