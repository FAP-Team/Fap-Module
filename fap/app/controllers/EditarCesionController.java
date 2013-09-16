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
import peticionCesion.PeticionAEAT;
import peticionCesion.PeticionATC;
import peticionCesion.PeticionBase;
import peticionCesion.PeticionINSSA008;
import peticionCesion.PeticionINSSR001;
import play.modules.guice.InjectSupport;
import play.mvc.Util;
import properties.FapProperties;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.async.GestorDocumentalServiceAsync;
import tags.ComboItem;
import tramitacion.TramiteBase;
import utils.BinaryResponse;
import utils.GestorDocumentalUtils;
import validation.CustomValidation;
import config.InjectorConfig;
import controllers.fap.PeticionFapController;
import controllers.fap.PresentacionFapController;
import controllers.gen.EditarCesionControllerGen;
import enumerado.fap.gen.EstadosPeticionEnum;
import enumerado.fap.gen.ListaCesionesEnum;
import enumerado.fap.gen.ListaEstadosEnum;


public class EditarCesionController extends EditarCesionControllerGen {
	//Inyeccion manual	
	static GestorDocumentalServiceAsync gestorDocumentalServiceAsync = InjectorConfig.getInjector().getInstance(GestorDocumentalServiceAsync.class);
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formCesionFichRespuesta(Long idPeticionCesiones, PeticionCesiones peticionCesiones, java.io.File subirArchivo, String tratarFich) {
		checkAuthenticity();
		if (!permisoFormCesionFichRespuesta("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción (FicheroRespuesta)");
		}
		PeticionCesiones dbPeticionCesiones = EditarCesionController.getPeticionCesiones(idPeticionCesiones);

		EditarCesionController.formCesionFichRespuestaBindReferences(peticionCesiones, subirArchivo);

		if (!Messages.hasErrors()) {
			EditarCesionController.formCesionFichRespuestaValidateCopy("editar", dbPeticionCesiones, peticionCesiones, subirArchivo);
		}

		if (!Messages.hasErrors()) {
			EditarCesionController.formCesionFichRespuestaValidateRules(dbPeticionCesiones, peticionCesiones, subirArchivo);
		}
		if (!Messages.hasErrors()) {
			subirFichero(dbPeticionCesiones); //Generacion del pdf de cada peticion/solicitud
			if ((!Messages.hasErrors()) && (tratarFich != null)) { //Si no hubo errores subiendo -> Cambio de estado
				dbPeticionCesiones.estado = EstadosPeticionEnum.respondida.name();
			}
			dbPeticionCesiones.save();
			log.info("Acción Editar de página: " + "gen/EditarCesion/EditarCesion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarCesion/EditarCesion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarCesionController.formCesionFichRespuestaRender(idPeticionCesiones);
	}

	
	public static void formCesionFichSubida(Long idPeticionCesiones, PeticionCesiones peticionCesiones, String btnGuardar) {
		checkAuthenticity();
		if (!permisoFormCesionFichSubida("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción (FicheroSubida)");
		}
		PeticionCesiones dbPeticionCesiones = EditarCesionController.getPeticionCesiones(idPeticionCesiones);
		EditarCesionController.formCesionFichSubidaBindReferences(peticionCesiones);

		if (!Messages.hasErrors()) {
			//Sube fichero
			EditarCesionController.formCesionFichSubidaValidateCopy("editar", dbPeticionCesiones, peticionCesiones);
		}

		if (!Messages.hasErrors()) {
			EditarCesionController.formCesionFichSubidaValidateRules(dbPeticionCesiones, peticionCesiones);
		}
		if (!Messages.hasErrors()) {
			if (btnGuardar != null) {
				EditarCesionController.btnGuardarFormCesion(idPeticionCesiones, peticionCesiones);
			}
			dbPeticionCesiones.save();
			log.info("Acción Editar de página: " + "gen/EditarCesion/EditarCesion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarCesion/EditarCesion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarCesionController.formCesionFichSubidaRender(idPeticionCesiones);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formCesionAplicarCambios(Long idPeticionCesiones, String cambiarFichero, String aplicarCambios) {
		checkAuthenticity();
		if (!permisoFormCesionAplicarCambios("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción (AplicarCambios)");
		}
		//Queremos subir otro fichero
		if (cambiarFichero != null) {
			PeticionCesiones peticionCesiones = EditarCesionController.getPeticionCesiones(idPeticionCesiones);
			cambiarFichero(peticionCesiones);
			peticionCesiones.save();
		}
		if (!Messages.hasErrors()) {
			EditarCesionController.formCesionAplicarCambiosValidateRules();
		}
		//Queremos aplicar los cambios desde fichero
		if (aplicarCambios != null){
			PeticionCesiones dbPeticionCesiones = EditarCesionController.getPeticionCesiones(idPeticionCesiones);
			tratarFich(dbPeticionCesiones);
			if (!Messages.hasErrors()) {
				dbPeticionCesiones.estado = EstadosPeticionEnum.asignada.name();
			}
			dbPeticionCesiones.save();
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "fap/EditarCesion/EditarCesion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "fap/EditarCesion/EditarCesion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarCesionController.formCesionAplicarCambiosRender(idPeticionCesiones);
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
				GestorDocumentalServiceAsync gestorDocumentalServiceAsync = config.InjectorConfig.getInjector().getInstance(GestorDocumentalServiceAsync.class);
				await(gestorDocumentalServiceAsync.saveDocumentoTemporal(dbPeticionCesiones.fichRespuesta, subirArchivo));
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
			await(gestorDocumentalServiceAsync.deleteDocumento(documento));
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
			BinaryResponse brp = await(gestorDocumentalServiceAsync.getDocumentoByUri(peticionCesiones.fichRespuesta.uri));
			FileOutputStream fichO = new FileOutputStream(fich);
			fichO.write(brp.getBytes());
			fichO.close();
		} catch (IOException e) {
			Messages.error("Error creando el fichero temporal");
		} catch (GestorDocumentalServiceException e) {
			Messages.error("Error del gestor documental ");
		}
		if (fich!=null){
			PeticionBase patc;
			try {
				patc = PeticionFapController.invoke(PeticionFapController.class, "getPeticionObject", peticionCesiones.tipo);
				patc.parsearPeticionBase(peticionCesiones, fich);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	//Métodos en el controlador manual 
	public static List<ComboItem> peticionCesiones_fichRespuesta_tipo() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		String tipo = getPeticionCesiones(Long.parseLong(params.get("idPeticionCesiones"))).tipo.toUpperCase();
		result.add(new ComboItem(FapProperties.get("fap.aed.tiposdocumentos.respuesta"+tipo)));
		return result;
	}
	
	public static void index(String accion, Long idPeticionCesiones) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción (Index)");
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
	public static void btnGuardarFormCesion(Long idPeticionCesiones, PeticionCesiones peticionCesiones) {
		//Sobreescribir este método para asignar una acción
		PeticionCesiones dbPeticionCesiones = EditarCesionController.getPeticionCesiones(idPeticionCesiones);
		dbPeticionCesiones.tipo = peticionCesiones.tipo;
		dbPeticionCesiones.estado = EstadosPeticionEnum.creada.name();
		dbPeticionCesiones.save();
		redirect("NuevaCesionController.index", "editar", idPeticionCesiones);
	}
	
	@Util
	public static void formCesionFichRespuestaRender(Long idPeticionCesiones) {
		if (!Messages.hasMessages()) {
			Messages.keep();
			redirect("EditarCesionController.index", "editar", idPeticionCesiones);
		}
		Messages.keep();
		redirect("EditarCesionController.index", "editar", idPeticionCesiones);
	}
	
	@Util
	public static void formCesionAplicarCambiosRender(Long idPeticionCesiones) {
		if (!Messages.hasErrors()) {
			Messages.keep();
			PeticionCesiones pt = getPeticionCesiones(idPeticionCesiones);
			//Si respondida -> Aplico cambios
			if (pt.estado.equals(EstadosPeticionEnum.asignada.name()))
				redirect("LeerCesionController.index", "leer", idPeticionCesiones);
			else{
				redirect("EditarCesionController.index", "editar", idPeticionCesiones);
			}
		}
		Messages.keep();
		redirect("EditarCesionController.index", "editar", idPeticionCesiones);
	}
	
	@Util
	public static void formCesionFichSubidaRender(Long idPeticionCesiones) {
		if (!Messages.hasMessages()) {
			Messages.keep();
			redirect("EditarCesionController.index", "editar", idPeticionCesiones);
		}
		Messages.keep();
		redirect("EditarCesionController.index", "editar", idPeticionCesiones);
	}
	
	
	@Util
	public static void btnVolver(Long idPeticionCesiones, PeticionCesiones peticionCesiones, java.io.File subirArchivo) {
		redirect("GenerarFichCesionController.index", "editar", idPeticionCesiones);
	}
}
