package services.openofice;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import services.ConversorService;

import com.sun.star.beans.PropertyValue;

public class OpenOfficeConversor implements ConversorService{
	OfficeManager officeManager;
	String tempFilePath;
	
	public OpenOfficeConversor() {
		// TODO Auto-generated constructor stub
		 officeManager = new DefaultOfficeManagerConfiguration()
         //.setRetryTimeout(30000L)
         //.setTaskExecutionTimeout(60000L)
         .buildOfficeManager();
 tempFilePath = System.getProperty("java.io.tmpdir") + "\\ArFile\\PDF\\";
	}
	
	public File convertToPdf(File inputFile){
	    new File(tempFilePath).mkdirs();
	        String filename = inputFile.getName();
	        filename = filename.substring(0, filename.lastIndexOf("."));

	    File outputFile = new File(tempFilePath + filename + ".pdf");

	    System.out.println(outputFile + " : " + inputFile);
	    officeManager.start(); // may tweak the start and stop code to appear elsewhere for additional efficiency

	    DocumentFormat docFormat = new DocumentFormat("Portable Document Format", "pdf", "application/pdf");
	    Map map = new HashMap();
	    map.put("FilterName", "writer_pdf_Export");
	    PropertyValue[] aFilterData = new PropertyValue[1];
	    aFilterData[0] = new PropertyValue();
	    aFilterData[0].Name = "SelectPdfVersion";
	    aFilterData[0].Value = 1;
	    map.put("FilterData", aFilterData);
	    docFormat.setStoreProperties(DocumentFamily.TEXT, map);

	    OfficeDocumentConverter docConverter = new OfficeDocumentConverter(officeManager);
	    docConverter.convert(inputFile, outputFile, docFormat);

	    officeManager.stop();

	    return outputFile;
	}
	
	// returns true if the file format is known to be convertible into the PDF/A-1 format
		public boolean isConvertableToPDF(String filename){
		    if (filename.endsWith(".doc") || filename.endsWith(".txt") || filename.endsWith(".xls") 
		            || filename.endsWith(".ppt") || filename.endsWith(".docx"))
		    {
		        return true;
		    }
		    return false;
		}
	
}
