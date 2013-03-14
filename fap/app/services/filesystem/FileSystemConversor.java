package services.filesystem;

import java.io.File;

import models.SolicitudGenerica;

import org.h2.engine.User;

import reports.Report;
import services.ConversorService;

import com.mysql.jdbc.log.Log;

public class FileSystemConversor implements ConversorService{

	public FileSystemConversor() {
		// TODO Auto-generated constructor stub
	}
	
	public File convertToPdf(File inputFile){
//		File outputFile = null;
//		try {
//			outputFile = new Report("reports/conversor.html").header("reports/header.html").registroSize().renderTmpFile(new Object());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return outputFile;
		return null;
	}
}
