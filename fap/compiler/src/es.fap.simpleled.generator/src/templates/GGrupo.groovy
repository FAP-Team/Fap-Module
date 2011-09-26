package templates;

import es.fap.simpleled.led.*;
import generator.utils.CampoUtils
import generator.utils.ListUtils;
import generator.utils.TagParameters;

public class GGrupo {
	def Grupo grupo;
	
	public static String generate(Grupo grupo){
		GGrupo g = new GGrupo();
		g.grupo = grupo;
		g.view();
	}
	
	public String view(){
		String elementos = "";
		for(Elemento elemento : grupo.getElementos()){
			elementos += Expand.expand(elemento);
		}
		
		TagParameters params = new TagParameters();
		if(grupo.titulo != null)
			params.putStr("titulo", grupo.titulo);
		
		if(grupo.siCombo != null){
			params.putStr("siCombo", grupo.siCombo.name) 
			params.put("siComboValue", ListUtils.list2GroovyListString(grupo.siComboValues.values))	
		}
		
		if(grupo.siCheck != null){
			params.putStr("siCheck", grupo.siCheck.name)
			params.put("siCheckValue", grupo.siCheckValues)
		}
		
		if(grupo.campo != null){
			def valores = ListUtils.list2GroovyListString(grupo.siCampoValues.values);
			CampoUtils campo = CampoUtils.create(grupo.campo);
			params.put("mostrarSi",  "${valores}.contains(${campo.firstLower()})")	
		}

		if(grupo.siExpresion != null){
			params.put("mostrarSi", grupo.siExpresion)
		}
		
		if (grupo.visible) {
			params.put("visible", grupo.visible.toBoolean());
		}
		

		if (grupo.permiso != null) {
			params.putStr("permiso", grupo.permiso.name);
		}
		
		
		def out = """
#{fap.grupo ${params.lista()}}
	${elementos}
#{/fap.grupo}
"""
	
		return out;	
	}

}
