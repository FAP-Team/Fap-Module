package controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Injector;

import messages.Messages;
import models.Agente;
import models.SolicitudGenerica;
import models.CodigoExclusion;
import models.Convocatoria;
import models.ImagenesPlantilla;
import models.PlantillaDocumento;
import models.RepresentantePersonaFisica;
import models.SolicitudGenerica;
import play.Play;
import play.db.jpa.JPABase;
import play.libs.Codec;
import play.libs.IO;
import play.modules.pdf.PDF;
import play.modules.pdf.PDF.MultiPDFDocuments;
import play.modules.pdf.PDF.Options;
import play.mvc.Util;
import play.mvc.results.Ok;
import properties.FapProperties;
import reports.Report;
import reports.ReportFAP;
import utils.BinaryResponse;
import validation.CustomValidation;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.fap.UtilsController;
import controllers.gen.PlantillasDocControllerGen;
import es.gobcan.eadmon.aed.ws.dominio.Solicitud;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.net.URL;
import java.net.URLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.log4j.Logger;


public class PlantillasDocController extends PlantillasDocControllerGen {

	// Entidades a excluir en el plugin de TinyMCE que inserta una entidad FAP en una plantilla del editor
	private static final String[] ENTIDADES_A_EXCLUIR = {"Singleton", "TableKeyValue", "TableKeyValueDependency", "Quartz"};
	// Entidades a incluir en el plugin de TinyMCE que inserta una entidad FAP en una plantilla del editor
	private static final String[] ENTIDADES_A_INCLUIR = {"Agente"};
	// Clases que tienen un métod getClase() en el paquete controllers.fap para poder instanciarlas 
	// Ej: getAgente() en AgenteControllers.java
	private static final List<String> GET_OBJECT_EN_CONTROLLERSFAP = 
			Collections.unmodifiableList( new ArrayList<String>(Arrays.asList("Agente"/*, ""*/)) );
    
