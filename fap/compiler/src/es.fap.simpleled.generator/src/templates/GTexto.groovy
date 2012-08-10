package templates;

import es.fap.simpleled.led.Texto
import generator.utils.*
import es.fap.simpleled.led.util.LedEntidadUtils

public class GTexto extends GSaveCampoElement{

	Texto texto;
	
	public GTexto(Texto texto, GElement container){
		super(texto, container);
		this.texto = texto;
		campo = CampoUtils.create(texto.campo);
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		params.putStr("campo", campo.firstLower())
		if (LedEntidadUtils.getSimpleTipo(campo.getUltimoAtributo()).equals("Double"))
			params.putStr ("format", "double")
		if (LedEntidadUtils.getSimpleTipo(campo.getUltimoAtributo()).equals("Moneda"))
			params.putStr ("format", "moneda")
		if(texto.titulo != null)
			params.putStr("titulo", texto.titulo)
		
		if(texto.requerido)
			params.put "requerido", true
			
	    if(texto.password)
			params.put "password", true //<input type="password"> en vez de <input type="text">
			
		if(texto.name != null)
			params.putStr "id", texto.name
		
		if (texto.ayuda != null) {
			if ((texto.tipoAyuda != null) && (texto.tipoAyuda.type.equals("propover")))
				params.put "ayuda", "tags.TagAyuda.popover('${texto.ayuda}')"
			else
				params.put "ayuda", "tags.TagAyuda.texto('${texto.ayuda}')"
		}
			
		if(texto.ancho != null)
			params.putStr "ancho", texto.ancho
			
		if(texto.anchoTitulo != null)
			params.putStr("anchoTitulo", texto.anchoTitulo)
			
		if((texto.duplicar != null) && (texto.duplicar == true))
			params.put ("duplicado", true)

		return """
			#{fap.texto ${params.lista()} /}		
		""";
	}
	
	public String validate(Stack<Set<String>> validatedFields){
		String validation = super.validate(validatedFields);
		if (texto.isDuplicar()){
			String campo_ = campo.firstLower().replaceAll("\\.", "_");
			validation += "CustomValidation.compare(${campo.firstLower()}, params.get(\"${campo_}copy\"));\n";
		}
		return validation;
	}
	
}
