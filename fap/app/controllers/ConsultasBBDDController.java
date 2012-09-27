package controllers;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.Query;

import play.db.DB;
import play.db.jpa.JPA;
import messages.Messages;
import models.Consulta;
import controllers.gen.ConsultasBBDDControllerGen;

public class ConsultasBBDDController extends ConsultasBBDDControllerGen {

	public static void index(String accion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/Admin/consultas.html");
		}

		log.info("Visitando página: " + "fap/Admin/consultas.html");
		renderTemplate("fap/Admin/consultas.html", accion);
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
