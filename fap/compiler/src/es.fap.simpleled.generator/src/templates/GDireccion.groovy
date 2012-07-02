package templates;

import es.fap.simpleled.led.*
import generator.utils.*

public class GDireccion extends GSaveCampoElement{
	
	Direccion direccion;
	
	public GDireccion(Direccion direccion, GElement container){
		super(direccion, container);
		this.direccion = direccion;
		campo = CampoUtils.create(direccion.campo);
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		params.putStr "campo", campo.firstLower();
		params.putStr "elemento", direccion.elemento;
		
		if(direccion.titulo != null)
			params.putStr("titulo", direccion.titulo);
			
		if(direccion.name != null)
			params.putStr "id", direccion.name;

		if (direccion.elemento == "Direccion"){
			if(direccion.isla != null)
				params.put "isla", direccion.isla;
				
			if(direccion.provincia != null)
				params.put "provincia", direccion.provincia;
			
			if(direccion.pais != null)
				params.put "pais", direccion.pais;
			
			if(direccion.requerido)
				params.put "requerido", true;
			
			if(direccion.ancho != null)
				params.put "ancho", direccion.ancho;
		}
			
		if (direccion.elemento == "DireccionCanaria"){
			if(direccion.isla != null)
				params.put "isla", direccion.isla;
		}
		
		if (direccion.elemento == ("DireccionNacional" || "DireccionInternacional")){
			if(direccion.provincia != null)
				params.put "provincia", direccion.provincia;
		}
		
		if (direccion.elemento == ("DireccionInternacional")){
			if(direccion.pais != null)
				params.put "pais", direccion.pais;
		}
		
		if(direccion.requerido)
			params.put "requerido", true;
			
		if(direccion.ancho != null)
			params.put "ancho", direccion.ancho;
		
		return """
				#{fap.direccion ${params.lista()} /}
			""";
		
	}
	
	public String validate(Stack<Set<String>> validatedFields){
		String validation = super.validate(validatedFields);
		validation += GCombo.validValueFromTable(campo.addMore("municipio"));
		
		if(direccion.elemento == "Direccion"){
			if (direccion.isla)
				validation += GCombo.validValueFromTable(campo.addMore("isla"));
			if (direccion.provincia)
				validation += GCombo.validValueFromTable(campo.addMore("provincia"));
			if (direccion.pais)
				validation += GCombo.validValueFromTable(campo.addMore("pais"));
		}
		
		if (direccion.elemento == "DireccionCanaria"){
			validation += GCombo.validValueFromTable(campo.addMore("isla"));
		}
		
		if (direccion.elemento == ("DireccionNacional" || "DireccionInternacional")){
			validation += GCombo.validValueFromTable(campo.addMore("provincia"));
		}
		
		if (direccion.elemento == ("DireccionInternacional")){
			validation += GCombo.validValueFromTable(campo.addMore("pais"));
		}
		
		return validation;
	}

}
