package templates;

import es.fap.simpleled.led.impl.EnlaceImpl;

import es.fap.simpleled.led.Enlace;
import generator.utils.*;
import generator.utils.HashStack.HashStackName;
import es.fap.simpleled.led.Boton;

public class GBoton {

	def Boton boton;
	
	public static String generate(Boton boton){
		GBoton g = new GBoton();
		g.boton = boton;
		g.view();
	}

    public String view(){
        TagParameters params = new TagParameters();
        params.putStr("id", boton.name)
        params.putStr("titulo", boton.titulo)
        if (boton.ancho != null)
            params.put "ancho", boton.ancho
        if (boton.popup != null)
            params.putStr "popup", boton.popup.name
		if (boton.isWaitPopup())
			params.put "waitPopup", boton.isWaitPopup()
		if (boton.type != null)
			params.putStr "type", boton.type
		
		if (boton.ayuda != null) {
			if ((boton.tipoAyuda != null) && (boton.tipoAyuda.type.equals("propover")))
				params.put "ayuda", "tags.TagAyuda.popover('${boton.ayuda}')"
			else
				params.put "ayuda", "tags.TagAyuda.texto('${boton.ayuda}')"
		}
		//params.put "idSolicitud", "idSolicitud"
			
		String result = ""
			
		if (boton.pagina != null) {   // envolvemos el botón dentro de un enlace (tag <a>)
			Enlace enlace = new EnlaceImpl();
			enlace.name = (boton.name ?: "")+"IDenlace" 
			enlace.titulo = result
			enlace.pagina = boton.pagina
			enlace.estilo = "btn ${boton.type}"
			result = Expand.expand(enlace)
		} 
		else {
			HashStack.push(HashStackName.SAVE_BOTON, boton.name);
			result ="""
#{fap.boton ${params.lista()} /}
			"""
		}
		
        return result;
    }
	
}
