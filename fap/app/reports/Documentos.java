package reports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.lang.StringUtils;

import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.exceptions.TemplateNotFoundException;
import play.modules.pdf.PDF;
import play.modules.pdf.PDF.MultiPDFDocuments;
import play.modules.pdf.PDF.Options;
import play.modules.pdf.PDF.PDFDocument;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Util;
import play.templates.Template;
import play.templates.TemplateLoader;
import utils.TemplateUtils;

public class Documentos{
		
	
	
	public static void enviar(String template, Object... args) {
		enviar(template, null, args);
	}
	
	public static void enviar(String template, String footer, Object... args) {
		MultiPDFDocuments m = new MultiPDFDocuments();
		Options opciones = new Options();
		opciones.FOOTER_TEMPLATE = footer;
		opciones.HEADER_TEMPLATE = "reports/header.html";
		opciones.pageSize = new IHtmlToPdfTransformer.PageSize(20.8d, 29.6d, 1d, 1d, 4d, 3.5d);
		m.add(template, opciones);
		PDF.renderTemplateAsPDF(null, m, args);
	}

	public static void enviarDocumentoSimple(String template, Object... args) {
		enviarDocumentoSimple(template, null, args);
	}

	public static void enviarDocumentoSimple(String template, String name, Object... args) {
		MultiPDFDocuments m = new MultiPDFDocuments();
		m.filename = name;
		m.add(template, new Options());
		PDF.renderTemplateAsPDF(null, m, args);
	}

	
	public static void enviarBorrador(Object... args) {
		enviar("reports/solicitud.html", "reports/footer.html", args);
	}
	
	public static void renderTmpFile(String template, String header, String footer, Object... args){
		
	}
	
 }
