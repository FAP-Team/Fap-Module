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
			
		if(combo.ancho != null)
			params.putStr "ancho", combo.ancho
			
		if(combo.comboTexto != null)
			params.putStr "comboText", combo.comboTexto.name
			
		if(combo.comboValor != null)
			params.putStr "comboValue", combo.comboValor.name
			
		if(combo.anchoTitulo != null)
			params.putStr("anchoTitulo", combo.anchoTitulo)
		
			
		if (combo.ordenar != null) {
			if (combo.ordenar.equals("ordenarPorClave"))
				params.put "ordenarPorClave", true
			else if (combo.ordenar.equals("ordenarPorValor"))
				params.put "ordenarPorTexto", true
		}
		
		if(combo.eliminarDuplicados)
			params.put "eliminarDuplicados", true

		/// Si el atributo es una referencia Many2One o ManyToMany
		if (campo.getUltimoAtributo().type?.compound?.tipoReferencia?.type?.equals("ManyToMany")
			|| campo.getUltimoAtributo()?.type?.compound?.tipoReferencia?.type?.equals("ManyToOne")) {
			params.put "manyTo", true
		}
				
		String view = 
		"""
#{fap.combo ${params.lista()} /}		
		"""
		return view;
	}
	
}