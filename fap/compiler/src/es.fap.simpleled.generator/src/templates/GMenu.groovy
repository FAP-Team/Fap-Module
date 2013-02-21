package templates

import es.fap.simpleled.led.*;
import generator.utils.CampoUtils
import generator.utils.Controller;
import generator.utils.Entidad;
import generator.utils.FileUtils;
import generator.utils.StringUtils
import es.fap.simpleled.led.util.LedCampoUtils
import es.fap.simpleled.led.util.LedEntidadUtils;

public class GMenu extends GElement {

	Menu menu;
	Set<String> scriptVariables;
	String scriptEntidadesDeclaracion = "";

	public GMenu(Menu menu, GElement container){
		super(menu, container);
		this.menu = menu;
		this.scriptVariables = new HashSet<String>();
	}

	public void generate(){
		scriptVariables = new HashSet<String>();
		String viewAntes = "<ul class='nav nav-list'>\n"
		String view = ""
		for(MenuElemento elemento : menu.elementos){
			view += generateElemento(elemento, -1);
		}
		view +="</ul>"
		view = viewAntes+"""
			%{
				${scriptEntidadesDeclaracion}
			%}
"""+ view;
		FileUtils.overwrite(FileUtils.getRoute('MENU_GEN'), getMenuName(), view);
	}
	
	/**
	* Devuelve el nombre del menu del formulario que se está procesando
	*/
   public String getMenuName(){
	   Formulario f = menu.eContainer();
	   if (f.menu == null) return null;
	   return f.name + ".html";
   }

	public String generateElemento(MenuGrupo grupo, int profundidad){
		profundidad++;
		String out = "";
		if (grupo.permiso != null) {
			out += """
				#{fap.permiso permiso:'${grupo.permiso.name}'}
			""";
		}
		String padding="";
		if (profundidad > 0){
			int pixeles = (profundidad*20)+15; // +15 para equilibrar con el margin-left negativo de los nav-header
			padding=""" style="padding-left: ${pixeles}px" """;
		}
		out += """
				<li class="nav-header" ${padding}>${grupo.titulo}</li>
		""";
		for(MenuElemento elemento : grupo.elementos){
			out += generateElemento(elemento, profundidad);
		}
		out += """
		<li class="nav-separator"></li>	
		""";
		if (grupo.permiso != null) {
			out += """
				#{/fap.permiso}
			""";
		}
		return out;
	}

	public String generateElemento(MenuEnlace enlace, int profundidad){
		String titulo = enlace.titulo != null ? enlace.titulo : enlace.pagina?.pagina.name
		String ref = "";
		String refSin = "";
		String permisoBefore = "";
		String permisoAfter = "";
		String padding="";
		if (profundidad > 0){
			int pixeles = profundidad*20;
			padding=""" style="padding-left: ${pixeles}px" """;
		}
		
		if (enlace.permiso != null) {
			permisoBefore = """
				#{fap.permiso permiso:'${enlace.permiso.name}'}
			""";
			permisoAfter = "#{/fap.permiso}";
		}
		if(enlace.pagina != null){
			return """
				${permisoBefore}
				${scriptUrl(Controller.create(GElement.getInstance(enlace.pagina.pagina, null)), enlace.pagina.accion)}
				<li class="#{fap.activeRoute href:url, activeClass:'active'/}" ${padding}><a href='\${url}'>${titulo}</a></li>
				${permisoAfter}
			""";
		}
		
		String script = "";
		String seleccion = "";
		if(enlace.accion != null){ //Accion
			String link = """play.mvc.Router.reverse("${enlace.accion}")"""
			String url = "url = ${link};";
			script = """
			%{
				${url}
			%}
			""";
			ref = "@{${enlace.accion}}"
			seleccion = """class="#{fap.activeRoute href:url, activeClass:'active' /}" """
	    } else if (enlace.accionLogout) {
			String link = """play.mvc.Router.reverse("fap.SecureController.logoutFap")"""
			String url = "url = ${link};";
			script = """
			%{
				${url}
			%}
			""";
			ref = "@{fap.SecureController.logoutFap}"
			seleccion = """class="#{fap.activeRoute href:url, activeClass:'active' /}" """
		} else if (enlace.accionLogin) {
			String link = """play.mvc.Router.reverse("fap.SecureController.loginFap")"""
			String url = "url = ${link};";
			script = """
			%{
				${url}
			%}
			""";
			ref = "@{fap.SecureController.loginFap}"
			seleccion = """class="#{fap.activeRoute href:url, activeClass:'active' /}" """
		} else if(enlace.url != null) //URL
			ref = enlace.url;
		else if(enlace.popup != null) { //Popup
			script = "${scriptUrl(Controller.create(GElement.getInstance(enlace.popup.popup, null)), enlace.popup.accion)}";
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
			<li ${seleccion} ${padding}><a href="${ref}">${titulo}</a></li>
			${permisoAfter}
		""";
	}
	
	private String scriptUrl(Controller controller, String accion){
		String link = controller.getRouteIndex(accion);
		List<Entidad> entidades = new ArrayList<Entidad>();
		String scriptEntidades = "";
		for (Entidad entidad: controller.allEntities){
			if (!scriptVariables.contains(entidad.variable)){
				scriptEntidadesDeclaracion += """ models.${entidad.clase} ${entidad.variable};\n """;
			}
			scriptVariables.add(entidad.variable);
			
			scriptEntidades += """ if (${entidad.variable} == null)
											${entidad.variable} = play.mvc.Scope.RenderArgs.current().get("${entidad.variable}");\n""";
			
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
