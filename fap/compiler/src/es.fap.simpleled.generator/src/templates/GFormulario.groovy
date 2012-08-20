package templates

import es.fap.simpleled.led.*;
import generator.utils.FileUtils;
import wfcomponent.Start;


class GFormulario extends GElement{

	Formulario formulario;
	String configMsj = "";
	String lista = "";
	
	public GFormulario(Formulario formulario, GElement container){
		super(formulario, container);
		this.formulario = formulario;
	}
		
	public void generate(){
		for(int i = 0; i < formulario.getPaginas().size(); i++){
			GElement.getInstance(formulario.getPaginas().get(i), null).generate();
			String tituloPaginaLista = formulario.name+"-"+formulario.getPaginas().get(i).name;
			String tituloPagina  = formulario.getPaginas().get(i).name;
			
			if (formulario.getPaginas().get(i).titulo !=null){
				tituloPaginaLista+="-"+formulario.getPaginas().get(i).titulo;
			}
			if ((formulario.name == "Solicitud")){
				lista+="""${tituloPaginaLista},
"""
				configMsj+="""ConfigurarMensaje(ConfigurarMensaje-${tituloPagina}):
   nombrePagina: ${tituloPagina}
   tipoMensaje: []
   habilitar: false

"""
			}
		}
		if((formulario.name == "Solicitud") && (Start.generatingModule)){
			FileUtils.append(FileUtils.getRoute('INI_DATA'), "paginasMsj.yml", configMsj);
			FileUtils.append(FileUtils.getRoute('INI_DATA'), "paginasMsjLista.yml", lista);
			
		}
		
		if((formulario.name == "Solicitud") && (!Start.generatingModule)){
			FileUtils.append(FileUtils.getRoute('INI_DATA'), "paginasAppMsj.yml", configMsj);
			FileUtils.append(FileUtils.getRoute('INI_DATA'), "paginasAppMsjLista.yml", lista);
		}
		
		for(int i = 0; i < formulario.getPopups().size(); i++)
			GElement.getInstance(formulario.getPopups().get(i), null).generate();

	}
	
}
