package controllers;

import play.*;
import play.mvc.*;
import play.db.jpa.Model;
import controllers.fap.*;
import validation.*;
import messages.Messages;
import messages.Messages.MessageType;
import utils.GestorDocumentalUtils;
import tables.TableRecord;
import models.*;
import tags.ReflectionUtils;
import security.Accion;
import platino.FirmaUtils;
import security.ResultadoPermiso;
import java.util.Arrays;
import properties.FapProperties;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import services.FirmaService;
import com.google.inject.Inject;
import controllers.gen.PaginaCancelarControllerGen;

public class PaginaCancelarController extends PaginaCancelarControllerGen {
	
		protected static Logger log = Logger.getLogger("Paginas");

		public static void index(String accion, Long idSolicitud) {
			if (accion == null)
				accion = getAccion();
			if (!permiso(accion)) {
				Messages.fatal("No tiene permisos suficientes para realizar esta acción");
				renderTemplate("fap/PaginaCancelar/PaginaCancelar.html");
			}

			Solicitud solicitud = null;
			if ("crear".equals(accion)) {
				solicitud = PaginaCancelarController.getSolicitud();
				if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

					solicitud.save();
					idSolicitud = solicitud.id;

					accion = "editar";
				}

			} else if (!"borrado".equals(accion))
				solicitud = PaginaCancelarController.getSolicitud(idSolicitud);

			log.info("Visitando página: " + "fap/PaginaCancelar/PaginaCancelar.html");
			renderTemplate("fap/PaginaCancelar/PaginaCancelar.html", accion, idSolicitud, solicitud);
		}

		@Util
		public static boolean permiso(String accion) {

			//Sobreescribir para incorporar permisos a mano
			return true;

		}

		@Util
		public static String getAccion() {

			return "editar";

		}

		@Util
		public static Solicitud getSolicitud(Long idSolicitud) {
			Solicitud solicitud = null;
			if (idSolicitud == null) {
				if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
					Messages.fatal("Falta parámetro idSolicitud");
			} else {
				solicitud = Solicitud.findById(idSolicitud);
				if (solicitud == null) {
					Messages.fatal("Error al recuperar Solicitud");
				}
			}
			return solicitud;
		}

		@Util
		public static Solicitud getSolicitud() {
			return new Solicitud();
		}

		@Util
		// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
		public static void guardarPCE(Long idSolicitud, Solicitud solicitud, String bGuardarPCE) {
			checkAuthenticity();
			if (!permisoGuardarPCE("editar")) {
				Messages.error("No tiene permisos suficientes para realizar la acción");
			}
			Solicitud dbSolicitud = PaginaCancelarController.getSolicitud(idSolicitud);

			PaginaCancelarController.guardarPCEBindReferences(solicitud);

			if (!Messages.hasErrors()) {

				PaginaCancelarController.guardarPCEValidateCopy("editar", dbSolicitud, solicitud);

			}

			if (!Messages.hasErrors()) {
				PaginaCancelarController.guardarPCEValidateRules(dbSolicitud, solicitud);
			}
			if (!Messages.hasErrors()) {
				dbSolicitud.save();
				log.info("Acción Editar de página: " + "fap/PaginaCancelar/PaginaCancelar.html" + " , intentada con éxito");
			} else
				log.info("Acción Editar de página: " + "fap/PaginaCancelar/PaginaCancelar.html" + " , intentada sin éxito (Problemas de Validación)");
			PaginaCancelarController.guardarPCERender(idSolicitud);
		}

		@Util
		public static void guardarPCERender(Long idSolicitud) {
			if (!Messages.hasMessages()) {
				Messages.ok("Página editada correctamente");
				Messages.keep();
				redirect("PaginaCancelarController.index", "editar", idSolicitud);
			}
			Messages.keep();
			redirect("PaginaCancelarController.index", "editar", idSolicitud);
		}
}
