
package controllers.fap;

import play.*;
import play.mvc.*;
import controllers.fap.*;
import tags.ReflectionUtils;
import validation.*;
import models.*;
import java.util.*;

import messages.Messages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.config.PropertyGetter.PropertyCallback;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import emails.Mails;


public class IncidenciasController extends GenericController {

	public static void index(){
		renderTemplate( "fap/Admin/incidencias.html" );
	}


	public static void incidencias(){
		
		java.util.List<Incidencia> rows = new ArrayList<Incidencia>();
        rows = Incidencia.findAll();
		tables.TableRenderNoPermisos<Incidencia> response = new tables.TableRenderNoPermisos<Incidencia>(rows);
		
		flexjson.JSONSerializer flex = new flexjson.JSONSerializer().include("rows.fecha", "rows.nombre", "rows.apellidos", "rows.email", "rows.telefono", "rows.asunto", "rows.texto").transform(new serializer.DateTimeTransformer(), org.joda.time.DateTime.class).exclude("*");
		String serialize = flex.serialize(response);
		renderJSON(serialize);

	}
}
