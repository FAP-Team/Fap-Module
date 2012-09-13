package reports;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.Set;

import models.PlantillaDocumento;
import models.PlantillaDocumentoEncabezado;

import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer.PageSize;

import play.Play;
import play.libs.Codec;
import play.modules.pdf.PDF;
import play.modules.pdf.PDF.MultiPDFDocuments;
import play.modules.pdf.PDF.Options;
import play.mvc.Scope;
import play.mvc.Scope.RenderArgs;
import play.vfs.VirtualFile;
import properties.FapProperties;

import java.util.*;

public class ReportFAP {

	private String template;
	private String header;
	private String footer;
	private String fileName;
	private PageSize pageSize;

	private static final int FILENAME_SIZE = 9;   // Tamaño de la parte aleatoria en los nombres de ficheros
	private static final String PLANTILLA_BASE = "plantillaBaseEditor.html";
	
	/**
	 * @param template Plantilla que se sustituirá para generar el PDF
	 */
	public ReportFAP(String template) {
		this(template, true, true);
	}
	
	/**
	 * @param idTemplate Identificador de la plantilla que se sustituirá para generar el PDF
	 */
	public ReportFAP(Long idTemplate) {
		this(idTemplate, true, true);
	}

	/**
	 * @param template Contenido que se sustituirá para generar el PDF
	 * @param contenidoEnFichero Con valor 'false', el contenido que queremos renderizar a PDF, no está físicamente en un fichero, 
	 * 			sino que se lo pasamos como una cadena de texto en el primer argumento.
	 * @param sustituirEntidades no sustituir el valor de las entidades ({$Agente.username}). 
	 */
	public ReportFAP(String template, boolean contenidoEnFichero, boolean sustituirEntidades) {
		String plantilla = generarPlantilla(template, contenidoEnFichero);
		System.out.println("********** [ReportFAP] sustituirEntidades = " + sustituirEntidades);
		
		String nombreFichero = "tmp_" + Codec.UUID().substring(0, FILENAME_SIZE);
		String rutaMasNombreFicheroTemporal = null;
		
		if (!sustituirEntidades) {
			try {
				BufferedReader reader = null;
				// XXX: Revisar ruta
				reader = new BufferedReader(new FileReader(plantilla));
				rutaMasNombreFicheroTemporal = FapProperties.get("fap.path.editor.tmp") + "/" + nombreFichero + ".html";
				PrintWriter writer = new PrintWriter(new FileWriter(rutaMasNombreFicheroTemporal));
				String line = null;
				while ((line = reader.readLine()) != null) {
					// FIXME: mejorar esta sustitución
					writer.println(line.replaceAll( Matcher.quoteReplacement("$"),"0") );
				}
			    reader.close();
			    writer.close();
			} catch (Exception e) { e.printStackTrace(); }
			this.template = rutaMasNombreFicheroTemporal;
		}
		else
			this.template = plantilla;
		normalSize();
	}
	
	/**
	 * @param template Identificador de la plantilla que se sustituirá para generar el PDF
	 * @param contenidoEnFichero Con valor 'false', el contenido que queremos renderizar a PDF, no está físicamente en un fichero, 
	 * 			sino que se lo pasamos como una cadena de texto en el primer argumento.
	 * @param sustituirEntidades no sustituir el valor de las entidades ({$Agente.username}). 
	 */
	public ReportFAP(Long idTemplate, boolean contenidoEnFichero, boolean sustituirEntidades) {
		String nombrePlantilla = getNombreFromId(idTemplate);
		String plantilla = generarPlantilla(nombrePlantilla, contenidoEnFichero);
		
		String nombreFichero = "tmp_" + Codec.UUID().substring(0, FILENAME_SIZE);
		String rutaMasNombreFicheroTemporal = null;
		
		if (!sustituirEntidades) {
			// FIXME: arreglar esta chapuza de sustitución (reemplazamos la imagen de pagebreak por un pixel transparente). 
			try {
				BufferedReader reader = null;
				// XXX: Revisar ruta
				reader = new BufferedReader(new FileReader(plantilla));
				rutaMasNombreFicheroTemporal = FapProperties.get("fap.path.editor.tmp") + "/" + nombreFichero + ".html";
				PrintWriter writer = new PrintWriter(new FileWriter(rutaMasNombreFicheroTemporal));
				String line = null;
				while ((line = reader.readLine()) != null)
					// FIXME: mejorar esta sustitución
					writer.println(line.replaceAll( Matcher.quoteReplacement("$"),"0") );
			    reader.close();
			    writer.close();
			} catch (Exception e) { e.printStackTrace(); }
			this.template = rutaMasNombreFicheroTemporal;
		}
		else
			this.template = plantilla;
		
		normalSize();
	}
	
