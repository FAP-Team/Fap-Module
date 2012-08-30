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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Injector;

import messages.Messages;
import models.Agente;
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
import play.mvc.Util;
import play.mvc.results.Ok;
import properties.FapProperties;
import reports.Report;
import utils.BinaryResponse;
import validation.CustomValidation;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.PlantillasDocControllerGen;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.net.URL;
import java.net.URLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


public class PlantillasDocController extends PlantillasDocControllerGen {
	
	// Entidades a excluir en el plugin de TinyMCE que inserta una entidad FAP en una plantilla del editor
	private static final String[] ENTIDADES_A_EXCLUIR = {"Singleton", "TableKeyValue", "TableKeyValueDependency", "Quartz"};
    private static final int FILENAME_SIZE = 6;
    
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
	 * Devolvemos al plugin del editor TinyMCE un JSON con las plantillas guardadas
	 */
	public static void insertarPlantilla() {
		// Ordeno esta consulta en orden alfabético inverso, para que quede en el orden correcto en el <select>
		List<PlantillaDocumento> plantillas = PlantillaDocumento.find("select plantilla from PlantillaDocumento plantilla order by plantilla.nombrePlantilla desc").fetch();
		String rutaMasNombrePlantilla,
			   jsonString = "[";
		for(int i = 0; i < plantillas.size(); i++) {
			rutaMasNombrePlantilla = "/" + generarPlantilla(plantillas.get(i).plantilla); 
			jsonString += "{\"idPlantilla\" : \"" + plantillas.get(i).id + "\", \"nombrePlantilla\" : \"" + plantillas.get(i).nombrePlantilla + "\", \"ruta\" : \"" + rutaMasNombrePlantilla + "\"}";
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
		renderJSON(response.toJSON("id", "nombrePlantilla"));
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en TextEditor.html
	 */
	public static void getContenidoPlantilla(Long idPlantilla) {
		PlantillaDocumento plantilla = PlantillaDocumento.findById(idPlantilla);
		renderText(plantilla.plantilla);
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en dialog.js (plugin 'guardar' del TinyMCE).
	 * 
	 * Para guardar la plantilla como un nuevo documento, en idPlantilla debe venir la cadena "nuevo".
	 */
	public static void guardarPlantilla(String idPlantilla, String nombrePlantilla, String contenido) {
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
		plantilla.save();
		renderText(plantilla.id.toString());
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en template.js (plugin 'headerfooter' del TinyMCE).
	 * 
	 * Para guardar la cabecera y el pie de la plantilla actual del editor.
	 */
	public static void guardarHeaderFooter(Long idHeader, Long idFooter, Long idPlantilla) {
		PlantillaDocumento plantilla = PlantillaDocumento.findById(idPlantilla);
		plantilla.idHeader = idHeader;
		plantilla.idFooter = idFooter;
		plantilla.save();
		ok();
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en template.js (plugin 'headerfooter' del TinyMCE).
	 * 
	 * Retorna los ids de la cabecera y el pie de la plantilla con idPlantilla.
	 */
	public static void getHeaderFooter(Long idPlantilla) {
		PlantillaDocumento plantilla = PlantillaDocumento.findById(idPlantilla);
		String headerFooter = "[{ \"idHeader\" : \"" + plantilla.idHeader + "\", " +
								 "\"idFooter\" : \"" + plantilla.idFooter + "\"}]";
		renderJSON(headerFooter);
	}
	
	
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
	 * Genera la plantilla con el texto dado, a partir de una plantilla base, y la almacena en el temporal
	 * correspondiente,
	 * 
	 * Retorna la ruta + nombre de la plantilla
	 */
	private static String generarPlantilla(String contenido) {
		String nombreFichero = "tmp_" + Codec.UUID().substring(0, FILENAME_SIZE);
		String rutaMasNombreFicheroTemporal = null;
		
		// Quitamos las imágenes que respresentan en la plantilla al salto de línea
		// FIXME: arreglar esta chapuza de sustitución (reemplazamos la imagen de pagebreak por un pixel transparente). 
		contenido = contenido.replaceAll("pagebreak.png", "pixel_transparente.png");
		try {
			BufferedReader reader = null;
			// XXX: Revisar ruta
			if(Play.mode.isDev())	// modo desarrollo
				reader = new BufferedReader(new FileReader("../../fap/app/views/reports/plantilla-base-editor.html"));
			else 					// modo producción
				reader = new BufferedReader(new FileReader("modules/fap/app/views/reports/plantilla-base-editor.html"));
			rutaMasNombreFicheroTemporal = FapProperties.get("fap.path.editor.tmp") + "/" + nombreFichero + ".html";
			PrintWriter writer = new PrintWriter(new FileWriter(rutaMasNombreFicheroTemporal));
			String line = null;
			while ((line = reader.readLine()) != null)
				writer.println(line.replaceAll("&contenido&",contenido));
		    reader.close();
		    writer.close();
		} catch (Exception e) { e.printStackTrace(); }

		return rutaMasNombreFicheroTemporal;
	}
	
	/*
	 * Genera la plantilla del header/footer
	 * 
	 * Retorna la ruta + nombre de la plantilla
	 */
	private static String generarHeaderFooter(String contenido) {
		String nombreFichero = "tmp_" + Codec.UUID().substring(0, FILENAME_SIZE);
		String rutaMasNombreFicheroTemporal = FapProperties.get("fap.path.editor.tmp") + "/" + nombreFichero + ".html";
		try {
		    BufferedWriter fichero = new BufferedWriter(new FileWriter(rutaMasNombreFicheroTemporal));
			fichero.write(contenido);
			fichero.close();
		} catch (Exception e) { e.printStackTrace(); }	
		
		return rutaMasNombreFicheroTemporal;
	}
	
	/*
	 * ¡¡Aviso!! Está puesta la url de este controlador "hardcodeada" en el .init de TinyMCE (TextEditor.html)
	 * Genera un pdf a partir del texto actual del editor.
	 */
	public static void html2pdf(String contenido, Long idPlantilla) {	
		if (contenido == null)
			contenido = "";
		
		// Obtenemos el header y el footer
		PlantillaDocumento plantilla;
		String header = null, footer = null;
		if (idPlantilla != null) {
			plantilla = PlantillaDocumento.findById(idPlantilla);
			if (plantilla.idHeader != null) {
				PlantillaDocumento plantillaHeader = PlantillaDocumento.findById(plantilla.idHeader);
				header = generarHeaderFooter(plantillaHeader.plantilla);
				
			}
			if (plantilla.idFooter != null) {
				PlantillaDocumento plantillaFooter = PlantillaDocumento.findById(plantilla.idFooter);
				footer = generarHeaderFooter(plantillaFooter.plantilla);
			}
		}
		
		String rutaMasNombreFicheroTemporal = generarPlantilla(contenido);
		File borrador = null;
		
		try {
				if (header == null && footer == null)
					borrador = new Report(rutaMasNombreFicheroTemporal).renderTmpFile();
				else if (header != null && footer != null)
					borrador = new Report(rutaMasNombreFicheroTemporal).header(header).footer(footer).renderTmpFile();
				else if (header != null)
					borrador = new Report(rutaMasNombreFicheroTemporal).header(header).renderTmpFile();
				else	// footer != null
					borrador = new Report(rutaMasNombreFicheroTemporal).footer(footer).renderTmpFile();
		} catch (Exception e) { e.printStackTrace(); }
		
		// Copiamos el pdf de la carpeta temporal de la aplicación a /public/tmp
		try {
			Process proc = null;
			String osName = System.getProperty("os.name" );
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
	 * las heredadas del módulo FAP)
	 */
	public static void obtenerListaEntidades() {
		List<String> listaEntidades = new ArrayList<String>();
		List<String> listaEntidadesAExluir = new ArrayList<String>(); // entidades que no queremos que aparezcan en la lista para insertarlas en las plantillas
		List<String> listaEntidadesPadre = new ArrayList<String>();	 // entidades del módulo FAP que son padres de alguna entidad de la aplicación (hija extends padre)
																	 // (para eliminarlas del listado de entidades que se presenta)		
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
	    for (Field field: type.getDeclaredFields()) {
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
		
		String tipo,						// tipo genérico del Field 
				entidadAtributo = null, 	// almacenamos la entidad del atributo (si corresponde)
				jsonString = "[";			// json que vamos a renderizar
		boolean entidadPropia;				// si un atributo es un tipo de java o es una entidad de nuestra aplicación
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
}
