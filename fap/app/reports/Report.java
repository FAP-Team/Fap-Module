package reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer.PageSize;

import play.Play;
import play.modules.pdf.PDF;
import play.modules.pdf.PDF.MultiPDFDocuments;
import play.modules.pdf.PDF.Options;
import play.mvc.Scope;
import play.mvc.Scope.RenderArgs;
import play.vfs.VirtualFile;

import java.util.*;

public class Report {

	private String template;
	private String header;
	private String footer;
	private PageSize pageSize;

	/**
	 * @param template Plantilla que se sustituirá para generar el PDF
	 */
	public Report(String template) {
		this.template = template;
		normalSize();
	}

	/**
	 * @param header Plantilla de la cabecera
	 * @return
	 */
	public Report header(String header) {
		this.header = header;
		return this;
	}

	/**
	 * @param footer Plantilla del pie de página
	 * @return
	 */
	public Report footer(String footer) {
		this.footer = footer;
		return this;
	}

	/**
	 * Ajusta el tamaño del pdf a A4
	 * @return
	 */
	public Report normalSize() {
		this.pageSize = new PageSize(20.8d, 29.6d, 1d, 1d, 1d, 2.7d);
		return this;
	}

	/**
	 * Ajusta el tamaño del pdf para dejar margen en la parte inferior
	 * para la información de registro que inserta platino.
	 * 
	 * @return
	 */
	public Report registroSize() {
		this.pageSize = new PageSize(20.8d, 29.6d, 1d, 1d, 5d, 2.7d);
		return this;
	}
	
	private MultiPDFDocuments getRenderOptions(){
		MultiPDFDocuments m = new MultiPDFDocuments();
		Options opciones = new Options();
		if (footer != null)
			opciones.FOOTER_TEMPLATE = footer;

		if (header != null)
			opciones.HEADER_TEMPLATE = header;

		opciones.pageSize = pageSize;
		m.add(template, opciones);
		return m;
	}
	
	/**
	 * Renderiza un PDF a un fichero temporal.
	 * 
	 * Este método únicamente puede ser utilizada desde los controladores.
	 * Para utilizarla desde otra clase, utilizar el método que recibe
	 * como parametro un Map<String, Object>.
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
		File tmp = File.createTempFile("report_", ".pdf", Play.tmpDir);
		
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
}
