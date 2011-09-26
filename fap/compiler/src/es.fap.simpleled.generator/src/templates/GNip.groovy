package templates;

import es.fap.simpleled.led.*
import generator.utils.*
import generator.utils.HashStack.HashStackName

public class GNip {
	def Nip nip
	
	public static String generate(Nip nip){
		GNip g = new GNip();
		g.nip = nip;
		return g.view();
	}
	
	public String view(){
		// Añado la entidad que lo engloba a los parametros del Save
		CampoUtils campo = CampoUtils.create(nip.campo);
		EntidadUtils.addToSaveEntity(campo);
		
		TagParameters params = new TagParameters();
		if(nip.name != null)
			params.putStr "id", nip.name
			
		if(nip.titulo != null)
			params.putStr("titulo", nip.titulo)
		
		if(nip.requerido)
			params.put "requerido", true
				
		params.putStr "campo", campo.firstLower();
		
		String view =
		"""
#{fap.nip ${params.lista()} /}
		"""
		return view;
	}
}
