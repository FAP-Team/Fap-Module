
package controllers.fap;

import play.*;
import play.db.DB;
import play.db.jpa.JPA;
import play.mvc.*;
import controllers.fap.*;
import tags.ReflectionUtils;
import validation.*;
import models.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import messages.Messages;
import java.lang.reflect.Field;

import javax.persistence.Query;

import com.google.gson.Gson;


public class ConsultasController extends GenericController {

	public static void index(){
		renderTemplate( "fap/Admin/consultas.html" );
	}

	@Before
	static void beforeMethod() {
		renderArgs.put("controllerName", "ConsultasControllerGen");
	}

	public static void tablatablaConsultas(Long idConsulta){
		java.util.List<Consulta> rows = Consulta.find( "select consulta from Consulta consulta" ).fetch();

		List<Consulta> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Consulta> response = new tables.TableRenderResponse<Consulta>(rowsFiltered);
		renderJSON(response.toJSON("consulta", "descripcion", "tipo", "id"));
	}

	@Util
	protected static boolean permisoejecutarConsulta(String accion) {
		//Sobreescribir para incorporar permisos a mano
		return true;
	}

	public static void ejecutarConsulta(Long id){
		Consulta consulta = Consulta.findById(id);

		if (consulta != null) {
			if (consulta.tipo.equals("tipoSQL")) {
				if (consulta.consulta.toUpperCase().startsWith("SELECT")) {
					ResultSet list = DB.executeQuery(consulta.consulta);
					renderList(list);
				}
				else {
					boolean update = DB.execute(consulta.consulta);
					renderUpdate(update);
				}				
			}
			else if (consulta.tipo.equals("tipoJPQL")) {
				Query query = JPA.em().createQuery(consulta.consulta);
				if (consulta.consulta.toUpperCase().startsWith("SELECT")) {
					List list = query.getResultList();
					renderList(list);
				}
				else {
					int update = query.executeUpdate();
					renderUpdate(update);
				}								
			}
			else 
				renderText("Tipo de consulta no valido ("+consulta.tipo+")");				
		}

		Messages.error("Tipo de consulta no valido ("+consulta.tipo+")");				

		renderText("Consulta con ID" +id + " no encontrada");
	}

	private static void renderUpdate(int update) {
		renderHtml("Actualizadas "+update+ "filas");		
	}

	private static void renderList(List list) {
		String tabla = "<table class=\"consulta\">";

		for (Object o : list) {
			tabla += "<tr>";
			if(o instanceof Object[]) {
				for (Object o2 : (Object[])o) {
					tabla += "<td>" + o2.toString() + "</td>";
				}
			}
			else {
				tabla += "<td>" + o.toString() + "</td>";
			}
			tabla += "</tr>";
		}
		tabla += "</table>";

		renderHtml(tabla);
	}

	private static void renderUpdate(boolean update) {
		if (update) 
			renderHtml("La modificación se realizo correctamente");
		else
			renderHtml("Se produjo un erro al realizar la modificación");
		
	}

	private static void renderList(ResultSet list) {
		String tabla = "<table class=\"consulta\">";

		try {
			ResultSetMetaData rsmd = list.getMetaData();
			int numColumns = rsmd.getColumnCount();

			tabla += "<tr>";
			for (int i=1; i<numColumns+1; i++) {
				tabla += "<th>" + rsmd.getColumnName(i) + "</th>";
			}
			tabla += "</tr>";
			
			while (list.next()) {
				tabla += "<tr>";
				for (int i=1; i<numColumns+1; i++) {
					tabla += "<td>" + list.getString(i) + "</td>";					
				}
				tabla += "</tr>";
			}

		} catch (SQLException e) {
			renderText("");
		}

		tabla += "</table>";

		renderHtml(tabla);
	}
}
