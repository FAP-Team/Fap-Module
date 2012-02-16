package templates;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject

import es.fap.simpleled.led.*
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;
import generator.utils.*
import generator.utils.HashStack.HashStackName;
import generator.utils.EntidadUtils;


public class GPagina {

	Controller controller;
	Pagina pagina;
	String formulario;
	boolean hasForm;
	CampoUtils campo;
	String name;
	
	public static void generate(Pagina pagina){
		GPagina g = new GPagina(pagina);
		HashStack.push(HashStackName.ROUTES, g);
		HashStack.push(HashStackName.CONTAINER, g);
		if (pagina.guardarParaPreparar) {
			HashStack.push(HashStackName.SAVE_CODE, g);
		}
		g.view();
		g.controller();
		HashStack.pop(HashStackName.CONTAINER);
	}

	public GPagina(Pagina pagina){
		this.pagina = pagina;
		this.name = pagina.name;
		this.formulario = pagina.eContainer().name;
		this.campo = CampoUtils.create(LedCampoUtils.getCampoPagina(pagina));
		this.hasForm = this.campo != null && !pagina.noForm && !hayForm(pagina);
	}
	
	public String controllerName(){
		return "${pagina.name}Controller";
	}

	public String controllerGenName(){
		return controllerName() + "Gen";
	}

	public String controllerFullName(){
		return controllerName();
	}

	public String controllerGenFullName(){
		return controllerGenName();
	}

	public String url(){
		return "/${formulario}/${pagina.name.toLowerCase()}";
	}

	public String view(){
		String viewElementos = "";
		for(Elemento elemento : pagina.getElementos()){
			viewElementos += Expand.expand(elemento);
		}
		
		crearControllerUtils();
		
		TagParameters params = new TagParameters();
		
		//Comprueba si hay elementos de subirArchivo en el formulario para añadir el encttype adecuado
		boolean hasSubirArchivo = HashStack.allElements(HashStackName.SUBIR_ARCHIVO)?.size() > 0;
		HashStack.remove(HashStackName.SUBIR_ARCHIVO);
		if(hasSubirArchivo != null && hasSubirArchivo)
			params.putStr "encType", "multipart/form-data";
		else
			params.putStr "encType", "application/x-www-form-urlencoded";

		if (pagina.permiso != null) {
			params.putStr "permiso", "${pagina.permiso.name}";
			if (pagina.permiso.mensaje != null)
				params.putStr "permisoMensaje", pagina.permiso.mensaje;
//				params.putStr "permisoMensaje", "No tiene suficientes privilegios para acceder a ésta página";
		}
		if (HashStack.top(HashStackName.PERMISSION) != null) {
			Permiso formPermiso = HashStack.top(HashStackName.PERMISSION);
			params.putStr "permiso", formPermiso.name;
			if (formPermiso.mensaje != null)
				params.putStr "permisoMensaje", formPermiso.mensaje;
		}
		
		params.put("accion", "accion");
		params.put("urlEditar", controller.getRouteAccion("editar"));
		params.put("urlCrear", controller.getRouteAccion("crear"));
		params.put("urlBorrar", controller.getRouteAccion("borrar"));
		params.putStr("titulo", pagina.titulo != null ? pagina.titulo : pagina.name);
		params.putStr("formulario", formulario);
		params.putStr("pagina", name);
		params.put("hayForm", hasForm);
		params.putStr("botonEditar", controller.accionEditar.boton);
		params.putStr("botonCrear", controller.accionCrear.boton);
		params.putStr("botonBorrar", controller.accionBorrar.boton);
		
		String view = """
#{fap.pagina ${params.lista(true)}
}
	${viewElementos}
#{/fap.pagina}
		"""
		
		FileUtils.overwrite(FileUtils.getRoute('VIEW'), "${pagina.name}/${pagina.name}.html", view);
	}

	public void crearControllerUtils(){
		List<String> saveController = HashStack.allElements(HashStackName.CONTROLLER);
		List<EntidadUtils> saveEntity = HashStack.allElements(HashStackName.SAVE_ENTITY).unique();
		List<EntidadUtils> indexEntity = HashStack.allElements(HashStackName.INDEX_ENTITY).unique();
		List<String> saveExtra = HashStack.allElements(HashStackName.SAVE_EXTRA).unique();
		List<String> saveCode = HashStack.allElements(HashStackName.SAVE_CODE);
		List<String> saveBoton = HashStack.allElements(HashStackName.SAVE_BOTON);
		List<String> firmaBoton = HashStack.allElements(HashStackName.FIRMA_BOTON);
		
		HashStack.remove(HashStackName.CONTROLLER);
		HashStack.remove(HashStackName.SAVE_ENTITY);
		HashStack.remove(HashStackName.SAVE_EXTRA);
		HashStack.remove(HashStackName.SAVE_CODE);
		HashStack.remove(HashStackName.SAVE_BOTON);
		HashStack.remove(HashStackName.FIRMA_BOTON);
		HashStack.remove(HashStackName.INDEX_ENTITY);
		
		controller = Controller.fromPagina(pagina);
		controller.saveController = saveController;
		controller.saveExtra = saveExtra;
		controller.saveCode = saveCode;
		controller.saveBoton = saveBoton;
		controller.firmaBoton = firmaBoton;
		controller.saveEntities = saveEntity;
		controller.indexEntities = indexEntity;
		controller.initialize();
	}

	public String controller(){
		controller.controller();
	}
	
	public String generateRoutes(){
		controller.generateRoutes();
	}
	
	public String getNameRoute() {
		return url();
	}

	public String saveCode(){
		return """
			if(!validation.hasErrors()){
				dbSolicitud.savePages.pagina${pagina.name} = true;
			}
		""";
	}
	
	public static boolean hayForm(EObject container){
		if (container instanceof Form)
			return true;
		EList<Elemento> elementos = LedCampoUtils.getElementos(container);
		if (elementos != null){
			for (EObject obj: elementos){
				if (hayForm(obj)){
					return true;
				}
			}
			return false;
		}
		return false;
	}

}
