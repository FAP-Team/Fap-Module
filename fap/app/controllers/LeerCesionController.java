package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import play.mvc.Util;
import services.GestorDocumentalService;
import services.async.GestorDocumentalServiceAsync;
import utils.BinaryResponse;
import messages.Messages;
import models.PeticionCesiones;
import config.InjectorConfig;
import controllers.gen.LeerCesionControllerGen;

public class LeerCesionController extends LeerCesionControllerGen {

	public static String descargarFichero(String uri){
		GestorDocumentalServiceAsync gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalServiceAsync.class);
	    try{
	    	String contenido = "";
	    	BinaryResponse brp = await(gestorDocumentalService.getDocumentoByUri(uri));
	    	File fich = null;
	    	if (brp.nombre.endsWith(".txt"))
	    		fich = File.createTempFile("tmp", ".txt");
	    	else
	    		fich = File.createTempFile("tmp", ".pdf");
	    	
	    	FileOutputStream fichO = new FileOutputStream(fich);
			fichO.write(brp.getBytes());
	    	
			FileReader fr = new FileReader (fich);
			BufferedReader br = new BufferedReader(fr);
			String linea = null;
			if (brp.nombre.endsWith(".txt")){
				contenido = brp.nombre+", ";
				while((linea=br.readLine())!=null){
					contenido += linea+"\n";
				}
			}
	    	fich.deleteOnExit();
	    	return contenido;
	    }
	    catch (Exception e) {
			e.printStackTrace();
		}
	    return null;
	}
	
	public static void index(String accion, Long idPeticionCesiones) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/LeerCesion/LeerCesion.html");
		}

		PeticionCesiones peticionCesiones = null;
		if ("crear".equals(accion)) {
			peticionCesiones = LeerCesionController.getPeticionCesiones();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				peticionCesiones.save();
				idPeticionCesiones = peticionCesiones.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			peticionCesiones = LeerCesionController.getPeticionCesiones(idPeticionCesiones);

		log.info("Visitando página: " + "gen/LeerCesion/LeerCesion.html");
		renderTemplate("fap/LeerCesion/LeerCesion.html", accion, idPeticionCesiones, peticionCesiones);
	}
	
}