	/**
	 * @param header Plantilla de la cabecera
	 * @return
	 */
	public ReportFAP header(String header) {
		this.header = generarHeaderFooter(header, "header");
		return this;
	}

	/**
	 * @param footer Plantilla del pie de página
	 * @return
	 */
	public ReportFAP footer(String footer) {
		this.footer = generarHeaderFooter(footer, "footer");
		return this;
	}

	/**
	 * @param header Plantilla de la cabecera
	 * @return
	 */
	public ReportFAP header(Long idHeader) {
		String nombreHeader = getNombreFromId(idHeader);
		this.header = generarHeaderFooter(nombreHeader, "header");
		return this;
	}

	/**
	 * @param footer Plantilla del pie de página
	 * @return
	 */
	public ReportFAP footer(Long idFooter) {
		String nombreFooter = getNombreFromId(idFooter);
		this.footer = generarHeaderFooter(nombreFooter, "footer");
		return this;
	}
	
	/**
	 * Ajusta el tamaño del pdf a A4
	 * @return
	 */
	public ReportFAP normalSize() {
		this.pageSize = new PageSize(20.8d, 29.6d, 1d, 1d, 1d, 2.7d);
		return this;
	}

	/**
	 * Ajusta el tamaño del pdf para dejar margen en la parte inferior
	 * para la información de registro que inserta platino.
	 * 
	 * @return
	 */
	public ReportFAP registroSize() {
		this.pageSize = new PageSize(20.8d, 29.6d, 1d, 1d, 5d, 2.7d);
		return this;
	}
	
	/**
	 * @param filename Nombre asociado al archivo PDF a generar
	 * @return
	 */
	public ReportFAP fileName(String filename) {
		this.fileName = filename;
		return this;
	}

	private MultiPDFDocuments getRenderOptions(){
		MultiPDFDocuments m = new MultiPDFDocuments();
		Options opciones = new Options();
		if (footer != null)
			opciones.FOOTER_TEMPLATE = footer;

		if (header != null)
			opciones.HEADER_TEMPLATE = header;
		
		if (fileName != null)
			m.filename = fileName;

		opciones.pageSize = pageSize;
		m.add(template, opciones);
		return m;
	}
	
	/**
	 * Renderiza un PDF a un fichero temporal.
	 * 
	 * Este método únicamente puede ser utilizada desde los controladores. Para utilizarla desde 
	 * otra clase, utilizar el método que recibe como parametro un Map<String, Object>.
	 * 
	 * El fichero temporal lo crea en Play.tmpDir
	 * 
	 * @param args Parámetros que se sustituiran en la plantilla
	 * @return
	 * @throws Exception
	 */
	public File renderTmpFile(Object... args) throws Exception {		
		File tmp = File.createTempFile("report_", ".pdf", Play.tmpDir);
		PDF.renderTemplateAsPDF(new FileOutputStream(tmp), getRenderOptions(), args);
		return tmp;
	}
	
	/**
	 * Renderiza un PDF a un fichero temporal.
	 * 
	 * Utilizar este método si no se está haciendo la llamada desde
	 * un controlador.
	 * 
	 * El fichero temporal lo crea en Play.tmpDir
	 * 
	 * @param args Parámetros que se sustituiran en la plantilla
	 * @return
	 * @throws Exception
	 */
	public File renderTmpFile(Map<String, Object> args) throws Exception {
		String prefix = fileName != null? fileName : "report";
			
		File tmp = File.createTempFile(prefix + "_", ".pdf", Play.tmpDir);
		
		//Añade todos los parámetros a renderArgs
		RenderArgs renderArgs = Scope.RenderArgs.current();
		for(Entry<String, Object> entry : args.entrySet()){
			renderArgs.put(entry.getKey(), entry.getValue());
		}
		
		PDF.renderTemplateAsPDF(new FileOutputStream(tmp), getRenderOptions());
		return tmp;
	}
	
