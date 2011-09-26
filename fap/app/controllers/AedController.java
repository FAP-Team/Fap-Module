
package controllers;

import aed.TiposDocumentosClient;
import messages.Messages;
import play.mvc.Util;
import controllers.gen.AedControllerGen;
			
public class AedController extends AedControllerGen {

	public static void tiposDeDocumentos(){
		boolean result = TiposDocumentosClient.actualizarTiposDocumentoDB();
		if(result){
			Messages.ok("Se actualizaron correctamente los tipos de documentos desde el AED");
		}else{
			Messages.error("Se produjo un error actualizando los tipos de documentos desde el AED");
		}
		Messages.keep();
		redirect("AedController.index");
	}
	
}
