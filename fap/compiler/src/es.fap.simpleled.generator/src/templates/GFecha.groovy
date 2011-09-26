package templates

import es.fap.simpleled.led.*
import generator.utils.*
import generator.utils.HashStack.HashStackName

class GFecha {

	def Fecha fecha;
	
	public static String generate(Fecha fecha){
		GFecha g = new GFecha();
		g.fecha = fecha;
		g.view();
	}
	
	public String view(){
		// Añado la entidad que lo engloba a los parametros del Save
		CampoUtils campo = CampoUtils.create(fecha.campo);
		EntidadUtils.addToSaveEntity(campo);
		
		TagParameters params = new TagParameters();
		params.putStr("campo", campo.firstLower())
		if(fecha.titulo != null)
			params.putStr("titulo", fecha.titulo)
		
		if(fecha.requerido)
			params.put "requerido", true
		
		String view = 
		"""
#{fap.fecha ${params.lista()} /}		
		"""
		return view;
	}
	
}
