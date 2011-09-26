package templates;

import es.fap.simpleled.led.Texto
import generator.utils.*
import generator.utils.HashStack.HashStackName

public class GTexto {

	def Texto texto;
	
	public static String generate(Texto texto){
		GTexto g = new GTexto();
		g.texto = texto;
		g.view();
	}
	
	public String view(){
		// Añado la entidad que lo engloba a los parametros del Save
		CampoUtils campo = CampoUtils.create(texto.campo);
		EntidadUtils.addToSaveEntity(campo);
		
		TagParameters params = new TagParameters();
		params.putStr("campo", campo.firstLower())
		if(texto.titulo != null)
			params.putStr("titulo", texto.titulo)
		
		if(texto.requerido)
			params.put "requerido", true
			
		if(texto.name != null)
			params.putStr "id", texto.name
		
		if (texto.ayuda != null)
			params.put "ayuda", "tags.TagAyuda.texto('${texto.ayuda}')"
			
		if(texto.ancho != null)
			params.putStr "ancho", texto.ancho
			
		if(texto.anchoTitulo != null)
			params.putStr("anchoTitulo", texto.anchoTitulo)

		String view = 
		"""
#{fap.texto ${params.lista()} /}		
		"""
		return view;
	}
	
}
