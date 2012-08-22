package templates;

import es.fap.simpleled.led.*
import generator.utils.*

public class GBarraDeslizante extends GSaveCampoElement{

	BarraDeslizante barra;
	
	public GBarraDeslizante(BarraDeslizante barra, GElement container){
		super(barra, container);
		this.barra = barra;
		campo = CampoUtils.create(barra.campo);
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		params.putStr("campo", campo.firstLower());
		
		if(barra.name != null)
			params.putStr("id", barra.name)
			
		if(barra.titulo != null)
			params.putStr("titulo", barra.titulo)
			
		if(barra.ancho != null)
			params.putStr("ancho", barra.ancho)
			
		if(barra.minimo != null)
			params.put("minimo", barra.minimo)
			
		if(barra.maximo != null)
			params.put("maximo", barra.maximo)
		
		if(barra.valorDefecto != null)
			params.put("valorDefecto", barra.valorDefecto)
			
		if(barra.alineadoAIzquierdas)
			params.put "alineadoAIzquierdas", true;
		
		if (barra.ayuda != null) {
			if ((barra.tipoAyuda != null) && (barra.tipoAyuda.type.equals("popover")))
				params.put "ayuda", "tags.TagAyuda.popover('${barra.ayuda}')"
			else
				params.put "ayuda", "tags.TagAyuda.texto('${barra.ayuda}')"
		}
		
		return """
			#{fap.barraDeslizante ${params.lista()} /}		
		""";
	}
	
}
