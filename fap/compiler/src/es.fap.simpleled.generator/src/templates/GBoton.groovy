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

		//params.put "idSolicitud", "idSolicitud"
			
		String result = ""
			
		if (boton.pagina != null) {   // envolvemos el botón dentro de un enlace (tag <a>)
			Enlace enlace = new EnlaceImpl();
			enlace.name = (boton.name ?: "")+"IDenlace" 
			enlace.titulo = boton.titulo
			enlace.pagina = boton.pagina
			enlace.estilo = "boton"
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
