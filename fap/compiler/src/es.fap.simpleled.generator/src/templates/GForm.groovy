package templates;

import java.util.List;
import org.eclipse.emf.ecore.EObject

import com.sun.media.sound.RealTimeSequencer.PlayThread;

import generator.utils.*;
import generator.utils.HashStack.HashStackName;
import es.fap.simpleled.led.*;
import es.fap.simpleled.led.util.LedEntidadUtils;


public class GForm {

	Controller controller;
	Form form;
	String name;
	
	public static String generate(Form form){
		GForm g = new GForm(form);
		String view = g.view();
		HashStack.push(HashStackName.CONTROLLER, g);
		HashStack.push(HashStackName.ROUTES, g);
		return view;
	}
	
	public GForm (Form form){
		this.form = form;
		this.name = StringUtils.firstLower(form.name);
	}
	
	public String view(){
		
		String elementos = crearControllerUtils();
		
		//Comprueba si hay elementos de subirArchivo en el formulario para añadir el encttype adecuado
		boolean hasSubirArchivo = HashStack.allElements(HashStackName.SUBIR_ARCHIVO)?.size() > 0;
		HashStack.remove(HashStackName.SUBIR_ARCHIVO);
		
		String encTypeStr = "";
		if(hasSubirArchivo != null && hasSubirArchivo){
			encTypeStr = ", enctype:\"multipart/form-data\"";
		}
		
		String view = """
			#{form ${controller.getRouteAccion(name)} ${encTypeStr}}
				${elementos}
			#{/form}
		""";
		
		if(form.autoEnviar){
			view += """
				<script>
					\$(function(){
						\$('#${name} input, #${name} select').change(function(){
							\$('#${name}').submit();
						});
					});
				</script>
			""";
		}
		
		if (form.permiso != null) {
			view = """
				#{fap.permiso permiso:"${form.permiso.name}"}
					$view
				#{/fap.permiso}		
			""";
		}
		return view;
	}
	
	public String crearControllerUtils(){
		int sizeExtra = HashStack.size(HashStackName.SAVE_EXTRA);
		int sizeEntity = HashStack.size(HashStackName.SAVE_ENTITY);
		int sizeCode = HashStack.size(HashStackName.SAVE_CODE);
		int sizeBoton = HashStack.size(HashStackName.SAVE_BOTON);
		int sizeIndex = HashStack.size(HashStackName.INDEX_ENTITY);
		int sizeController = HashStack.size(HashStackName.CONTROLLER);
		
		String elementos = "";
		for(Elemento elemento: form.elementos){
			elementos += Expand.expand(elemento);
		}

		List<EntidadUtils> saveEntity = HashStack.popUntil(HashStackName.SAVE_ENTITY, sizeEntity).unique();
		List<String> saveExtra = HashStack.popUntil(HashStackName.SAVE_EXTRA, sizeExtra).unique();
		List<String> saveCode = HashStack.popUntil(HashStackName.SAVE_CODE, sizeCode);
		List<String> saveBoton = HashStack.popUntil(HashStackName.SAVE_BOTON, sizeBoton);
		List<EntidadUtils> indexEntity = HashStack.popUntil(HashStackName.INDEX_ENTITY, sizeIndex).unique();
		List<String> saveController = HashStack.popUntil(HashStackName.CONTROLLER, sizeController);
		
		controller = Controller.fromForm(form);
		controller.saveController = saveController;
		controller.saveExtra = saveExtra;
		controller.saveCode = saveCode;
		controller.saveBoton = saveBoton;
		controller.saveEntities = saveEntity;
		controller.indexEntities = indexEntity;
		controller.initialize();
		return elementos;
	}
	
	public String controller(){
		return """
			${controller.metodoEditar()}
			${controller.metodoEditarRender()}
			${controller.validateCopyMethod()}
			${controller.metodoEditarValidateRules()}
			${controller.metodoPermiso()}
			${controller.botonesMethods()}
		"""; 
	}

	
	public String generateRoutes(){
		return controller.generateRoutes();
	}
	
}
