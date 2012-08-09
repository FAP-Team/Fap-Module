package templates

import es.fap.simpleled.led.*;
import generator.utils.FileUtils;


class GFormulario extends GElement{

	Formulario formulario;
	String texto = "";
	
	public GFormulario(Formulario formulario, GElement container){
		super(formulario, container);
		this.formulario = formulario;
	}
		
	public void generate(){		
		for(int i = 0; i < formulario.getPaginas().size(); i++){
			GElement.getInstance(formulario.getPaginas().get(i), null).generate();
			String tituloPagina = formulario.getPaginas().get(i).titulo ?: formulario.getPaginas().get(i).name;
			if (formulario.name == "Solicitud"){	
				texto+="""ConfigurarMensaje(ConfigurarMensaje-Solicitud):
  PaginaAconfigurar: "${tituloPagina}"
  tipoMensaje:
  tituloMensaje:
  contenido:
  habilitar: false

"""
			}
		}
		if(formulario.name == "Solicitud"){
			FileUtils.append(FileUtils.getRoute('INI_DATA'), "paginas.yml", texto);
		}
		for(int i = 0; i < formulario.getPopups().size(); i++)
			GElement.getInstance(formulario.getPopups().get(i), null).generate();
			
	}
	
}
