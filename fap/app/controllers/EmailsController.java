
package controllers;

import messages.Messages;
import models.Mail;
import play.mvc.Util;
import controllers.gen.EmailsControllerGen;
import emails.Mails;
			
public class EmailsController extends EmailsControllerGen {

    public static void actualizarDesdeFichero() {
        Mail.deleteAll();
        long count = Mails.loadFromFiles();
        Messages.ok("Se cargaron desde fichero " + count + " registros");
        Messages.keep();
        redirect( "EmailsController.index" );
    }
	
}
