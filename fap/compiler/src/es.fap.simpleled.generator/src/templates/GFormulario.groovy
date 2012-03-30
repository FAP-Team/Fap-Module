package templates

import es.fap.simpleled.led.*;

class GFormulario extends GElement{

	Formulario formulario;
	
	public GFormulario(Formulario formulario, GElement container){
		super(formulario, container);
		this.formulario = formulario;
	}
		
	public void generate(){
		for(int i = 0; i < formulario.getPaginas().size(); i++)
			GElement.getInstance(formulario.getPaginas().get(i), null).generate();
			
		for(int i = 0; i < formulario.getPopups().size(); i++)
			GElement.getInstance(formulario.getPopups().get(i), null).generate();
			
	}
}
