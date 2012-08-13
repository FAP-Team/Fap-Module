package templates;

import es.fap.simpleled.led.*;
import generator.utils.*

public class GAreaTexto extends GSaveCampoElement{

	AreaTexto areaTexto;
	
	public GAreaTexto(AreaTexto texto, GElement container){
		super(texto, container);
		this.areaTexto = texto;
		campo = CampoUtils.create(texto.campo);
	}

	public String view(){
		TagParameters params = new TagParameters();
		params.putStr("campo", campo.firstLower())
		if(areaTexto.titulo != null)
			params.putStr("titulo", areaTexto.titulo)

		if(areaTexto.valor != null)
			params.putStr("valor", areaTexto.valor)

		if(areaTexto.requerido)
			params.put "requerido", true
		if(areaTexto.name != null)
			params.putStr "id", areaTexto.name

		if(areaTexto.filas > 0 )
			params.putStr "filas", areaTexto.filas.toString()
			
		if (areaTexto.ayuda != null) {
			if ((areaTexto.tipoAyuda != null) && ((areaTexto.tipoAyuda.type.equals("propover")) || (areaTexto.tipoAyuda.type.equals("popover"))))
				params.put "ayuda", "tags.TagAyuda.popover('${areaTexto.ayuda}')"
			else
				params.put "ayuda", "tags.TagAyuda.texto('${areaTexto.ayuda}')"
		}
		
		if(areaTexto.ancho != null)
			params.putStr "ancho", areaTexto.ancho
			
		if(areaTexto.anchoTitulo != null)
			params.putStr("anchoTitulo", areaTexto.anchoTitulo)

		return """
			#{fap.areaTexto ${params.lista()} /}		
		""";
	}
	
}
