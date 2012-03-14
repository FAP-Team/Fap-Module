package reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer.PageSize;

import play.modules.pdf.PDF;
import play.modules.pdf.PDF.MultiPDFDocuments;
import play.modules.pdf.PDF.Options;
import play.vfs.VirtualFile;

public class Report {

	private String template;
	private String header;
	private String footer;
	private PageSize pageSize;

	public Report(String template) {
		this.template = template;
		normalSize();
	}

	public Report header(String header) {
		this.header = header;
		return this;
	}

	public Report footer(String footer) {
		this.footer = footer;
		return this;
	}

	public Report normalSize() {
		this.pageSize = new PageSize(20.8d, 29.6d, 1d, 1d, 1d, 2.7d);
		return this;
	}

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
	 * Renderiza un PDF a un fichero temporal
	 * @param args Parámetros que se sustituiran en la plantilla
	 * @return
	 * @throws Exception
	 */
	public File renderTmpFile(Object... args) throws Exception {
		// TODO guardar en carpeta temporal de play
		File tmp = File.createTempFile("report_", ".pdf");
		PDF.renderTemplateAsPDF(new FileOutputStream(tmp), getRenderOptions(), args);
		return tmp;
	}
	
	public File renderTestTmpFile (Object... args) throws Exception {
		play.Logger.debug("Render temp file como servicio");
		File tmp = File.createTempFile("reportTest_", ".txt");
		play.Logger.debug("Creamos el fichero temporal en "+tmp.getAbsolutePath());
		Writer output = new BufferedWriter(new FileWriter(tmp));
		output.write("hola probando escribir en temporal en el fichero: "+tmp.getAbsolutePath());
		output.close();
		play.Logger.debug("Se realizó el renderTestTemplate al fichero "+tmp.getAbsolutePath());
		return tmp;
	}
	
	public void renderResponse(Object... args) throws Exception {
		PDF.renderTemplateAsPDF(null, getRenderOptions(), args);
	}
}