	/**
	 * Renderiza un PDF y genera una Http.Response con el contenido
	 * del pdf.
	 * 
	 * Este método únicamente se puede utilizar desde los controladores.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public void renderResponse(Object... args) throws Exception {
		PDF.renderTemplateAsPDF(null, getRenderOptions(), args);
	}
	
	/*
	 * Genera la plantilla con el texto dado, a partir de una plantilla base, y la almacena en el temporal
	 * correspondiente,
	 * 
	 * Retorna la ruta más el nombre de la plantilla
	 */
	public static String generarPlantilla(String template, boolean contenidoEnFichero) {
		if (template == null)
			return null;
		
		if ( contenidoEnFichero && isPlantillaEnReports(template) ) 
			return template;
		
		String contenido;
		if (contenidoEnFichero) {
			String[] path = template.split("/");
			template = path[path.length - 1];		// nombre de la plantilla, ej: foo.html
			PlantillaDocumento plantilla = PlantillaDocumento.find("select plantilla from PlantillaDocumento plantilla where nombrePlantilla = '" + template + "'").first();
			contenido = plantilla.plantilla;
		}
		else 
			contenido = template;
		
		String nombreFichero = "tmp_" + Codec.UUID().substring(0, FILENAME_SIZE);
		String rutaMasNombreFicheroTemporal = null;
		
		// Quitamos las imágenes que respresentan en la plantilla al salto de línea
		// FIXME: arreglar esta chapuza de sustitución (reemplazamos la imagen de pagebreak por un pixel transparente). 
		contenido = contenido.replaceAll("pagebreak.png", "pixel_transparente.png");
		try {
			BufferedReader reader = null;
			// XXX: Revisar ruta
			if(Play.mode.isDev())	// modo desarrollo
				reader = new BufferedReader(new FileReader("../../fap/app/views/reports/" + PLANTILLA_BASE));
			else 					// modo producción
				reader = new BufferedReader(new FileReader("modules/fap/app/views/reports/" + PLANTILLA_BASE));
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
	public static String generarHeaderFooter(String template, String headerOrFooter) {
		if (template == null)
			return null;
		
		if ( isPlantillaEnReports(template) )
			return template;
		
		String contenido = "";
		if (headerOrFooter == "header") {
			PlantillaDocumento headerPlantilla = PlantillaDocumento.find("select plantilla from PlantillaDocumento plantilla where " +
																		 "nombrePlantilla = '" + template + "'").first();
			contenido = headerPlantilla.plantilla; 
		}
		else if (headerOrFooter == "footer") {
			PlantillaDocumento footerPlantilla = PlantillaDocumento.find("select plantilla from PlantillaDocumento plantilla where " +
					 													 "nombrePlantilla = '" + template + "'").first();
			contenido = footerPlantilla.plantilla;
		}
		
		String nombreFichero = "tmp_" + Codec.UUID().substring(0, FILENAME_SIZE);
		String rutaMasNombreFicheroTemporal = FapProperties.get("fap.path.editor.tmp") + "/" + nombreFichero + ".html";
		try {
		    BufferedWriter fichero = new BufferedWriter(new FileWriter(rutaMasNombreFicheroTemporal));
			fichero.write(contenido);
			fichero.close();
		} catch (Exception e) { e.printStackTrace(); }	
		
		return rutaMasNombreFicheroTemporal;
	}
	
	/**
	 * Comprobamos si la plantilla existe en local
	 * 
	 * @param template
	 * @return True si el fichero existe, falso en caso contrario
	 */
	public static boolean isPlantillaEnReports(String template) {
		if ( (template == null) || (template.isEmpty()) )
			return false;
		
		// FIXME: ver ruta para comprobar que no está en el reports del módulo fap
		File fich1 = new File("../../fap/app/views/" + template);
		File fich2 = new File(template);
		if ( fich1.exists() || fich2.exists() )
			  return true;
	  
		return false;
	}
	
	/**
	 * Devuelve el nombre de la plantilla a partir de su id.
	 * 
	 * @param idTemplate 
	 * @return
	 */
	public static String getNombreFromId(Long idTemplate) {
		if (idTemplate == null)
			return null;
		PlantillaDocumento plantilla = PlantillaDocumento.findById(idTemplate);
		return plantilla.nombrePlantilla; 
	}
}
