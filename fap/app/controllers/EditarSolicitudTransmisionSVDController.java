package controllers;

import messages.Messages;
import models.Agente;
import models.DatosGenericosPeticionSVDFAP;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import models.TitularSVDFAP;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import play.mvc.Util;
import controllers.fap.AgenteController;
import controllers.gen.EditarSolicitudTransmisionSVDControllerGen;

public class EditarSolicitudTransmisionSVDController extends EditarSolicitudTransmisionSVDControllerGen {

	protected static Logger log = Logger.getLogger("Paginas");

	public static void index(String accion, Long idSolicitudTransmisionSVDFAP) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/EditarSolicitudTransmisionSVD/EditarSolicitudTransmisionSVD.html");
		}
		Long idSolicitud = null;
		SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP = null;
		if ("crear".equals(accion)) {
			solicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDController.getSolicitudTransmisionSVDFAP();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitudTransmisionSVDFAP.save();
				idSolicitudTransmisionSVDFAP = solicitudTransmisionSVDFAP.id;
				idSolicitud = solicitudTransmisionSVDFAP.solicitud.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			solicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/EditarSolicitudTransmisionSVD/EditarSolicitudTransmisionSVD.html" + " Agente: " + logAgente);
		renderTemplate("fap/EditarSolicitudTransmisionSVD/EditarSolicitudTransmisionSVD.html", accion, idSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP, idSolicitud);
	}

	@Util
	public static void editarRender(Long idSolicitudTransmisionSVDFAP, Long idSolicitud) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("EditarSolicitudTransmisionSVDController.index", "editar", idSolicitudTransmisionSVDFAP, idSolicitud);
		}
		Messages.keep();
		redirect("EditarSolicitudTransmisionSVDController.index", "editar", idSolicitudTransmisionSVDFAP, idSolicitud);
	}

	@Util
	public static void crearRender(Long idSolicitudTransmisionSVDFAP, Long idSolicitud) {
		if (!Messages.hasMessages()) {

			Messages.ok("Página creada correctamente");
			Messages.keep();
			redirect("EditarSolicitudTransmisionSVDController.index", "editar", idSolicitudTransmisionSVDFAP, idSolicitud);

		}
		Messages.keep();
		redirect("EditarSolicitudTransmisionSVDController.index", "crear", idSolicitudTransmisionSVDFAP, idSolicitud);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void editar(Long idSolicitudTransmisionSVDFAP, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {

		Long idSolicitud = null;

		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudTransmisionSVDFAP dbSolicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDController.getSolicitudTransmisionSVDFAP(idSolicitudTransmisionSVDFAP);
		idSolicitud = solicitudTransmisionSVDFAP.solicitud.id;

		EditarSolicitudTransmisionSVDController.EditarSolicitudTransmisionSVDBindReferences(solicitudTransmisionSVDFAP);

		if (!Messages.hasErrors()) {

			EditarSolicitudTransmisionSVDController.EditarSolicitudTransmisionSVDValidateCopy("editar", dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);

		}

		if (!Messages.hasErrors()) {
			EditarSolicitudTransmisionSVDController.editarValidateRules(dbSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			dbSolicitudTransmisionSVDFAP.save();
			log.info("Acción Editar de página: " + "fap/EditarSolicitudTransmisionSVD/EditarSolicitudTransmisionSVD.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "fap/EditarSolicitudTransmisionSVD/EditarSolicitudTransmisionSVD.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		EditarSolicitudTransmisionSVDController.editarRender(idSolicitudTransmisionSVDFAP, idSolicitud);
	}

	public static void crear(Long idSolicitudTransmisionSVDFAP, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {

		Long idSolicitud = null;
		if (idSolicitudTransmisionSVDFAP != null) {
			EditarSolicitudTransmisionSVDController.editar(idSolicitudTransmisionSVDFAP, solicitudTransmisionSVDFAP);
			idSolicitud = solicitudTransmisionSVDFAP.solicitud.id;

			solicitudTransmisionSVDFAP.fechaCreacion = new DateTime();
			solicitudTransmisionSVDFAP.estado = "creada";

			SolicitudGenerica solicitud = solicitudTransmisionSVDFAP.solicitud;
			TitularSVDFAP titular = new TitularSVDFAP();
			titular.documentacion = solicitud.solicitante.numeroId;
			titular.nombreCompleto = solicitud.solicitante.nombreCompleto;
			//falta nombre y apellidos divididos: nombre, apellido1 y apellido2
			titular.tipoDocumentacion = solicitud.solicitante.tipoDeDocumento;

			DatosGenericosPeticionSVDFAP datosGenericos = new DatosGenericosPeticionSVDFAP();
			datosGenericos.titular = titular;
			datosGenericos.solicitante.idExpediente = solicitud.id.toString(); //Id expediente = Id solicitud?

			solicitudTransmisionSVDFAP.datosGenericos = datosGenericos;
		}
		else {
			idSolicitudTransmisionSVDFAP = EditarSolicitudTransmisionSVDController.crearLogica(solicitudTransmisionSVDFAP);
			idSolicitud = solicitudTransmisionSVDFAP.solicitud.id;
			EditarSolicitudTransmisionSVDController.crearRender(idSolicitudTransmisionSVDFAP, idSolicitud);

		}
	}


}
