package templates;

import es.fap.simpleled.led.Combo
import es.fap.simpleled.led.util.LedCampoUtils
import generator.utils.*


public class GCombo extends GSaveCampoElement{

	Combo combo;
	
	public GCombo(Combo combo, GElement container){
		super(combo, container);
		this.combo = combo;
		campo = CampoUtils.create(combo.campo);
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		params.putStr "campo", campo.firstLower();
		
		if(combo.name != null)
			params.putStr("id", combo.name);
		if(combo.titulo != null)
			params.putStr("titulo", combo.titulo);
		if(combo.requerido)
			params.put "requerido", true;
		if(combo.busqueda)
			params.put "searchable", true;
		if(combo.mostrarClave)
			params.put "mostrarClave", true;
		if(combo.ancho != null)
			params.putStr "ancho", combo.ancho;
		if(combo.comboTexto != null)
			params.putStr "comboText", combo.comboTexto.name;
		if(combo.comboValor != null)
			params.putStr "comboValue", combo.comboValor.name;
		if(combo.anchoTitulo != null)
			params.putStr("anchoTitulo", combo.anchoTitulo);
		if (combo.dependeDe != null)
			params.putStr "dependeDe", combo.dependeDe.name;
		
		if (combo.ordenar != null) {
			if (combo.ordenar.equals("ordenarPorClave"))
				params.put "ordenarPorClave", true;
			else if (combo.ordenar.equals("ordenarPorValor"))
				params.put "ordenarPorTexto", true;
		}

		if (combo.ayuda != null) {
			if ((combo.tipoAyuda != null) && ((combo.tipoAyuda.type.equals("propover")) || (combo.tipoAyuda.type.equals("popover"))))
				params.put "ayuda", "tags.TagAyuda.popover('${combo.ayuda}')";
			else
				params.put "ayuda", "tags.TagAyuda.texto('${combo.ayuda}')";
		}
		
		if(combo.eliminarDuplicados)
			params.put "eliminarDuplicados", true;
			
		/// Si el atributo es una referencia Many2One o ManyToMany
		if (campo.getUltimoAtributo().type?.compound?.tipoReferencia?.type?.equals("ManyToMany")
			|| campo.getUltimoAtributo()?.type?.compound?.tipoReferencia?.type?.equals("ManyToOne")) {
			params.put "manyTo", true;
		}
				
		return """
			#{fap.combo ${params.lista()} /}		
		""";
	}
	
	public String validate(Stack<Set<String>> validatedFields){
		String validation = super.validate(validatedFields);
		if (LedCampoUtils.getUltimoAtributo(combo.campo).type.compound?.multiple)
			validation += validListOfValuesFromTable(campo);
		else
			validation += validValueFromTable(campo);
		return validation;
	}
	
	private static String validValueFromTable(CampoUtils campo){
		return "CustomValidation.validValueFromTable(\"${campo.firstLower()}\", ${campo.firstLower()});\n";
	}
	
	private static String validListOfValuesFromTable(CampoUtils campo){
		return "CustomValidation.validListOfValuesFromTable(\"${campo.firstLower()}\", ${campo.firstLower()});\n";
	}
}