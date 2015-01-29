package controllers;

import messages.Messages;
import messages.Messages.MessageType;
import models.Agente;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import play.mvc.Util;
import controllers.fap.AgenteController;
import controllers.gen.EditarSolicitudTransmisionSVDIdentidadControllerGen;

public class EditarSolicitudTransmisionSVDIdentidadController extends EditarSolicitudTransmisionSVDIdentidadControllerGen {

	//Se pasa tambien el id de la Solicitud Generica
	public static void index(String accion, Long idSolicitud, Long idSolicitudTransmisionSVDFAP) {

		SolicitudGenerica solicitud = EditarSolicitudTransmisionSVDIdentidadController.getSolicitud(idSolicitud);

		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html");
		}
		checkRedirigir();

		SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP = null;
		if ("crear".equals(accion)) {
			solicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDIdentidadController.getSolicitudTransmisionSVDFAP();

			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitudTransmisionSVDFAP.save();
				idSolicitudTransmisionSVDFAP = solicitudTransmisionSVDFAP.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			solicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDIdentidadController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html" + " Agente: " + logAgente);

		//Se pasa por el render la Solicitud Generica y la Solicitud de Transmision
		renderTemplate("fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html", accion, solicitud, solicitudTransmisionSVDFAP);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(Long idSolicitud, Long idSolicitudTransmisionSVDFAP, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		SolicitudTransmisionSVDFAP dbSolicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDIdentidadController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);

		EditarSolicitudTransmisionSVDIdentidadController.EditarSolicitudTransmisionSVDIdentidadBindReferences(solicitudTransmisionSVDFAP);

		if (!Messages.hasErrors()) {

			EditarSolicitudTransmisionSVDIdentidadController.EditarSolicitudTransmisionSVDIdentidadValidateCopy("editar", dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);

		}

		if (!Messages.hasErrors()) {
			EditarSolicitudTransmisionSVDIdentidadController.editarValidateRules(dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			dbSolicitudTransmisionSVDFAP.save();
			log.info("Acción Editar de página: " + "fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		EditarSolicitudTransmisionSVDIdentidadController.editarRender(idSolicitud, idSolicitudTransmisionSVDFAP);
	}

	public static void crear(Long idSolicitud, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {

		SolicitudGenerica solicitud = EditarSolicitudTransmisionSVDIdentidadController.getSolicitud(idSolicitud);
		solicitudTransmisionSVDFAP.solicitud = solicitud;

		Long idSolicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDIdentidadController.crearLogica(idSolicitud, solicitudTransmisionSVDFAP);
		EditarSolicitudTransmisionSVDIdentidadController.crearRender(idSolicitud, idSolicitudTransmisionSVDFAP);
	}

	@Util
	public static Long crearLogica(Long idSolicitud, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {
		checkAuthenticity();
		if (!permiso("crear")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudTransmisionSVDFAP dbSolicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDIdentidadController.getSolicitudTransmisionSVDFAP();

		EditarSolicitudTransmisionSVDIdentidadController.EditarSolicitudTransmisionSVDIdentidadBindReferences(solicitudTransmisionSVDFAP);

		if (!Messages.hasErrors()) {

			EditarSolicitudTransmisionSVDIdentidadController.EditarSolicitudTransmisionSVDIdentidadValidateCopy("crear", dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);

		}

		if (!Messages.hasErrors()) {
			EditarSolicitudTransmisionSVDIdentidadController.crearValidateRules(dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);
		}

		SolicitudGenerica solicitud = EditarSolicitudTransmisionSVDIdentidadController.getSolicitud(idSolicitud);
		Long idSolicitudTransmisionSVDFAP = null;
		Agente logAgente = AgenteController.getAgente();

		if (!Messages.hasErrors()) {

			dbSolicitudTransmisionSVDFAP.solicitud = solicitud;
			dbSolicitudTransmisionSVDFAP.nombreServicio = "identidad";

			dbSolicitudTransmisionSVDFAP.datosGenericos = solicitudTransmisionSVDFAP.datosGenericos;
			dbSolicitudTransmisionSVDFAP.datosGenericos.titular.documentacion = solicitud.solicitante.numeroId;
			dbSolicitudTransmisionSVDFAP.datosGenericos.titular.nombreCompleto = solicitud.solicitante.nombreCompleto;

			dbSolicitudTransmisionSVDFAP.datosGenericos.titular.tipoDocumentacion = solicitud.solicitante.fisica.nip.tipo;
			dbSolicitudTransmisionSVDFAP.datosGenericos.solicitante.idExpediente = solicitud.id.toString(); //Id expediente = Id solicitud?

			dbSolicitudTransmisionSVDFAP.datosEspecificos = solicitudTransmisionSVDFAP.datosEspecificos;

			dbSolicitudTransmisionSVDFAP.save();
			idSolicitudTransmisionSVDFAP = dbSolicitudTransmisionSVDFAP.id;

			log.info("Acción Crear de página: " + "fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html" + " , intentada con éxito" + " Agente: " + logAgente);
		} else {
			log.info("Acción Crear de página: " + "fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		}
		return idSolicitudTransmisionSVDFAP;
	}

	public static void borrar(Long idSolicitud, Long idSolicitudTransmisionSVDFAP) {
		checkAuthenticity();
		if (!permiso("borrar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudTransmisionSVDFAP dbSolicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDIdentidadController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);

		if (!Messages.hasErrors()) {
			EditarSolicitudTransmisionSVDIdentidadController.borrarValidateRules(dbSolicitudTransmisionSVDFAP);
		}

		if (!Messages.hasErrors()) {

		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {

			dbSolicitudTransmisionSVDFAP.delete();

			log.info("Acción Borrar de página: " + "fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html" + " , intentada con éxito" + " Agente: " + logAgente);
		} else {
			log.info("Acción Borrar de página: " + "fap/EditarSolicitudTransmisionSVDIdentidad/EditarSolicitudTransmisionSVDIdentidad.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		}
		EditarSolicitudTransmisionSVDIdentidadController.borrarRender(idSolicitud, idSolicitudTransmisionSVDFAP);
	}


	//Se modifica el render para que pase el id de la Solicitud Generica
	@Util
	public static void editarRender(Long idSolicitud, Long idSolicitudTransmisionSVDFAP) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("EditarSolicitudTransmisionSVDIdentidadController.index", "editar", idSolicitud, idSolicitudTransmisionSVDFAP);
		}
		Messages.keep();
		redirect("EditarSolicitudTransmisionSVDIdentidadController.index", "editar", idSolicitud, idSolicitudTransmisionSVDFAP);
	}

	@Util
	public static void crearRender(Long idSolicitud, Long idSolicitudTransmisionSVDFAP) {
		if (!Messages.hasMessages()) {

			Messages.ok("Página creada correctamente");
			Messages.keep();
			redirect("CesionDatosExpedienteSVDController.index", "editar", idSolicitud);

		}
		Messages.keep();
		redirect("EditarSolicitudTransmisionSVDIdentidadController.index", "crear", idSolicitud, idSolicitudTransmisionSVDFAP);
	}

	@Util
	public static void borrarRender(Long idSolicitud, Long idSolicitudTransmisionSVDFAP) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página borrada correctamente");
			Messages.keep();
			redirect("CesionDatosExpedienteSVDController.index", "editar", idSolicitud);
		}
		Messages.keep();
		redirect("EditarSolicitudTransmisionSVDIdentidadController.index", "borrar", idSolicitud, idSolicitudTransmisionSVDFAP);
	}


	//Obtiene solicitud Generica a partir del id
	@Util
	public static SolicitudGenerica getSolicitud(Long idSolicitud) {
		SolicitudGenerica solicitud = null;
		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		} else {
			solicitud = SolicitudGenerica.findById(idSolicitud);
			if (solicitud == null) {
				Messages.fatal("Error al recuperar SolicitudTransmisionSVDFAP");
			}
		}
		return solicitud;
	}

}
