package templates

import es.fap.simpleled.led.*;
import generator.utils.FileUtils;
import generator.utils.HashStack;
import generator.utils.HashStack.HashStackName;
import generator.utils.ModelUtils;

public class GMenu {

	def Menu menu;

	public static String generate(Menu menu){
		GMenu g = new GMenu();
		g.menu = menu;
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
		"""
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
"""
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
"""

	if (grupo.permiso != null) {
		out += """
		#{/fap.permiso}
"""
		}

		return out;
	}

	public String generateElemento(MenuEnlace enlace){
		String titulo = enlace.titulo != null ? enlace.titulo : enlace.pagina 
		String ref = "";
		String refSin = "";
		String previousOut = "";
		String nextOut = "";
		
		if (enlace.permiso != null) {
			previousOut = """
			#{fap.permiso permiso:'${enlace.permiso.name}'}
"""
			nextOut = """#{/fap.permiso}"""
		}
		
		if(enlace.pagina != null) {//Página
			String entidad = enlace.pagina.eContainer().name;
			String link = enlace.pagina.name;
			
			// Si conocemos la entidad, la colocamos en el enlace (solo formularios "coj****"
			if ((entidad != null) && (entidad.equals("Solicitud"))) {
				ref = "@{${link}Controller.index(id${entidad})}"
				refSin = "@${link}Controller.index(id${entidad})" // Para link activo
			} else {
				ref = "@{${link}Controller.index()}"
				refSin = "@${link}Controller.index()" // Para link activo
			}
			// Posible link del menu a ctivo
			return """${previousOut}  <li><a class="#{fap.activeRoute href:${refSin}, activeClass:'menu-activo' /}" href=${ref}>${titulo}</a></li>  ${nextOut}
""";
		}
		
		
		if(enlace.accion != null) //Accion
			ref = "@{${enlace.accion}}"
		else if(enlace.url != null) //URL
			ref = enlace.url
		else if(enlace.popup != null){ //Popup
			GPopup gpopup = GPopup.generateParaEnlace(enlace.popup);
			ref= "javascript:popup_open('${gpopup.popupName}', '@{popups.${gpopup.popupName}Controller.abrir}', {accion:'crear'})";
		}
		else //Enlace por defecto, para prototipado principalmente
			ref = "#"
		
		// Sin el link del menu activo
		return """  ${previousOut}       <li><a href="${ref}">${titulo}</a></li> ${nextOut}
""";
	}
}
