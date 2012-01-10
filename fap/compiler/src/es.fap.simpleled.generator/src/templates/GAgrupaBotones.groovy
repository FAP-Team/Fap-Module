package templates;

import es.fap.simpleled.led.impl.EnlaceImpl;

import es.fap.simpleled.led.AgrupaBotones
import es.fap.simpleled.led.Enlace;
import generator.utils.*;
import generator.utils.HashStack.HashStackName;
import es.fap.simpleled.led.Boton;

public class GAgrupaBotones {
	
	AgrupaBotones agrupaB;
	
	public static String generate(AgrupaBotones grupoBotones){
		GAgrupaBotones g = new GAgrupaBotones();
		g.agrupaB = grupoBotones;
		g.view();
	}

    public String view(){
		String result = ""
		result += """<div class="actions button_container">
		"""
		for (Boton b : agrupaB.botones) {
			result += GBoton.generate(b);
		}
		result += """</div>
		"""
        return result;
    }
	
}
