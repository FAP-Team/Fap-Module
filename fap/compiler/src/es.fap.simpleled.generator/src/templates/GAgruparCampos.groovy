package templates;

import es.fap.simpleled.led.*;
import generator.utils.Entidad;
import generator.utils.ListUtils;
import generator.utils.TagParameters;

public class GAgruparCampos extends GGroupElement{
	
	AgruparCampos agruparCampos;
	
	public GAgruparCampos(AgruparCampos agruparCampos, GElement container){
		super(agruparCampos, container);
		this.agruparCampos = agruparCampos;
		elementos = agruparCampos.getElementos();
	}
	
	public String view(){
		String elementos = "";
		for(Elemento elemento : agruparCampos.getElementos()){
			elementos += getInstance(elemento).view();
		}
		return """
			#{fap.agruparCampos}
				${elementos}
			#{/fap.agruparCampos}
		""";
	}
	
}
