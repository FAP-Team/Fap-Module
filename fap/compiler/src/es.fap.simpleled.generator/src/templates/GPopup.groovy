package templates;

import java.util.List;
import java.util.Map;

import generator.utils.*;
import generator.utils.HashStack.HashStackName;
import es.fap.simpleled.led.*;
import generator.utils.EntidadUtils;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.parseTreeConstruction.LedParsetreeConstructor.ThisRootNode;

public class GPopup {

	Controller controller;
	Popup popup;
	String formulario;
	CampoUtils campo;
	
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
		String elementos = "";
		for(Elemento elemento : popup.getElementos()){
			elementos += Expand.expand(elemento);
		}
		
		crearControllerUtils();
				
        TagParameters params = new TagParameters();

		if (popup.permiso != null)
			params.putStr('permiso', popup.permiso.name);

		params.putStr("popup", popup.name);
		params.put("accion", "accion");
		params.put("urlEditar", controller.getRouteAccion("editar"));
		params.put("urlCrear", controller.getRouteAccion("crear"));
		params.put("urlBorrar", controller.getRouteAccion("borrar"));
		params.putStr("titulo", popup.titulo ?: popup.name);
			
		String view = """
#{fap.popup ${params.lista(true)}
}
	${elementos}
#{/fap.popup}
		"""
					
		FileUtils.overwrite(FileUtils.getRoute('VIEW'), "popups/${viewName()}", view);
	}
	
	public void crearControllerUtils(){
		List<String> saveController = HashStack.allElements(HashStackName.CONTROLLER);
		List<EntidadUtils> saveEntity = HashStack.allElements(HashStackName.SAVE_ENTITY).unique();
		List<EntidadUtils> indexEntity = HashStack.allElements(HashStackName.INDEX_ENTITY).unique();
		List<String> saveExtra = HashStack.allElements(HashStackName.SAVE_EXTRA).unique();
		List<String> saveCode = HashStack.allElements(HashStackName.SAVE_CODE);
		List<String> saveBoton = HashStack.allElements(HashStackName.SAVE_BOTON);
		
		HashStack.remove(HashStackName.CONTROLLER);
		HashStack.remove(HashStackName.SAVE_ENTITY);
		HashStack.remove(HashStackName.SAVE_EXTRA);
		HashStack.remove(HashStackName.SAVE_CODE);
		HashStack.remove(HashStackName.SAVE_BOTON);
		HashStack.remove(HashStackName.INDEX_ENTITY);
		
		controller = Controller.fromPopup(popup);
		controller.saveController = saveController;
		controller.saveExtra = saveExtra;
		controller.saveCode = saveCode;
		controller.saveBoton = saveBoton;
		controller.saveEntities = saveEntity;
		controller.indexEntities = indexEntity;
		controller.initialize();
	}
	
	public String controller(){
		controller.controller();
	}
	
	public String generateRoutes(){
		return controller.generateRoutes();
	}
	
}
