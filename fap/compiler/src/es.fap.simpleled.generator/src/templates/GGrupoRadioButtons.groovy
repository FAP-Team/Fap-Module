package templates;

import java.util.Set;
import java.util.Stack;

import com.sun.org.apache.bcel.internal.generic.RETURN;

import es.fap.simpleled.led.util.LedEntidadUtils
import es.fap.simpleled.led.util.LedCampoUtils
import es.fap.simpleled.led.*;
import generator.utils.*;

import fap.app.tags.*;

public class GGrupoRadioButtons extends GSaveCampoElement {
	
	GrupoRadioButtons grb;
	
	public GGrupoRadioButtons(GrupoRadioButtons grupoRadioButtons, GElement container) {
		super(grupoRadioButtons, container);
		grb = grupoRadioButtons;
		campo = CampoUtils.create(grb.campo);
	}
	
	public String getName() {
		return campo.firstLower();
	}
	
	public String getDefault() {
		return grb.valor;
	}
	
	public String view () {
		TagParameters params = new TagParameters();

		params.putStr("campo", campo.firstLower());
		
		String radioButtons = "";
		
		for (RadioButton rb: grb.getRadios()) {
			radioButtons += ((GRadioButton) getInstance(rb)).view();
		}
		
		if(grb.titulo != null)
			params.putStr("label", grb.titulo);
		else
			params.putStr("label", "");
			
		params.put("requerido", grb.requerido);

		def out = """
			#{fap.grupoRadioButton ${params.lista()}}
				${radioButtons}
			#{/fap.grupoRadioButton}
		""";
		
		return out;
	}
	
	public String validate(Stack<Set<String>> validatedFields){
		if (campo.isMethod())
			return "";
		String validation = "";
		String campoStr = StringUtils.firstLower(campo.str);
		ArrayList<String> radios = new ArrayList<String>();
		if (LedEntidadUtils.esLista(campo.getUltimoAtributo())) {
			if (LedCampoUtils.getUltimoAtributo(grb.campo).type.compound?.multiple)
				validation += validListOfValuesFromTable(campo);
			else
				validation += validValueFromTable(campo);
		} else {
			for(RadioButton rb: grb.getRadios())
				radios.add(((GRadioButton) getInstance(rb)).toString());
			if (LedEntidadUtils.getEntidad(campo.getUltimoAtributo()))
				validation += valid(campo.str, validatedFields);
			validation += valid(campo.str, validatedFields, radios);
			int dotPlace = campoStr.lastIndexOf('.', campoStr.length() - 1);
			dotPlace = campoStr.lastIndexOf('.', dotPlace - 1);
			while (dotPlace != -1) {
				validation += valid(campoStr.substring(0, dotPlace), validatedFields);
				dotPlace = campoStr.lastIndexOf('.', dotPlace - 1);
			}
			if (element.metaClass.respondsTo(element, "isRequerido") && element.isRequerido())
				validation += required(campo);
		}
		return validation;
	}
	
	private static String valid(String campo, Stack<Set<String>> validatedFields, ArrayList<String> values) {
		String toRet = "";
		campo = StringUtils.firstLower(campo);
		for (Set<String> set: validatedFields){
			if (set.contains(campo))
				return toRet;
		}
		validatedFields.peek().add(campo);
		String varName = campo.substring(campo.lastIndexOf(".") + 1) + "Aux";
		toRet += "ArrayList<String> " + varName + " = new ArrayList<String>();"
		for(String val: values)
			toRet += varName + ".add(\"" + val + "\");"
		toRet += "CustomValidation.valid(\"${campo}\", ${campo}, " + varName + ");"
		return toRet;
	}
	
	private static String validValueFromTable(CampoUtils campo){
		return "CustomValidation.validValueFromTable(\"${campo.firstLower()}\", ${campo.firstLower()});\n";
	}
	
	private static String validListOfValuesFromTable(CampoUtils campo){
		return "CustomValidation.validListOfValuesFromTable(\"${campo.firstLower()}\", ${campo.firstLower()});\n";
	}
	
}
