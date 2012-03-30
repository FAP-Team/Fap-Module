package templates;

import es.fap.simpleled.led.impl.EnlaceImpl;

import es.fap.simpleled.led.AgrupaBotones
import es.fap.simpleled.led.Enlace;
import generator.utils.*;
import es.fap.simpleled.led.Boton;

public class GAgrupaBotones extends GElement{
	
	AgrupaBotones agrupa;
	
	public GAgrupaBotones(AgrupaBotones grupoBotones, GElement container){
		super(grupoBotones, container);
		this.agrupa = grupoBotones;
	}

    public String view(){
		String clazz = "actions button_container";
		if (agrupa.type == "well")
			clazz = "well";
		String result = ""
		result += """<div class="${clazz}">
		""";
		for (Boton b: agrupa.botones)
			result += getInstance(b).view();
		result += """</div>
		""";
        return result;
    }
	
}
