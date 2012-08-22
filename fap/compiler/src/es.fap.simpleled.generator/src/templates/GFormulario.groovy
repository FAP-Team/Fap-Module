package templates

import es.fap.simpleled.led.*;
import generator.utils.FileUtils;
import wfcomponent.Start;


class GFormulario extends GElement{

	Formulario formulario;
	String configMsj = "";
	
	public GFormulario(Formulario formulario, GElement container){
		super(formulario, container);
		this.formulario = formulario;
	}
		
	public void generate(){
		for(int i = 0; i < formulario.getPaginas().size(); i++){
			GElement.getInstance(formulario.getPaginas().get(i), null).generate();
			String tituloPagina  = formulario.getPaginas().get(i).name;
				configMsj+="""ConfigurarMensaje(ConfigurarMensaje-${tituloPagina}):
   nombrePagina: ${tituloPagina}
   tipoMensaje: []
   habilitar: false
   formulario: ${formulario.name}

"""
		}
		if(Start.generatingModule){
			FileUtils.append(FileUtils.getRoute('INI_DATA'), "paginasMsj.yml", configMsj);			
		}
		else{
			FileUtils.append(FileUtils.getRoute('INI_DATA'), "paginasAppMsj.yml", configMsj);
		}
		
		for(int i = 0; i < formulario.getPopups().size(); i++)
			GElement.getInstance(formulario.getPopups().get(i), null).generate();

	}
	
}
