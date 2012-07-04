package controllers.fap;

import play.mvc.*;
import utils.DocumentosUtils;

public class UtilsController extends Controller {

    public static boolean documentoEsMultiple(String tipoUri) {
    	return DocumentosUtils.esTipoMultiple(tipoUri);
    }

}