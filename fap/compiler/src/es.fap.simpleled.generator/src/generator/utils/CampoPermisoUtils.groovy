package generator.utils;

import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.CampoPermiso
import es.fap.simpleled.led.CampoPermisoAtributos
import es.fap.simpleled.led.Entity;

public class CampoPermisoUtils {

	CampoPermiso campo;
	String str;
	
	public static CampoPermisoUtils create(CampoPermiso campo){
		if (campo == null){
			return null;
		}
		CampoPermisoUtils field = new CampoPermisoUtils();
		field.campo = campo;
		field.str = getCampoStr(campo);
		return field;
	}
	
	public static String getCampoStr(CampoPermiso campo){
		if (campo == null){
			return null;
		}
		if (campo.isAction()){
			return "accion";
		}
		String campoStr = "";
		if (campo.isAgente()){
			campoStr += "agente";
		}
		else{
			campoStr = campo.getVariable().getName();
		}
		CampoPermisoAtributos attrs = campo.getAtributos();
		while (attrs != null){
			campoStr += "." + attrs.getAtributo().getName();
			attrs = attrs.getAtributos();
		}
		return campoStr;
	}
	
}