	public static void index(String accion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/TextEditor/TextEditor.html");
		}
		log.info("Visitando página: " + "fap/TextEditor/TextEditor.html");		
		renderTemplate("fap/TextEditor/TextEditor.html");
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en dialog.html (plugin 'template' del TinyMCE).
	 * Devolvemos al plugin del editor TinyMCE un JSON con las plantillas html generadas
	 */
	public static void insertarPlantilla() {
		// Ordeno esta consulta en orden alfabético inverso, para que quede en el orden correcto en el <select>
		List<PlantillaDocumento> plantillas = PlantillaDocumento.find("select plantilla from PlantillaDocumento plantilla order by plantilla.nombrePlantilla desc").fetch();
		String rutaMasNombrePlantilla,
			   jsonString = "[";
		for(int i = 0; i < plantillas.size(); i++) {
			rutaMasNombrePlantilla = "/" + ReportFAP.generarPlantilla(plantillas.get(i).plantilla, false);
			jsonString += "{\"idPlantilla\" : \"" + plantillas.get(i).id + "\", \"nombrePlantilla\" : \"" + plantillas.get(i).nombrePlantilla + 
						  "\", \"ruta\" : \"" + rutaMasNombrePlantilla + "\", \"descripcion\" : \"" + plantillas.get(i).descripcion  +  "\"}";
			if (i < plantillas.size()-1 )
				jsonString += ", ";
		}
		jsonString += "]";
		renderJSON(jsonString);
	}

	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en dialog.html (plugin 'guardar' del TinyMCE).
	 * Devolvemos al plugin del editor TinyMCE un JSON con las propiedades de las todas las plantillas de la base de datos
	 */
	public static void getAllPlantillas() {
		List<PlantillaDocumento> rows = PlantillaDocumento.find("select plantilla from PlantillaDocumento plantilla order by plantilla.nombrePlantilla asc").fetch();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		tables.TableRenderResponse<PlantillaDocumento> response = new tables.TableRenderResponse<PlantillaDocumento>(rows, false, false, false, "", "", "", getAccion(), ids);
		renderJSON(response.toJSON("id", "nombrePlantilla", "descripcion"));
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en TextEditor.html
	 */
	public static void getContenidoPlantilla(Long idPlantilla) {
		PlantillaDocumento plantilla = PlantillaDocumento.findById(idPlantilla);
		renderText(plantilla.plantilla);
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en TextEditor.html
	 * Comprobamos que no se creen dos plantillas con el mismo nombre.
	 */
	public static void comprobarNombrePlantillaUnico(String nombrePlantilla) {
		List<PlantillaDocumento> listaPlantillas = PlantillaDocumento.find("select plantilla from PlantillaDocumento plantilla " +
																		   "where nombrePlantilla = '" + nombrePlantilla + "'").fetch();
		boolean duplicado = false;
		if ( !listaPlantillas.isEmpty() )
			duplicado = true;

		renderJSON( "[{ \"duplicado\" : \"" + duplicado + "\"}]" );
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en dialog.js (plugin 'guardar' del TinyMCE).
	 * 
	 * Para guardar la plantilla como un nuevo documento, en idPlantilla debe venir la cadena "nuevo".
	 */
	public static void guardarPlantilla(String idPlantilla, String nombrePlantilla, String descripcionPlantilla, String contenido) {
		if ( (idPlantilla == null) || (contenido == null) )
			error();

		PlantillaDocumento plantilla;
		if (idPlantilla.equals("nuevo")) {
			if ( (nombrePlantilla == null) || (nombrePlantilla.isEmpty()) ) 
				nombrePlantilla = "Nueva Plantilla";
			plantilla = new PlantillaDocumento();
			plantilla.nombrePlantilla = nombrePlantilla;
		}
		else 
			plantilla = PlantillaDocumento.findById(Long.valueOf(idPlantilla));

		plantilla.plantilla = contenido;
		if ( (descripcionPlantilla != null) && (!descripcionPlantilla.isEmpty()) ) 
			plantilla.descripcion = descripcionPlantilla;
		plantilla.save();
		renderText(plantilla.id);
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en template.js (plugin 'headerfooter' del TinyMCE).
	 * 
	 * Retorna los ids de la cabecera y el pie de la plantilla con idPlantilla.
	 */
	/*
	public static void getHeaderFooter(Long idPlantilla) {	
		List<PlantillaDocumentoEncabezado> listaPlantillas = PlantillaDocumentoEncabezado.find("select plantilla from PlantillaDocumentoEncabezado plantilla " +
				   																		 "where plantillaDocumento.id = '" + idPlantilla + "'").fetch();
		
		String jsonString = "[";			// json que vamos a renderizar
		for (int i = 0; i < listaPlantillas.size(); i++) {
			jsonString += "{\"idHeader\" : \"" + listaPlantillas.get(i).header.id + "\", \"idFooter\" : \"" + listaPlantillas.get(i).footer.id + "\"}";
			if (i < listaPlantillas.size()-1 )
				jsonString += ", ";
		}
		jsonString += "]";
		renderJSON(jsonString);
	}
	*/
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en dialog.js (plugin 'eliminarplantilla' del TinyMCE).
	 */
	public static void eliminarPlantilla(Long idPlantilla) {	
		if (idPlantilla == null)
			error();
		
		PlantillaDocumento plantilla = PlantillaDocumento.findById(idPlantilla);
		String nombrePlantilla = plantilla.nombrePlantilla;
		plantilla.delete();
		play.Logger.info("El usuario <" + AgenteController.getAgente().username + "> ha eliminado la plantilla con id=" + idPlantilla + ": " + nombrePlantilla);
		ok();
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en dialog.html (plugin 'insertarimg' del TinyMCE).
	 * Devolvemos al plugin del editor TinyMCE un JSON con las propiedades de las imágenes guardadas
	 */
	public static void insertarImagen() {
		// Ordeno esta consulta en orden alfabético inverso, para que quede en el orden correcto en el <select>
		List<ImagenesPlantilla> rows = ImagenesPlantilla.find("select img from ImagenesPlantilla img order by img.nombreImagen desc").fetch();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		tables.TableRenderResponse<ImagenesPlantilla> response = new tables.TableRenderResponse<ImagenesPlantilla>(rows, false, false, false, "", "", "", getAccion(), ids);
		renderJSON(response.toJSON("uriImagen", "nombreImagen"));
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en dialog.html (plugin 'insertarimg' del TinyMCE).
	 * Subida de imágenes para las plantillas del editor TinyMCE 
	 */
	public static void uploadImagen(File imagen) throws FileNotFoundException {
		if (imagen == null) {
			throw new FileNotFoundException();
		}

		ImagenesPlantilla imgPlantilla = new ImagenesPlantilla();
		imgPlantilla.nombreImagen = imagen.getName().trim();
		String pathCarpetaImg = FapProperties.get("fap.path.editor.img");
		imgPlantilla.uriImagen = pathCarpetaImg + "/" + imagen.getName();
		imgPlantilla.save();
		File file = new File(pathCarpetaImg, imgPlantilla.nombreImagen);
        try {
        	IO.write(new FileInputStream(imagen), file);
        } catch(Exception e) { e.printStackTrace(); };
        imgPlantilla.save();
		redirect("controllers.PlantillasDocController.index");
	}
	
	/*
	 * Devuelve la ruta donde se almacenan las imágenes para el editor TinyMCE, dada en las properties
	 * 
	 */
	public static void getRutaImagen() {
		String rutaImagen = "[{ \"ruta\" : \"" + FapProperties.get("fap.path.editor.img") + "\"}]";
		renderJSON(rutaImagen);
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en el .init de TinyMCE (TextEditor.html)
	 * Genera un pdf a partir del texto actual del editor.
	 */
	public static void html2pdf(String contenido, Long idPlantilla, Long idHeader, Long idFooter, boolean sustituirEntidades) {	
		if (contenido == null)
			contenido = "";

		contenido = insertarPlantillaEnContenido(contenido);		// si se requiere una plantilla en medio del contenido

		File borrador = null;
		try {
			if (!sustituirEntidades) { 
				contenido = contenido.replaceAll( Pattern.quote("${"), "-->{");
				borrador = new ReportFAP(contenido, false).header(idHeader).footer(idFooter).renderTmpFile();
			}
			else {
				String plantilla = null;
				Map<String, Object> listaObjetos = new HashMap<String, Object>();			
				plantilla = ReportFAP.generarPlantilla(contenido, false);
				Set<String> listaEntidades = new HashSet<String>();
				listaEntidades = getListaEntidadesASustituir(plantilla);
				listaObjetos = getListaObjetosASustituir(listaEntidades);
				borrador = new ReportFAP(contenido, false).header(idHeader).footer(idFooter).renderTmpFile(listaObjetos);	
			}
		} catch (Exception e) { e.printStackTrace(); }
	
		// Copiamos el pdf de la carpeta temporal de la aplicación a /public/tmp
		Process proc = null;
		try {		
			String osName = System.getProperty("os.name");
			if (osName.contains("Win"))			// windows
				proc = Runtime.getRuntime().exec("copy " + Play.tmpDir + "/" + borrador.getName() + " public/tmp/");
			else								// linux, mac, ...
				proc = Runtime.getRuntime().exec("cp " + Play.tmpDir + "/" + borrador.getName() + " public/tmp/");
			/*
			InputStream is = proc.getInputStream();
			int size;
			String s;
			int exCode = proc.waitFor();
			StringBuffer ret = new StringBuffer();
			while((size = is.available()) != 0) {
				byte[] b = new byte[size];
				is.read(b);
				s = new String(b);
				ret.append(s);
			}
			 */
		} catch (Exception e) { e.printStackTrace(); }

		renderText("/" + FapProperties.get("fap.path.editor.tmp") + "/" + borrador.getName());
	}

	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en dialog.html (plugin 'insertarentidadfap' del TinyMCE).
	 * Devolvemos al plugin del editor TinyMCE una lista con todas las entidades (propias de la aplicación más 
	 * las heredadas del módulo FAP).
	 */
	public static void obtenerListaEntidades() {
		List<String> listaEntidades = new ArrayList<String>();
		List<String> listaEntidadesAExluir = new ArrayList<String>(); // entidades que no queremos que aparezcan en la lista para insertarlas en las plantillas
		// Entidades del módulo FAP que son padres de alguna entidad de la aplicación (hija extends padre) (para eliminarlas del listado de entidades que se presenta)	
		List<String> listaEntidadesPadre = new ArrayList<String>();	 

		// Si la lista de entidades disponibles es la lista constante ENTIDADES_A_INCLUIR
		if (FapProperties.getBoolean("fap.editor.entidades.incluir")) {
			listaEntidades = Arrays.asList(ENTIDADES_A_INCLUIR);
			Collections.sort(listaEntidades, Collections.reverseOrder());
			renderJSON(listaEntidades);	
		}
		
		// Si la lista de entidades disponibles son todas las de la aplicación exceptuando la lista constante ENTIDADES_A_EXCLUIR
		for (String entidad : ENTIDADES_A_EXCLUIR)
			listaEntidadesAExluir.add(entidad);
	
		try {
			// Primero obtenemos las entidades propias de la aplicación
			// XXX: Revisar ruta
			BufferedReader reader = new BufferedReader(new FileReader("app/led/Entidades.fap")); 
			String line = null;
			while ((line = reader.readLine()) != null) {
				// Lista de entidades de la aplicación
				Pattern pattern = Pattern.compile("^Entidad ([_A-Za-z0-9]+)");
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) 
					listaEntidades.add(matcher.group(1));	
				// Lista de las entidades padre de las que hereda alguna entidad de la aplicación
				pattern = Pattern.compile("^Entidad ([_A-Za-z0-9]+) extends ([_A-Za-z0-9]+)");
				matcher = pattern.matcher(line);
				while (matcher.find()) 
					listaEntidadesPadre.add(matcher.group(2));
			}
			
			// Ahora obtenemos las entidades del módulo fap (excluyendo las que no nos interesan)
			// XXX: Revisar ruta
			if(Play.mode.isDev())	// modo desarrollo
				reader = new BufferedReader(new FileReader("../../fap/app/led/fap/Entidades.fap"));
			else					// modo producción
				reader = new BufferedReader(new FileReader("modules/fap/app/led/fap/Entidades.fap"));
			line = null;
			while ((line = reader.readLine()) != null) {
				Pattern pattern = Pattern.compile("^Entidad ([_A-Za-z0-9]+)");
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					if ( !listaEntidades.contains(matcher.group(1)) && !listaEntidadesPadre.contains(matcher.group(1)) &&
							!listaEntidadesAExluir.contains(matcher.group(1)) ) {
						listaEntidades.add(matcher.group(1));
					}
				}
			}
		    reader.close();
		} catch (Exception e1) {e1.printStackTrace();}
		
		Collections.sort(listaEntidades, Collections.reverseOrder());
		renderJSON(listaEntidades);
	}
	
	/*
	 * Devuelve todos los atributos de una entidad (sin profundizar en las relaciones con otras entidades)
	 * 
	 */
	public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
	    for (Field field: type.getFields()) {
	        fields.add(field);
	    }
	    
	    return fields;
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en dialog.html (plugin 'insertarentidadfap' del TinyMCE).
	 * Devolvemos al plugin del editor TinyMCE una lista con todos los atributos de la entidad seleccionada 
	 */
	public static void obtenerAtributosEntidad(String entidad) {
		Class clase = null;
		try {
			clase = Class.forName("models."+entidad);
		} catch (ClassNotFoundException e) {e.printStackTrace();}
		
		List<Field> listaFields = getAllFields(new LinkedList<Field>(), clase);
		// Ordenamos por el nombre del atributo
		Collections.sort(listaFields, new Comparator(){
					            public int compare(Object o1, Object o2) {
					                Field f1 = (Field) o1;
					                Field f2 = (Field) o2;
					               return f1.getName().compareToIgnoreCase(f2.getName());
					            }		
						});
		
		String tipo,						// tipo genérico del Field 
				entidadAtributo = null, 	// almacenamos la entidad del atributo (si corresponde)
				jsonString = "[";			// json que vamos a renderizar
		boolean entidadPropia;				// si un atributo es un tipo java o es una entidad de nuestra aplicación
		for (int i = 0; i < listaFields.size(); i++) {
			entidadPropia = false;
			tipo = 	listaFields.get(i).getGenericType().toString();	// Ej: java.util.List<models.Tema>
			Pattern pattern = Pattern.compile("models\\.([_A-Za-z0-9]+)");
			Matcher matcher = pattern.matcher(tipo);
			while (matcher.find()) {
				entidadAtributo = matcher.group(1);
				entidadPropia = true;
			}
			if (entidadPropia)  
				jsonString += "{\"nombre\" : \"" + listaFields.get(i).getName() + "\", \"entidad\" : \"" + entidadAtributo + "\"}";
			else
				jsonString += "{\"nombre\" : \"" + listaFields.get(i).getName() + "\"}";
			if (i < listaFields.size()-1 )
				jsonString += ", ";
		}
		jsonString += "]";
		renderJSON(jsonString);
	}

	/*
	 * Si no hay entidades a sustituir se invalida la cadena ${} que permite al report sustituir los valores internamente
	 * 
	 */
	public static Set<String> getListaEntidadesASustituir(String plantilla) {
		Set<String> listaEntidades = new HashSet<String>();
		try {
			BufferedReader reader = null;
			reader = new BufferedReader(new FileReader(plantilla));
			String line = null;
			Pattern pattern = Pattern.compile( ".*?" + Pattern.quote("${") + "([_A-Za-z0-9\\.]+)" + Pattern.quote("}"));
			Matcher matcher;
			while ((line = reader.readLine()) != null) {
				
				matcher = pattern.matcher(line);
				while (matcher.find())
					listaEntidades.add( matcher.group(1).split("\\.")[0] );   	// cogemos la entidad (Ej: "Agente" de Agente.username)
			}
			reader.close();
		} catch (Exception e) { e.printStackTrace(); }
		
		return listaEntidades;
	}
	
	/**
	 * Pasándole una lista de entidades, devuelve el objeto instanciado de cada una de ellas.
	 * 
	 * @param listaEntidades
	 * @return Lista de los objetos instanciados
	 */
	public static Map<String, Object> getListaObjetosASustituir(Set<String> listaEntidades) {
		Map<String, Object> objetosARenderizar = new HashMap<String, Object>();
		String nombreClase = null, 
				rutaControlador = null;
		Object obj;
		Class clase;
		
		for (String entidad : listaEntidades) {
			obj = clase = null;
			nombreClase = entidad.substring(0,1).toUpperCase() + entidad.substring(1);		// del editor viene con la clase en minúscula				
			
			if ( GET_OBJECT_EN_CONTROLLERSFAP.contains(nombreClase) )
				rutaControlador = "controllers.fap.";
//			if  ( GET_OBJECT_EN_CONTROLLERSGEN.contains(nombreClase) ) 
//				rutaControlador = "controllers.gen.";
			else
			 	rutaControlador = "controllers.";
			
			try {
				clase = Class.forName(rutaControlador + nombreClase + "Controller");
				Method method = clase.getMethod("get" + nombreClase, new Class[0]);
				obj = method.invoke("get" + nombreClase, new Object[0]);
			} catch (Exception e) { e.printStackTrace(); }
			
			objetosARenderizar.put(entidad, obj);
		}
		
		return objetosARenderizar;
	}
	
	/**
	 * Sustituye el tag propio del editor (insertado con el plugin "requerirplantilla") con su contenido
	 * correspondiente buscado en base de datos.
	 * 
	 * @param contenido Cadena de texto capturada en el editor, del que vamos a sustituir si tiene una plantilla
	 * requerida, por su contenido correspondiente.
	 * @return Plantilla ya sustituida
	 */
	public static String insertarPlantillaEnContenido(String contenido) {
		Pattern pattern = Pattern.compile("@([_A-Za-z0-9]+)@");		// ejemplo de tag: @plantilla.html@
		Matcher matcher = pattern.matcher(contenido);
		PlantillaDocumento plantilla; 
		while (matcher.find()) {
			plantilla = PlantillaDocumento.find("select plantilla from PlantillaDocumento plantilla where nombrePlantilla = '" + matcher.group(1) + "'").first();
			contenido = contenido.replaceFirst("@" + matcher.group(1) + "@", plantilla.plantilla);
		}
		return contenido;
	}
}
