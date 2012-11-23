package templates;

import es.fap.simpleled.led.Texto
import es.fap.simpleled.led.TextoOculto
import generator.utils.*
import es.fap.simpleled.led.util.LedEntidadUtils

public class GTextoOculto extends GSaveCampoElement{

	TextoOculto textoOculto;
	
	public GTextoOculto(TextoOculto textoOculto, GElement container){
		super(textoOculto, container);
		this.textoOculto = textoOculto;
		campo = CampoUtils.create(textoOculto.campo);
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		params.putStr("campo", campo.firstLower())

		if(textoOculto.name != null)
			params.putStr "id", textoOculto.name
			
		return """
			#{fap.textoOculto ${params.lista()} /}		
		""";
	}
	
}
