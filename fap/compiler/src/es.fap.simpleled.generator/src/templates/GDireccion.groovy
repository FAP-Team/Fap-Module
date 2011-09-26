package templates;

import es.fap.simpleled.led.*
import generator.utils.*
import generator.utils.HashStack.HashStackName

public class GDireccion {
	def Direccion direccion
	
	public static String generate(Direccion direccion){
		GDireccion g = new GDireccion();
		g.direccion = direccion;
		return g.view();
	}
	
	public String view(){
		// Añado la entidad que lo engloba a los parametros del Save
		CampoUtils campo = CampoUtils.create(direccion.campo);
		EntidadUtils.addToSaveEntity(campo);
		
		TagParameters params = new TagParameters();
		params.putStr "campo", campo.firstLower()
		
		if(direccion.titulo != null)
			params.putStr("titulo", direccion.titulo)
			
		if(direccion.name != null)
			params.putStr "id", direccion.name
					
		if(direccion.provincia != null)
			params.put "provincia", direccion.provincia
		
		if(direccion.pais != null)
			params.put "pais", direccion.pais
		
		if(direccion.requerido)
			params.put "requerido", true
		
		if(direccion.ancho != null)
			params.put "ancho", direccion.ancho

		String view =
		"""
#{fap.direccion ${params.lista()} /}
		"""
		return view;
	}
}
