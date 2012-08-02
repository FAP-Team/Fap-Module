package templates;

import es.fap.simpleled.led.*
import generator.utils.*

public class GDireccionMapa extends GSaveCampoElement{
	
	DireccionMapa direccionMapa;
	
	public GDireccionMapa(DireccionMapa direccionMapa, GElement container){
		super(direccionMapa, container);
		this.direccionMapa = direccionMapa;
		campo = CampoUtils.create(direccionMapa.campo);
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		params.putStr "campo", campo.firstLower();
		
		if(direccionMapa.titulo != null)
			params.putStr("titulo", direccionMapa.titulo);
			
		if(direccionMapa.name != null)
			params.putStr "id", direccionMapa.name;
		
		if(direccionMapa.requerido)
			params.put "requerido", true;
			
		if(direccionMapa.ancho != null)
			params.put "ancho", direccionMapa.ancho;
		
		return """
				#{fap.direccionMapa ${params.lista()} /}
			""";
		
	}
	
	public String validate(Stack<Set<String>> validatedFields){
		String validation = super.validate(validatedFields);
		
		return validation;
	}

}
