package templates

import es.fap.simpleled.led.*;
import generator.utils.CampoUtils
import generator.utils.Controller
import generator.utils.EntidadUtils
import generator.utils.FileUtils;
import generator.utils.HashStack;
import generator.utils.StringUtils
import generator.utils.HashStack.HashStackName;
import es.fap.simpleled.led.util.LedCampoUtils
import es.fap.simpleled.led.util.LedEntidadUtils;

public class GMenu {

	def Menu menu;
	Set<String> scriptVariables;

	public static String generate(Menu menu){
		GMenu g = new GMenu();
		g.menu = menu;
		g.scriptVariables = new HashSet<String>();
		return g.generateView();
	}

	/**
	 * Devuelve el nombre del menu del formulario que se está procesando
	 */
	public static String getMenuName(){
		Formulario f = HashStack.top (HashStackName.FORMULARIO)
		if(f.menu == null) return null;
		return f.name + ".html";
	}

	/**
	 * Codigo para incluir el menu dentro de las páginas
	 * Si el formulario no tiene menú devuelve cadena vacia
	 */
	public static String getIncludeMenuInPage(){
		String menuName = getMenuName();
		
		if (menuName == null) {
			Formulario f = HashStack.top (HashStackName.FORMULARIO);
			menuName = f.name + ".html";
		}
		return """
			#{if play.getVirtualFile("app/views/gen/menu/$menuName") != null}
				#{set 'menu'}
						#{include 'gen/menu/${menuName}'/}
				#{/set}
			#{/if}
		""";
	}
	
	public String generateView(){
		String view = "<ul>"
		for(MenuElemento elemento : menu.elementos){
			view += generateElemento(elemento);
		}
		view +="</ul>"
		FileUtils.overwrite(FileUtils.getRoute('MENU_GEN'), getMenuName(), view);
		return view;
	}



	public String generateElemento(MenuGrupo grupo){
		String out = "";
		if (grupo.permiso != null) {
			out += """
				#{fap.permiso permiso:'${grupo.permiso.name}'}
			""";
		}
		out += """
				<li class="menu-group"><span class="menu-header">${grupo.titulo}</span>
			<ul>
		""";
		for(MenuElemento elemento : grupo.elementos){
			out += generateElemento(elemento);
		}
		out += """
				</ul>
			</li>	
		""";
		if (grupo.permiso != null) {
			out += """
				#{/fap.permiso}
			""";
		}
		return out;
	}

	public String generateElemento(MenuEnlace enlace){
		String titulo = enlace.titulo != null ? enlace.titulo : enlace.pagina?.pagina.name
		String ref = "";
		String refSin = "";
		String permisoBefore = "";
		String permisoAfter = "";
		
		if (enlace.permiso != null) {
			permisoBefore = """
				#{fap.permiso permiso:'${enlace.permiso.name}'}
			""";
			permisoAfter = "#{/fap.permiso}";
		}
		
		if(enlace.pagina != null){
			return """
				${permisoBefore}
				${scriptUrl(Controller.fromPagina(enlace.pagina.pagina).initialize(), enlace.pagina.accion)}
				<li><a class="#{fap.activeRoute href:url, activeClass:'menu-activo' /}" href='\${url}'>${titulo}</a></li>
				${permisoAfter}
			""";
		}
		
		String script = "";
		if(enlace.accion != null) //Accion
			ref = "@{${enlace.accion}}"
		else if(enlace.url != null) //URL
			ref = enlace.url;
		else if(enlace.popup != null){ //Popup
			script = "${scriptUrl(Controller.fromPopup(enlace.popup.popup).initialize(), enlace.popup.accion)}";
			ref= "javascript:popup_open('${enlace.popup.popup.name}', '\${url}')";
		}
		else if(enlace.anterior != null){
			script = "${scriptAnterior()}";
			ref= "\${urlAnterior}";
		}
		else //Enlace por defecto, para prototipado principalmente
			ref = "#"
		
		// Sin el link del menu activo
		return """
			${permisoBefore}
			${script}
			<li><a href="${ref}">${titulo}</a></li>
			${permisoAfter}
		""";
	}
	
	private String scriptUrl(Controller controller, String accion){
		String link = controller.getRouteIndex(accion);
		List<EntidadUtils> entidades = new ArrayList<EntidadUtils>();
		if (!controller.entidad.nulo())
			entidades.add(controller.entidad);
		if (!controller.almacen.nulo())
			entidades.add(controller.almacen);
		entidades.addAll(controller.intermedias);
		String scriptEntidades = "";
		for (EntidadUtils entidad: entidades){
			if (!scriptVariables.contains(entidad.variable)){
				scriptVariables.add(entidad.variable);
				scriptEntidades += """models.${entidad.clase} ${entidad.variable} = play.mvc.Scope.RenderArgs.current().get("${entidad.variable}");\n""";
			}
		}
		String url = "url = ${link};";
		if (!scriptVariables.contains("url")){
			url = "play.mvc.Router.ActionDefinition url = ${link};";
			scriptVariables.add("url");
		}
		return """
			%{
				${scriptEntidades}
				${url}
			%}
		""";
	}
	
	private String scriptAnterior(){
		String url = "";
		String key = "key";
		if (!scriptVariables.contains("urlAnterior")){
			url = "String urlAnterior;";
			key = "String key";
			scriptVariables.add("urlAnterior");
		}
		return """
			%{
				${url}
				${key} = "redirigir\${play.mvc.Scope.RenderArgs.current().get("container")}";
				if (play.mvc.Controller.response.cookies.containsKey(key))
					urlAnterior = play.mvc.Controller.response.cookies.get(key).value;
				else if (play.mvc.Controller.request.cookies.containsKey(key))
					urlAnterior = play.mvc.Controller.request.cookies.get(key).value;
			%}
		""";
	}
	
}
