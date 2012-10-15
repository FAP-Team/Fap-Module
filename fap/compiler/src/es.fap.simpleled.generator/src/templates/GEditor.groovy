package templates;

import es.fap.simpleled.led.*;
import generator.utils.*

public class GEditor extends GSaveCampoElement{

	Editor editor;
	
	public GEditor(Editor editor, GElement container){
		super(editor, container);
		this.editor = editor;
		campo = CampoUtils.create(editor.campo);
	}

	public String view(){
		TagParameters params = new TagParameters();
		params.putStr("campo", campo.firstLower());

		return """
			#{fap.editor ${params.lista()} /}		
		""";
	}
	
}
