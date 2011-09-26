package templates;

import es.fap.simpleled.led.*;
import generator.utils.ListUtils;
import generator.utils.TagParameters;

public class GAgruparCampos {
	def AgruparCampos agruparCampos;
	
	public static String generate(AgruparCampos agruparCampos){
		GAgruparCampos g = new GAgruparCampos();
		g.agruparCampos = agruparCampos;
		g.view();
	}
	
	public String view(){
		String elementos = "";
		for(Elemento elemento : agruparCampos.getElementos()){
			elementos += Expand.expand(elemento);
		}
		
		def out = """
#{fap.agruparCampos}
	${elementos}
#{/fap.agruparCampos}
"""
	
		return out;	
	}
}
