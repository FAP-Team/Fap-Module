package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.inject.Inject;

import messages.Messages;
import models.Documento;
import models.PeticionCesiones;
import play.modules.guice.InjectSupport;
import play.mvc.Util;
import properties.FapProperties;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import tags.ComboItem;
import utils.AEATUtils;
import utils.ATCUtils;
import utils.BinaryResponse;
import utils.GestorDocumentalUtils;
import utils.INSSUtils;
import validation.CustomValidation;
import config.InjectorConfig;
import controllers.gen.EditarCesionControllerGen;
import enumerado.fap.gen.EstadosPeticionEnum;
import enumerado.fap.gen.ListaCesionesEnum;
import enumerado.fap.gen.ListaEstadosEnum;


public class EditarCesionController extends EditarCesionControllerGen {
	//Inyeccion manual	
	static GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
	
	@Util
	 // Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formCesion(Long idPeticionCesiones, PeticionCesiones peticionCesiones, java.io.File subirArchivo, String btnGuardar, String tratarFich, String cambiarFichero, String aplicarCambios) {
		checkAuthenticity();
		if (!permisoFormCesion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		PeticionCesiones dbPeticionCesiones = EditarCesionController.getPeticionCesiones(idPeticionCesiones);

		EditarCesionController.formCesionBindReferences(peticionCesiones, subirArchivo);

		if ((!Messages.hasErrors()) &&(tratarFich != null)) {
			//Aqui se sube el fichero al Gestor Documental
			EditarCesionController.formCesionValidateCopy("editar", dbPeticionCesiones, peticionCesiones, subirArchivo);
		}

		if (!Messages.hasErrors()) {
			EditarCesionController.formCesionValidateRules(dbPeticionCesiones, peticionCesiones, subirArchivo);
		}
		if (!Messages.hasErrors()) {
			if (cambiarFichero != null){
				cambiarFichero(dbPeticionCesiones);
			}
			if (aplicarCambios != null){
				tratarFich(dbPeticionCesiones);
			}
			if (btnGuardar != null) {
				EditarCesionController.btnGuardarFormCesion(idPeticionCesiones, peticionCesiones, subirArchivo);
				EditarCesionController.formCesionRender(idPeticionCesiones);
			}
			if (tratarFich != null){
				subirFichero(dbPeticionCesiones); //Generacion del pdf de cada peticion/solicitud
			}
			if ((!Messages.hasErrors()) && (tratarFich != null)) { //Si no hubo errores subiendo -> Cambio de estado
				dbPeticionCesiones.estado = EstadosPeticionEnum.respondida.name();
			}
			if ((!Messages.hasErrors()) && (aplicarCambios != null)) {
				dbPeticionCesiones.estado = EstadosPeticionEnum.asignada.name();
			}
			dbPeticionCesiones.save();
			log.info("Acción Editar de página: " + "fap/EditarCesion/EditarCesion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "fap/EditarCesion/EditarCesion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarCesionController.formCesionRender(idPeticionCesiones);
	}

	@Util
	public static void formCesionValidateCopy(String accion, PeticionCesiones dbPeticionCesiones, PeticionCesiones peticionCesiones, java.io.File subirArchivo) {
		CustomValidation.clearValidadas();
		if (secure.checkGrafico("tipoAsignado", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("peticionCesiones", peticionCesiones);
			CustomValidation.validValueFromTable("peticionCesiones.tipo", peticionCesiones.tipo);
			dbPeticionCesiones.tipo = peticionCesiones.tipo;

		}
		CustomValidation.valid("peticionCesiones.fichRespuesta", peticionCesiones.fichRespuesta);
		CustomValidation.valid("peticionCesiones", peticionCesiones);
		dbPeticionCesiones.fichRespuesta.tipo = peticionCesiones.fichRespuesta.tipo;
		dbPeticionCesiones.fichRespuesta.descripcion = peticionCesiones.fichRespuesta.descripcion;
		

		if (subirArchivo == null)
			validation.addError("subirArchivo", "Archivo requerido");
		else if (subirArchivo.length() > properties.FapProperties.getLong("fap.file.maxsize"))
			validation.addError("subirArchivo", "Tamaño del archivo superior al máximo permitido (" + org.apache.commons.io.FileUtils.byteCountToDisplaySize(properties.FapProperties.getLong("fap.file.maxsize")) + ")");
		else {
			String extension = GestorDocumentalUtils.getExtension(subirArchivo);
			String mimeType = play.libs.MimeTypes.getMimeType(subirArchivo.getAbsolutePath());
			if (!utils.GestorDocumentalUtils.acceptExtension(extension))
				validation.addError("subirArchivo", "La extensión \"" + extension + "\" del documento a incorporar, no es válida. Compruebe los formatos de documentos aceptados.");
		}
		if (!validation.hasErrors()) {
			try {
				services.GestorDocumentalService gestorDocumentalService = config.InjectorConfig.getInjector().getInstance(services.GestorDocumentalService.class);
				gestorDocumentalService.saveDocumentoTemporal(dbPeticionCesiones.fichRespuesta, subirArchivo);
				dbPeticionCesiones.save();
				Messages.ok("El fichero de peticiones se ha subido correctamente");
			} catch (services.GestorDocumentalServiceException e) {
				play.Logger.error(e, "Error al subir el documento al Gestor Documental");
				validation.addError("", "Error al subir el documento al Gestor Documental");
			} catch (Exception e) {
				play.Logger.error(e, "Ex: Error al subir el documento al Gestor Documental");
				validation.addError("", "Error al subir el documento al Gestor Documental");
			}
		}
	}
	
	@Util
	public static void subirFichero (PeticionCesiones peticionCesiones) {
		peticionCesiones.respCesion.uri = peticionCesiones.fichRespuesta.uri;	
		peticionCesiones.save();
	}
	
	@Util
	private static void cambiarFichero(PeticionCesiones peticionCesiones) {
		peticionCesiones.estado = EstadosPeticionEnum.creada.name();
		Documento documento = peticionCesiones.fichRespuesta;
		try {
			gestorDocumentalService.deleteDocumento(documento);
			peticionCesiones.fichRespuesta.uri = null;
		} catch (GestorDocumentalServiceException e) {
			Messages.error("Error borrando el archivo del gestor documental");
		}
	}
	
	@Util
	public static void tratarFich(PeticionCesiones peticionCesiones) {
		peticionCesiones.respCesion.fechaActuacionGestor = new DateTime(); //Al actualizar

		//Generar el pdf
		File fich = null;
		try {
			fich = File.createTempFile("tmp", ".txt");
			BinaryResponse brp = gestorDocumentalService.getDocumentoByUri(peticionCesiones.fichRespuesta.uri);
			FileOutputStream fichO = new FileOutputStream(fich);
			fichO.write(brp.getBytes());
			fichO.close();
		} catch (IOException e) {
			Messages.error("Error creando el fichero temporal");
		} catch (GestorDocumentalServiceException e) {
			Messages.error("Error del gestor documental ");
		}
		
		if ((fich!=null) && (peticionCesiones.tipo.equals(ListaCesionesEnum.inssR001.name()))){
			INSSUtils.parsearINSSR001(peticionCesiones, fich);
		}else if ((fich!=null) && (peticionCesiones.tipo.equals(ListaCesionesEnum.aeat.name()))){
			AEATUtils.parsearAEAT(peticionCesiones, fich);
		}else if ((fich!=null) && (peticionCesiones.tipo.equals(ListaCesionesEnum.atc.name()))){
			ATCUtils.parsearATC(peticionCesiones, fich);
		}else if ((fich!=null) && (peticionCesiones.tipo.equals(ListaCesionesEnum.inssA008.name()))){
			INSSUtils.parsearINSSA008(peticionCesiones, fich);
		}else
			Messages.error("Error recuperando el fichero del Gestor Documental");
	}
	
	//Métodos en el controlador manual 
	public static List<ComboItem> peticionCesiones_fichRespuesta_tipo() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		result.add(new ComboItem(FapProperties.get("fap.aed.tiposdocumentos.respuestaINSSA008")));
		return result;
	}
	
	public static void index(String accion, Long idPeticionCesiones) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/EditarCesion/EditarCesion.html");
		}
		checkRedirigir();

		PeticionCesiones peticionCesiones = null;
		if ("crear".equals(accion)) {
			peticionCesiones = EditarCesionController.getPeticionCesiones();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				peticionCesiones.save();
				idPeticionCesiones = peticionCesiones.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			peticionCesiones = EditarCesionController.getPeticionCesiones(idPeticionCesiones);

		log.info("Visitando página: " + "fap/EditarCesion/EditarCesion.html");
		renderTemplate("fap/EditarCesion/EditarCesion.html", accion, idPeticionCesiones, peticionCesiones);
	}

	@Util
	public static void btnGuardarFormCesion(Long idPeticionCesiones, PeticionCesiones peticionCesiones, java.io.File subirArchivo) {
		//Sobreescribir este método para asignar una acción
		PeticionCesiones dbPeticionCesiones = EditarCesionController.getPeticionCesiones(idPeticionCesiones);
		dbPeticionCesiones.tipo = peticionCesiones.tipo;
		dbPeticionCesiones.estado = EstadosPeticionEnum.creada.name();
		dbPeticionCesiones.save();
		redirect("NuevaCesionController.index", "editar", idPeticionCesiones);
	}
	
}
