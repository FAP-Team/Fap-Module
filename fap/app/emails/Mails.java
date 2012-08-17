package emails;

import java.util.*;

import utils.Fixtures;
import utils.TemplateUtils;
import play.Logger;
import models.*;

public class Mails {
			
	public static void enviar(String idMail, Object... args) {
		Map<String, Object> argsMap = TemplateUtils.obtenerArg(args);
		Mail email = Mail.find("select mail from Mail mail where mail.idMail=?", idMail).first();
		if(email != null){
			email.send(argsMap);
		}else{
			try{
			throw new IllegalArgumentException("No se encontró el ID del mail en la base de datos");
			}
			catch (Exception e) {
				play.Logger.error("No se encontró el mail en la base de datos");
			}
		}
	}
	
	public static long loadFromFiles(){
        Fixtures.loadFolderFromAppAndFap("app/emails/initial-data/");
		return Mail.count();
    }
 }