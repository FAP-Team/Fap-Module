package templates;

import es.fap.simpleled.led.VisorFactura
import generator.utils.*;


public class GVisorFactura extends GSaveCampoElement {
	
	VisorFactura VisorFactura;
	
	public GVisorFactura(VisorFactura visorFactura, GElement container) {
		super(visorFactura, container);
		this.visorFactura = visorFactura;
		campo = CampoUtils.create(visorFactura.campo);
	}
	
	public String view() {
		TagParameters params = new TagParameters()
		if(visorFactura.name != null)
			params.putStr("id", visorFactura.name)
		params.putStr("campo", campo.firstLower())
		println("Campo: " + campo.firstLower());
		return "#{fap.visorfactura ${params.lista()} /}	";
	}
	
}