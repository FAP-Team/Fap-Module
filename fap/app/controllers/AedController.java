
package controllers;

import aed.TiposDocumentosClient;
import messages.Messages;
import play.mvc.Util;
import controllers.gen.AedControllerGen;
			
public class AedController extends AedControllerGen {

	public static void actualizarTramites(){
		boolean result = TiposDocumentosClient.actualizarTramites();
		if (result)
			Messages.ok("Se actualizaron correctamente los tipos de documentos desde el AED");
		else
				Messages.error("Se produjo un error actualizando los tipos de documentos desde el AED");
		
		result = TiposDocumentosClient.actualizarTiposDocumentoDB();
		if (result)
			Messages.ok("Se actualizaron correctamente los tipos de documentos desde el AED en la BBDD, según los trámites");
		else
			Messages.error("Se produjo un error actualizando los tipos de documentos desde el AED en la BBDD, según los trámites");
		
		result = TiposDocumentosClient.actualizarCodigosExclusion();
		if (result)
			Messages.ok("Se actualizaron correctamente los tipos de codigos de exclusión desde el AED en la BBDD");
		else
			Messages.error("Se produjo un error actualizando los tipos de códigos de exclusión desde el AED en la BBDD");
		Messages.keep();
		redirect("AedController.index");
	}
	
}
