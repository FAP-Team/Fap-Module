package templates;

import es.fap.simpleled.led.*
import generator.utils.*
import generator.utils.HashStack.HashStackName

public class GCheck {

	def Check check;
	
	public static String generate(Check check){
		GCheck g = new GCheck();
		g.check = check;
		g.view();
	}
	
	public String view(){
		// Añado la entidad que lo engloba a los parametros del Save
		CampoUtils campo = CampoUtils.create(check.campo);
		EntidadUtils.addToSaveEntity(campo);
		
		TagParameters params = new TagParameters();
		params.putStr("campo", campo.firstLower());
		
		if(check.titulo != null)
			params.putStr("titulo", check.titulo)
		
		if(check.name != null)
			params.putStr("id", check.name)	
			
		if(check.anchoTitulo != null)
			params.putStr("anchoTitulo", check.anchoTitulo)
		
			
		String view = 
		"""
#{fap.check ${params.lista()} /}		
		"""
		return view;
	}	
}
