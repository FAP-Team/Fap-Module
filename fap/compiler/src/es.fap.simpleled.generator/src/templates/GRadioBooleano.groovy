package templates;

import es.fap.simpleled.led.*
import generator.utils.*

public class GRadioBooleano extends GSaveCampoElement{

	RadioBooleano radio;
	
	public GRadioBooleano(RadioBooleano radio, GElement container){
		super(radio, container);
		this.radio = radio;
		campo = CampoUtils.create(radio.campo);
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		params.putStr("campo", campo.firstLower());
		
		if(radio.name != null)
			params.putStr("id", radio.name)
			
		if(radio.titulo != null)
			params.putStr("titulo", radio.titulo)
			
		if(radio.anchoTitulo != null)
			params.putStr("anchoTitulo", radio.anchoTitulo)
			
		if(radio.botonDerecho != null)
			params.putStr("labelRight", radio.botonDerecho)
			
		if(radio.botonIzquierdo != null)
			params.putStr("labelLeft", radio.botonIzquierdo)
		
		if(radio.alineadoAIzquierdas)
			params.put "alineadoAIzquierdas", true;
		
		if (radio.ayuda != null) {
			if ((radio.tipoAyuda != null) && ((radio.tipoAyuda.type.equals("propover")) || (radio.tipoAyuda.type.equals("popover"))))
				params.put "ayuda", "tags.TagAyuda.popover('${radio.ayuda}')"
			else
				params.put "ayuda", "tags.TagAyuda.texto('${radio.ayuda}')"
		}
		
		return """
			#{fap.radioBooleano ${params.lista()} /}		
		""";
	}	
}
