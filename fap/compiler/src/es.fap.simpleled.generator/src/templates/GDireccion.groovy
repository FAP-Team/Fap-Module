package templates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
			
		if(direccion.requerido)
			params.put "requerido", true;
			
		if(direccion.ancho != null)
			params.put "ancho", direccion.ancho;

		if (direccion.elemento.equals("Direccion")){
			return """
				#{fap.direccion ${params.lista()} /}
			""";
		}
			
		if (direccion.elemento.equals("DireccionCanaria")){
			return """
				#{fap.direccionCanaria ${params.lista()} /}
			""";
		}
		
		if (direccion.elemento.equals("DireccionNacional")){
			return """
				#{fap.direccionNacional ${params.lista()} /}
			""";
		}
		
		if (direccion.elemento.equals("DireccionInternacional")){
			return """
				#{fap.direccionInternacional ${params.lista()} /}
			""";
		}
		
	}
	
	public String validate(Stack<Set<String>> validatedFields){
		String validation = "";
		if (direccion.elemento.equals("DireccionCanaria")){
			validation += """${campo.firstLower()}.tipo = "canaria";
 """
		} else if (direccion.elemento.equals("DireccionNacional")){
			validation += """${campo.firstLower()}.tipo = "nacional";
 """
		} else if (direccion.elemento.equals("DireccionInternacional")){
			validation += """${campo.firstLower()}.tipo = "internacional";
 """
		}
		validation += super.validate(validatedFields);
		validation += """if (properties.FapProperties.getBoolean("fap.direccion.anterior.version2.1")){
    //CustomValidation.validValueFromTable("${campo.addMore("municipio").firstLower()}", ${campo.addMore("municipio").firstLower()}.replace(",", "").trim());
} else if (${campo.addMore("tipo").firstLower()}.equals("canaria")){
	CustomValidation.validValueFromTable("${campo.addMore("provincia").firstLower()}", ${campo.addMore("provincia").firstLower()}.replace(",", "").trim());
	CustomValidation.validValueFromTable("${campo.addMore("isla").firstLower()}", ${campo.addMore("isla").firstLower()});
	CustomValidation.validValueFromTable("${campo.addMore("municipio").firstLower()}", ${campo.addMore("municipio").firstLower()}.replace(",", "").trim());
} else if (${campo.addMore("tipo").firstLower()}.equals("nacional")){
	CustomValidation.validValueFromTable("${campo.addMore("comunidad").firstLower()}", ${campo.addMore("comunidad").firstLower()});
	CustomValidation.validValueFromTable("${campo.addMore("provincia").firstLower()}", ${campo.addMore("provincia").firstLower()}.replace(",", "").trim());
	CustomValidation.validValueFromTable("${campo.addMore("municipio").firstLower()}", ${campo.addMore("municipio").firstLower()}.replace(",", "").trim());
} else if (${campo.addMore("tipo").firstLower()}.equals("internacional")){
	CustomValidation.validValueFromTable("${campo.addMore("pais").firstLower()}", ${campo.addMore("pais").firstLower()});
}
""";
		return validation;
	}
	
	public String copy(){
		String dev="";
		dev = copyCampos(campo);
		dev += """ if (properties.FapProperties.getBoolean("fap.direccion.anterior.version2.1")){
		${campo.dbStr()}.municipio = ${campo.firstLower()}.municipio.replace(",", "").trim();
} else if (${campo.firstLower()}.tipo.equals("canaria")){
		${campo.dbStr()}.provincia = ${campo.firstLower()}.provincia.replace(",", "").trim();
		${campo.dbStr()}.municipio = ${campo.firstLower()}.municipio.replace(",", "").trim();
		${campo.dbStr()}.pais = "_724";
		${campo.dbStr()}.comunidad = "_05";
		${campo.dbStr()}.provinciaInternacional = "";
} else if (${campo.firstLower()}.tipo.equals("nacional")){
		${campo.dbStr()}.provincia = ${campo.firstLower()}.provincia.replace(",", "").trim();
		${campo.dbStr()}.municipio = ${campo.firstLower()}.municipio.replace(",", "").trim();
		${campo.dbStr()}.pais = "_724";
		${campo.dbStr()}.isla = "";
		${campo.dbStr()}.provinciaInternacional = "";
} else if (${campo.firstLower()}.tipo.equals("internacional")){
		${campo.dbStr()}.isla = "";
		${campo.dbStr()}.provincia = "";
		${campo.dbStr()}.comunidad = "";
		${campo.dbStr()}.municipio = "";
}
"""
		return dev;
	}
	
	public String controller() {
		String nombreFuncion = (campo.firstLower()+".tipo").replaceAll("\\.", "_");
		return """public static List<tags.ComboItem> ${nombreFuncion}(Map<String, Object> args) {
		List<tags.ComboItem> result = new ArrayList<tags.ComboItem>();
		String[] tipoDireccionesProperty = FapProperties.get("fap.direcciones.tipo").split(",");
		for (String tipo : tipoDireccionesProperty){
			result.add(new tags.ComboItem(tipo.trim(), TableKeyValue.getValue("tipoDireccion", tipo.trim())));
		}
		return result;
}
""";
	}

}
