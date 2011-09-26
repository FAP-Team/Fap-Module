package templates

import es.fap.simpleled.led.*;
import generator.utils.HashStack;
import generator.utils.HashStack.HashStackName;

class GFormulario {

	public static String generate(Formulario formulario){
		
		if (formulario.name.equals("SolicitudGenerico")){
			formulario.name = "Solicitud";
		}
		
		HashStack.push(HashStackName.FORMULARIO, formulario)
		
		if ((formulario.permiso != null)) {
			HashStack.push(HashStackName.PERMISSION, formulario.permiso);
		}
		
		if(formulario.menu != null)
			Expand.expand(formulario.menu);
		
		for(Pagina pagina : formulario.getPaginas()){
			Expand.expand(pagina);
		}
		
		for(Popup popup : formulario.getPopups()){
			Expand.expand(popup);
		}
		
		if ((formulario.permiso != null)) {
			HashStack.pop(HashStackName.PERMISSION);
		}
		HashStack.pop(HashStackName.FORMULARIO)
	}
}
