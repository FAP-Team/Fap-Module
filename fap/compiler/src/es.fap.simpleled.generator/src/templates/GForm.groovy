package templates;

import java.util.List;

import com.sun.media.sound.RealTimeSequencer.PlayThread;

import generator.utils.*;
import generator.utils.HashStack.HashStackName;
import es.fap.simpleled.led.*;


public class GForm {

	def elementoGramatica;
	def contenedor;
	String name;
	CampoUtils campo;
	Permiso permiso;
	
	EntidadUtils padre;
	List<EntidadUtils> entities;
	List<String> saveExtra;
	List<String> saveCode;
	
	public static String generate(Form form){
		GForm g = new GForm();
		g.elementoGramatica = form;
		g.name = form.name;	
		if (form.campo != null){
			g.campo = CampoUtils.create(form.campo);
		}
		g.permiso = form.permiso
		g.contenedor = HashStack.top(HashStackName.GPAGINA);
		String view = g.view();
		HashStack.push(HashStackName.CONTROLLER, g);
		HashStack.push(HashStackName.ROUTES, g);
		return view;
	}
	
	public static String generate(Pagina pagina){
		GForm g = new GForm();
		g.elementoGramatica = pagina;
		g.name = "save";	
		g.campo = null;
		g.permiso = pagina.permiso;
		g.contenedor = HashStack.top(HashStackName.GPAGINA);
		String view = g.view();
		HashStack.push(HashStackName.CONTROLLER, g);
		HashStack.push(HashStackName.ROUTES, g);
		return view;
	}
	
	private controllerName(){
		return 	contenedor.controllerFullName();
	}
	
