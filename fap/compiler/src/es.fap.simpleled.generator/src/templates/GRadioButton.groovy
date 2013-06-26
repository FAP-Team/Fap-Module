package templates;

import java.util.Set;
import java.util.Stack;

import es.fap.simpleled.led.*;
import generator.utils.TagParameters;

public class GRadioButton extends GElement {
	
	RadioButton rb;
	String parent;
	String parentCampo;
	String parentDefault;
	
	public GRadioButton(RadioButton radioButton, GElement container) {
		super(radioButton, container);
		parent = container.getName();
		parentCampo = container.campo.firstLower();
		parentDefault = container.getDefault();
		rb = radioButton;
	}
	
	public String view () {
		TagParameters params = new TagParameters();
		
		params.putStr("campo", parentCampo);
		
		params.putStr("parent", parent);
		
		if(rb.name != null)
			params.putStr("id", rb.name)
			
		if ((parentDefault != null) && (parentDefault == rb.name))
			params.put("default", true);
		else
			params.put("default", false);
		
		if(rb.valor != null)
			params.putStr("valor", rb.valor);
		
		if (rb.titulo != null)
			params.putStr("titulo", rb.titulo);
			
		return """
			#{fap.radioButton ${params.lista()} /}
		""";
	}
	
	public String toString () {
		return rb.valor
	}

}