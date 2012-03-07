package templates;

import java.util.List;
import java.util.Map;

import generator.utils.*;
import generator.utils.HashStack.HashStackName;
import es.fap.simpleled.led.*;
import generator.utils.EntidadUtils;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.parseTreeConstruction.LedParsetreeConstructor.*;

public class GPopup {

	Controller controller;
	Popup popup;
	String formulario;
	CampoUtils campo;
	String name;
	
	public static void generate(Popup popup){
		GPopup g = new GPopup(popup);
		HashStack.push(HashStackName.ROUTES, g)
		HashStack.push(HashStackName.CONTAINER, g)
		g.view();
		g.controller();
		HashStack.pop(HashStackName.CONTAINER);
	}
	
	public GPopup(Popup popup){
		this.popup = popup;
		this.name = popup.name;
		this.formulario = popup.eContainer().name;
		this.campo = CampoUtils.create(popup.campo);
	}
	
	public String url(){
		return "/${formulario}/${popup.name.toLowerCase()}";
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
	
	public String view(){
		
		String elementos = crearControllerUtils();
				
        TagParameters params = new TagParameters();

		params.putStr("popup", popup.name);
		params.put("accion", "accion");
		params.put("urlEditar", controller.getRouteAccion("editar"));
		params.put("urlCrear", controller.getRouteAccion("crear"));
		params.put("urlBorrar", controller.getRouteAccion("borrar"));
		params.putStr("titulo", popup.titulo ?: popup.name);
		
		if (popup.permiso != null) {
			params.putStr "permiso", "${popup.permiso.name}";
			if (popup.permiso.mensaje != null)
				params.putStr "permisoMensaje", popup.permiso.mensaje;
		}
		if (HashStack.top(HashStackName.PERMISSION) != null) {
			Permiso formPermiso = HashStack.top(HashStackName.PERMISSION);
			params.putStr "permiso", formPermiso.name;
			if (formPermiso.mensaje != null)
				params.putStr "permisoMensaje", formPermiso.mensaje;
		}
		
		String view = """
#{fap.popup ${params.lista(true)}
}
	${elementos}
#{/fap.popup}
		"""
					
		FileUtils.overwrite(FileUtils.getRoute('VIEW'), "popups/${viewName()}", view);
	}
	
	public String crearControllerUtils(){
		int sizeExtra = HashStack.size(HashStackName.SAVE_EXTRA);
		int sizeEntity = HashStack.size(HashStackName.SAVE_ENTITY);
		int sizeCode = HashStack.size(HashStackName.SAVE_CODE);
		int sizeBoton = HashStack.size(HashStackName.SAVE_BOTON);
		int sizeFirma = HashStack.size(HashStackName.FIRMA_BOTON);
		int sizeController = HashStack.size(HashStackName.CONTROLLER);
		
		String elementos = "";
		for(Elemento elemento: popup.elementos){
			elementos += Expand.expand(elemento);
		}

		List<EntidadUtils> saveEntity = HashStack.popUntil(HashStackName.SAVE_ENTITY, sizeEntity).unique();
		List<String> saveExtra = HashStack.popUntil(HashStackName.SAVE_EXTRA, sizeExtra).unique();
		List<String> saveCode = HashStack.popUntil(HashStackName.SAVE_CODE, sizeCode);
		List<String> saveBoton = HashStack.popUntil(HashStackName.SAVE_BOTON, sizeBoton);
		List<String> firmaBoton = HashStack.popUntil(HashStackName.FIRMA_BOTON, sizeFirma);
		List<String> saveController = HashStack.popUntil(HashStackName.CONTROLLER, sizeController);
		
		controller = Controller.fromPopup(popup);
		controller.saveController = saveController;
		controller.saveExtra = saveExtra;
		controller.saveCode = saveCode;
		controller.saveBoton = saveBoton;
		controller.firmaBoton = firmaBoton;
		controller.saveEntities = saveEntity;
		controller.initialize();
		return elementos;
	}
	
	public String controller(){
		controller.controller();
	}
	
	public String generateRoutes(){
		return controller.generateRoutes();
	}
	
	public String getNameRoute() {
		return url();
	}
}
