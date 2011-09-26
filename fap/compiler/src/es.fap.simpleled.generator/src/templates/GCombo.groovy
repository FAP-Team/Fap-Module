package templates;

import es.fap.simpleled.led.Combo
import generator.utils.*
import generator.utils.HashStack.HashStackName

public class GCombo {

	def Combo combo;
	def String id;
	
	
	public static String generate(Combo combo){
		GCombo g = new GCombo();
		g.combo = combo;
		g.view();
	}
	
	public String view(){
		// Añado la entidad que lo engloba a los parametros del Save
		CampoUtils campo = CampoUtils.create(combo.campo);
		EntidadUtils.addToSaveEntity(campo);
		
		TagParameters params = new TagParameters();
		params.putStr "campo", campo.firstLower();
		
		if(combo.name != null)
			params.putStr("id", combo.name);
		
		if(combo.titulo != null)
			params.putStr("titulo", combo.titulo)
		
		if(combo.requerido)
			params.put "requerido", true
			
		if(combo.busqueda)
			params.put "searchable", true
			
		if(combo.mostrarClave)
			params.put "mostrarClave", true
			
		if(combo.multiple)
			params.put "multiple", true
			
		if(combo.ancho != null)
			params.putStr "ancho", combo.ancho
			
		if(combo.comboTexto != null)
			params.putStr "comboText", combo.comboTexto
			
		if(combo.comboValor != null)
			params.putStr "comboValue", combo.comboValor
			
		if(combo.anchoTitulo != null)
			params.putStr("anchoTitulo", combo.anchoTitulo)

				
		String view = 
		"""
#{fap.combo ${params.lista()} /}		
		"""
		return view;
	}
	
}