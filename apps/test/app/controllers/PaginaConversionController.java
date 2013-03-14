//package controllers;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//import messages.Messages;
//import models.Solicitud;
//import play.mvc.Util;
//import play.mvc.results.RenderBinary;
//import reports.Report;
//import services.ConversorService;
//import services.GestorDocumentalService;
//import services.GestorDocumentalServiceException;
//import utils.BinaryResponse;
//import config.InjectorConfig;
//import controllers.gen.PaginaConversionControllerGen;

//public class PaginaConversionController extends PaginaConversionControllerGen {
//	
//	static GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
//	
//	@Util
//	public static void botonPrueba(Long idSolicitud, Solicitud solicitud) {
//		
//		try {
//			File fich = File.createTempFile("tmp", ".doc");
//			BinaryResponse brp = gestorDocumentalService.getDocumentoByUri(solicitud.pruebaConversion.docPrueba.uri);
//			FileOutputStream fichO = new FileOutputStream(fich);
//			fichO.write(brp.getBytes());
//			fichO.close();
//
//			ConversorService conversorService = InjectorConfig.getInjector().getInstance(ConversorService.class);
//			File fichPDF = conversorService.convertToPdf(fich);
//			
//			renderBinary(fichPDF);
//
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (GestorDocumentalServiceException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	@Util
//	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
//	public static void formBoton(Long idSolicitud, String botonPrueba) {
//		checkAuthenticity();
//		if (!permisoFormBoton("editar")) {
//			Messages.error("No tiene permisos suficientes para realizar la acción");
//		}
//
//		if (!Messages.hasErrors()) {
//			Solicitud solicitud = Solicitud.findById(idSolicitud);
//			botonPrueba(idSolicitud, solicitud);
//		}
//
//		if (!Messages.hasErrors()) {
//			PaginaConversionController.formBotonValidateRules();
//		}
//		if (!Messages.hasErrors()) {
//
//			log.info("Acción Editar de página: " + "gen/PaginaPrueba/PaginaPrueba.html" + " , intentada con éxito");
//		} else
//			log.info("Acción Editar de página: " + "gen/PaginaPrueba/PaginaPrueba.html" + " , intentada sin éxito (Problemas de Validación)");
//		PaginaConversionController.formBotonRender(idSolicitud);
//	}	
//	
//}
