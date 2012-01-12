package templates;

import java.util.List;

import org.eclipse.emf.ecore.EObject

import es.fap.simpleled.led.*
import generator.utils.*
import generator.utils.HashStack.HashStackName;
import generator.utils.EntidadUtils;


public class GPagina {

	def elementoGramatica;
	Pagina pagina;
	String formulario;
	List<EntidadUtils> entities;
	List<String> saveExtra;
	List<String> saveCode;
	
	public static String generate(Pagina pagina){
		GPagina g = new GPagina();
		g.pagina = pagina;
		g.formulario = ModelUtils.getActualContainer().name;
		g.elementoGramatica = pagina;
		HashStack.push(HashStackName.ROUTES, g);
		HashStack.push(HashStackName.GPAGINA, g);
		if (pagina.guardarParaPreparar) {
			HashStack.push(HashStackName.SAVE_CODE, g);
		}
		if (pagina.inicial) {
			HashStack.push(HashStackName.FIRST_PAGE, pagina.name);
		} else {
			HashStack.push(HashStackName.PAGE_NAME, pagina.name);
		}
		g.view();
		g.controller();
		HashStack.pop(HashStackName.GPAGINA);
	}

	public String controllerName(){
		return pagina.name + "Controller";
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
		return "/" + formulario + "/" + pagina.name.toLowerCase();
	}

	public String view(){
		
		String viewElementos;
		if (pagina.noForm) {
			String elementos = "";
			for(Elemento elemento : pagina.getElementos()){
				elementos += Expand.expand(elemento);
			}
			viewElementos = """
				${elementos}
			"""
		}
		else{
			viewElementos = GForm.generate(pagina);
		}

		TagParameters paramsform = new TagParameters();
		Permiso formPermiso = HashStack.top(HashStackName.PERMISSION);
		if (formPermiso != null) {
			paramsform.putStr "permiso", formPermiso.name;
			paramsform.putStr "mensaje", "No tiene suficientes privilegios para acceder a páginas de éste formulario";
		}

		TagParameters params = new TagParameters();

		if (pagina.permiso != null) {
			params.putStr "permiso", "${pagina.permiso.name}";
			params.putStr "mensaje", "No tiene suficientes privilegios para acceder a ésta página";
		}
		
		String titulo = pagina.isTitulo() ? pagina.namePagina : pagina.name;
		String view = """
			#{extends 'fap/template.html' /}
			#{set title:'${titulo}' /}

			#{fap.permiso ${paramsform.lista()}}

			${GMenu.getIncludeMenuInPage()}

			#{fap.messages}

			#{fap.permiso ${params.lista()}}

			${viewElementos}

			#{/fap.permiso}
			#{/fap.messages}
			#{/fap.permiso}
			#{if play.getVirtualFile("../../public/javascripts/$formulario/$titulo"+".js") != null}
            	#{script '../../public/javascripts/${formulario}/${titulo}.js' /}
			#{/if}
		"""

		FileUtils.overwrite(FileUtils.getRoute('VIEW'), "${pagina.name}/${pagina.name}.html", view);
	}

	public String controller(){
		
		String controladorPadre = "GenericController"
		String withControlador = ""
		if (pagina.noAutenticar) {
			controladorPadre = "Controller"
			withControlador = "@With({PropertiesFap.class, MessagesController.class, AgenteController.class})"
		}
		
		String controllerHS = "";
		for(elemento in HashStack.allElements(HashStackName.CONTROLLER)){
			controllerHS += elemento.controller()
		}
		HashStack.remove(HashStackName.CONTROLLER);
		entities = HashStack.allElements(HashStackName.SAVE_ENTITY);
		
		entities.addAll(HashStack.allElements(HashStackName.INDEX_ENTITY));
		entities = entities.unique();
		
		saveExtra = HashStack.allElements(HashStackName.SAVE_EXTRA);
		saveCode = HashStack.allElements(HashStackName.SAVE_CODE);
		
		HashStack.remove(HashStackName.SAVE_ENTITY);
		HashStack.remove(HashStackName.SAVE_EXTRA);
		HashStack.remove(HashStackName.SAVE_CODE);
		HashStack.remove(HashStackName.SAVE_BOTON);
		HashStack.remove(HashStackName.INDEX_ENTITY);

		List<String> renderParams = entities.collect { it.variable };
		
		String redirectMethod = '"${controllerName()}.index"';
		String template = """ "gen/${pagina.name}/${pagina.name}.html" """;

		EntidadUtils solicitud = EntidadUtils.create();
		if (ModelUtils.isSolicitudForm()) {
			solicitud = EntidadUtils.create(LedUtils.findSolicitud());
		}
		String controllerGen = """
			package controllers.gen;

			import play.*;
			import play.mvc.*;
			import controllers.fap.*;
			import tags.ReflectionUtils;
			import validation.*;
			import models.*;
			import java.util.*;
			import messages.Messages;
			import java.lang.reflect.Field;

			import security.Secure;
			import javax.inject.Inject;
			import services.*;

			${withControlador}
			public class ${controllerGenName()} extends ${controladorPadre} {

				@Inject
				protected static Secure secure;

				public static void index(${solicitud.typeId}){
					${entities.collect{"$it.typeVariable = ${ControllerUtils.simpleGetterCall(it, false)};"}.join("\n")}

					renderTemplate(${StringUtils.params(template, renderParams)});
				}
				
				@Before
				static void beforeMethod() {
					renderArgs.put("controllerName", "${controllerGenName()}");
				}
	
		""";
		
		for (EntidadUtils entidad: entities){
			controllerGen += ControllerUtils.simpleGetter(entidad, false);
		}
		
		controllerGen += """
				${controllerHS}
			}
		"""
		
		FileUtils.overwrite(FileUtils.getRoute('CONTROLLER_GEN'), controllerGenName() + ".java", controllerGen);
		
		String controller = """
			package controllers;

			import controllers.gen.${controllerGenName()};
			
			public class ${controllerName()} extends ${controllerGenName()} {

			}
		"""
		
		FileUtils.write(FileUtils.getRoute("CONTROLLER"), controllerName() + ".java", controller);
	}

	public String generateRoutes(){
		StringBuffer sb = new StringBuffer();
		StringUtils.appendln sb, Route.to("GET", url(), controllerName() + ".index")
		return sb.toString();
	}

	public String saveCode(){
		return """
			if(!validation.hasErrors()){
				dbSolicitud.savePages.pagina${pagina.name} = true;
			}
		""";
	}

}
