package templates;

import java.util.List;
import java.util.Map;

//import messages.Messages;


import generator.utils.*;
import es.fap.simpleled.led.*;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.parseTreeConstruction.LedParsetreeConstructor.*;

public class GPopup extends GGroupElement{

	Popup popup;
	Formulario formulario;
	CampoUtils campo;
	String name;
	boolean maximizar;

	public GPopup(Popup popup, GElement container){
		super(popup, container);
		this.popup = popup;
		this.name = popup.name;
		this.maximizar = popup.maximizar;
		this.formulario = popup.eContainer();
		this.campo = CampoUtils.create(LedCampoUtils.getCampoPaginaPopup(popup));
		elementos = popup.getElementos();
	}

	public String view(){
		String elementos = "";
		for(Elemento elemento: popup.elementos)
			elementos += getInstance(elemento).view();

		TagParameters params = new TagParameters();
		Controller c = Controller.create(this);
		params.putStr("popup", popup.name);
		params.put("accion", "accion");
		params.put("urlRedirigir", "urlRedirigir");
		params.put("urlEditar", c.getRouteAccion("editar"));
		params.put("urlCrear", c.getRouteAccion("crear"));
		params.put("urlBorrar", c.getRouteAccion("borrar"));
		params.put("urlDuplicar", c.getRouteAccion("duplicar"));
		
		params.putStr("titulo", popup.titulo ?: popup.name);
		params.put("maximizar", popup.maximizar);


		if (popup.permiso != null) {
			params.putStr "permiso", "${popup.permiso.name}";
			if (popup.permiso.mensaje != null)
				params.putStr "permisoMensaje", popup.permiso.mensaje;
		}else if (formulario.permiso != null) {
			params.putStr "permiso", formulario.permiso.name;
			if (formulario.permiso.mensaje != null)
				params.putStr "permisoMensaje", formulario.permiso.mensaje;
		}

		String view = """
#{fap.popup ${params.lista(true)}
}
	${elementos}
#{/fap.popup}
		""";

		FileUtils.overwrite(FileUtils.getRoute('VIEW'), "popups/${viewName()}", view);
	}

	public String controllerName(){
		return "${popup.name}Controller";
	}

	public String controllerFullName(){
		return "popups.${controllerName()}";
	}

	public String controllerGenName(){
		return "${controllerName()}Gen";
	}

	public String controllerGenFullName(){
		return "popups.${controllerGenName()}";
	}

	public String viewName(){
		return "${popup.name}.html";
	}

	public String controller(){
		Controller.create(this).controller();
	}

	public String routes(){
		return Controller.create(this).generateRoutes();
	}

	public String url(){
		return "/${formulario.name}/${popup.name.toLowerCase()}";
	}
	
	public String saveCode(){
		String saveCode = super.saveCode();
		String saveSolicitud = "";
		
		if ((popup.copia) && (!Controller.create(this).getItvariableDb().contains("dbSolicitud"))){
			saveSolicitud="""dbSolicitud.save(); """;
		}

		if (popup.copia){
			saveCode += """
						   if (!peticionModificacion.isEmpty()){
							if ((!Messages.hasErrors())){
							   Gson gson = new Gson();
							   String jsonPM = gson.toJson(peticionModificacion);
							   JsonPeticionModificacion jsonPeticionModificacion = new JsonPeticionModificacion();
							   jsonPeticionModificacion.jsonPeticion = jsonPM;
							   dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size()-1).jsonPeticionesModificacion.add(jsonPeticionModificacion);
							}
							   dbSolicitud.save();
							}
						""";
						//${saveSolicitud}
		}
		return saveCode;
	}
}
