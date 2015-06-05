package templates

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.google.gson.Gson;

import es.fap.simpleled.led.ComboUO;
import es.fap.simpleled.led.Combo;
import es.fap.simpleled.led.Grupo;
import generator.utils.CampoUtils;
import generator.utils.TagParameters;
import es.fap.simpleled.led.util.LedCampoUtils;
import generator.utils.*;

class GComboUO extends GElement{

	ComboUO combo;
	CampoUtils campo;
	GElement gPaginaPopup;
	
	public GComboUO(ComboUO combo, GElement container){
		super(combo, container);
		this.combo = combo;
		campo = CampoUtils.create(combo.campo);
		gPaginaPopup = getPaginaOrPopupContainer();
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		
		params.putStr("id", combo.name);
		params.put("accion", "accion");
		params.putStr("campo", campo.firstLower());
		
		String controllerName = gPaginaPopup.controllerFullName();
		if (controllerName != null)
			params.putStr("controllerName", controllerName);
			
		if(combo.requerido)
			params.put "requerido", true;
			
		if(combo.busqueda)
			params.put "searchable", true;
	
		return """
           #{fap.comboUO ${params.lista()} /}		
		""";
	}
	
	public String controller(){
		return """
			public static String ${combo.name}handlerComboUO(int codigo, int subnivel){
				return ServiciosGenericosUtils.handlerComboUO(codigo, subnivel);
			}
		""";
	}
	
	public String validateCopy(Stack<Set<String>> validatedFields){
		Entidad entidadRaiz = campo.getEntidad();
		String validation = super.validate(validatedFields);
		validation += "CustomValidation.valid(\"${campo.sinUltimoAtributo()}\", ${campo.sinUltimoAtributo()});\n";
		if (combo.requerido)
			validation += "CustomValidation.required(\"${campo.firstLower()}\", ${campo.firstLower()});\n";
		validation += """   Long ${combo.name}codigoUO = ${campo.firstLower()};
							if (${combo.name}codigoUO != null) {
								${entidadRaiz.variableDb}.${campo.sinEntidad(campo.sinUltimoAtributo())} = ServiciosGenericosUtils.getUnidadOrganicaFAP(${combo.name}codigoUO);
								${entidadRaiz.variableDb}.${campo.sinEntidad()} = ${combo.name}codigoUO;
							} 
		              """;
		return validation;
	}
}
