package templates;

import es.fap.simpleled.led.*;
import generator.utils.*
import generator.utils.HashStack.HashStackName

public class GAreaTexto {

	def AreaTexto areaTexto;
	
	public static String generate(AreaTexto texto){
		GAreaTexto g = new GAreaTexto();
		g.areaTexto = texto;
		g.view();
	}

	public String view(){
		// Añado la entidad que lo engloba a los parametros del Save
		
		CampoUtils campo = CampoUtils.create(areaTexto.campo);
		
		EntidadUtils.addToSaveEntity(campo);
		
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
			
		if(areaTexto.anchoTitulo != null)
			params.putStr("anchoTitulo", areaTexto.anchoTitulo)


		String view =
		"""
#{fap.areaTexto ${params.lista()} /}		
		"""
		return view;
	}
	
}