	private controllerMethodName(){
		return name;
	}

			
	public String view(){
		
		int sizeExtra = HashStack.size(HashStackName.SAVE_EXTRA);
		int sizeEntity = HashStack.size(HashStackName.SAVE_ENTITY);
		int sizeCode = HashStack.size(HashStackName.SAVE_CODE);
		
		String elementos = "";
		for(Elemento elemento: elementoGramatica.elementos){
			elementos += Expand.expand(elemento);
		}

		padre = EntidadUtils.create(campo);
		if (campo == null && ModelUtils.isSolicitudForm()){
			padre = EntidadUtils.create(LedUtils.findSolicitud());
		}
		padre.addToIndexEntity();
		
		if (elementoGramatica instanceof Form){
			entities = HashStack.getUntil(HashStackName.SAVE_ENTITY, sizeEntity).unique();
			saveExtra = HashStack.getUntil(HashStackName.SAVE_EXTRA, sizeExtra);
			saveCode = HashStack.getUntil(HashStackName.SAVE_CODE, sizeCode);
		}
		else{
			entities = HashStack.allElements(HashStackName.SAVE_ENTITY).unique();
			saveExtra = HashStack.allElements(HashStackName.SAVE_EXTRA);
			saveCode = HashStack.allElements(HashStackName.SAVE_CODE);
		}
		
		//Comprueba si hay elementos de subirArchivo en el formulario para añadir el encttype adecuado
		boolean hasSubirArchivo = HashStack.allElements(HashStackName.SUBIR_ARCHIVO)?.size() > 0;
		HashStack.remove(HashStackName.SUBIR_ARCHIVO);
		
		String encTypeStr = "";
		if(hasSubirArchivo != null && hasSubirArchivo){
			encTypeStr = ", enctype:\"multipart/form-data\"";
		}
		String saveButtonStr = "";
		
		// SI GENERA BOTONES QUE NO SON, HAY QUE CAMBIAR ESTA CONDICION:
		if ((entities.size() + saveExtra.size() > 0) && (elementoGramatica instanceof Pagina)) {
			saveButtonStr = """
				<div class="button_container">
					#{fap.boton titulo:"Guardar" /}
				</div>
			""";
		}
		String view = """
			#{form @${controllerName()}.${controllerMethodName()}(${padre.getId()}), id:"${name}" ${encTypeStr}}
				${elementos}
				${saveButtonStr}
			#{/form}
		""";
		
		if(elementoGramatica instanceof Form  && elementoGramatica.autoEnviar){
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
		
		if (permiso != null) {
		view = """
			#{fap.permiso permiso:"${permiso.name}"}
			
			$view
			
			#{/fap.permiso}		
"""
		}

		return view;
	}
	
	public String controller(){
		
		List<String> saveParams = entities.collect { it.getTypeVariable() };
		List<String> redirect = entities.collect { it.getVariable() };
		
		String redirectMethod = """ "${controllerName()}.index" """;
		String redirectMethodOk = null;
		
		if(elementoGramatica instanceof Form && elementoGramatica.redirigir != null){
			redirectMethodOk = """ "${elementoGramatica.redirigir.name}Controller.index" """
		}else{
			redirectMethodOk = redirectMethod
		}

		
		String controllerCode = """
			@Util
			protected static boolean permiso${name}(String accion) {
				${ControllerUtils.permisoContent(permiso)}
			}
		"""
		
		controllerCode += """
			${ControllerUtils.validateCopyMethod(this, (EntidadUtils[])entities.toArray())}
			
			public static void ${controllerMethodName()}(${StringUtils.params(padre.typeId, saveParams, saveExtra)}){
				checkAuthenticity();
				if (permiso${name}("update") || permiso${name}("create")) {
				
					${entities.collect{ "${it.typeDb} = ${ControllerUtils.simpleGetterCall(it, false)};"}.join("\n")}
				
					${ControllerUtils.validateCopyCall(this, (EntidadUtils[])entities.toArray())}

					if(!validation.hasErrors()){
						${ControllerUtils.validateRulesCall(this, (EntidadUtils[])entities.toArray())}
					}
					
					if(!validation.hasErrors()){
						${entities.collect{ "${it.variableDb}.save(); Logger.info(\"Guardando ${it.variable} \" + ${it.variableDb}.id);"}.join("\n")}
				
		"""
		if(campo != null && entities.size() > 0){
			controllerCode += """
						${padre.typeDb} = ${ControllerUtils.simpleGetterCall(padre, false)};
						${padre.variableDb}.${campo.sinEntidad()}.add(${entities.get(0).variableDb});
						${padre.variableDb}.save();
			"""
		}
		controllerCode += """
					}
				}
				else {
					Messages.fatal("No tiene permisos suficientes para realizar esta acción");
					/* no se hace aqui Messages.keep(); */
				}
				
				${controllerMethodName()}Render(${padre.id});

			}
			
			${ControllerUtils.validateRulesMethod(this, (EntidadUtils[])entities.toArray())}

			
		"""
						
		boolean isAutoenviar = elementoGramatica instanceof Form  && elementoGramatica.autoEnviar;
		String msgOk = "";
		if(!isAutoenviar){
			msgOk = """
				if (!Messages.hasMessages()) {
					Messages.ok("Página guardada correctamente");
				}		
			"""
		}
		
		String redirectCode = "";
		if(redirectMethod.equals(redirectMethodOk)){
			redirectCode = """redirect(${StringUtils.params(redirectMethod, padre.id)});"""
		}else{
			redirectCode = """
				if(Messages.hasErrors()){
					redirect(${StringUtils.params(redirectMethod, padre.id)});
				}else{
					redirect(${StringUtils.params(redirectMethodOk, padre.id)});
				}			
			"""
		}
		
		controllerCode += """
			@Util
			public static void ${controllerMethodName()}Render(${padre.typeId}){
				${msgOk}
				Messages.keep();
				${redirectCode}
			}
		"""
		
		return controllerCode; 
	}

	
	public String generateRoutes(){
		return Route.to("POST", contenedor.url() + "/" + name, contenedor.controllerFullName() + "." + controllerMethodName())
	}
	
}
