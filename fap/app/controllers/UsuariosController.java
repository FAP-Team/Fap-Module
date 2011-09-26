
package controllers;

import java.util.List;

import play.mvc.Util;
import controllers.gen.UsuariosControllerGen;
import models.*;

public class UsuariosController extends UsuariosControllerGen {

	public static void index() {
		List<TableKeyValue> entries = TableKeyValue.findAll();
		renderTemplate("gen/Usuarios/Usuarios.html", entries);
	}
	
	
}
