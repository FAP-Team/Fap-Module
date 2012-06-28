package controllers.popups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import play.mvc.Util;

import services.GestorDocumentalService;
import tags.ComboItem;
import validation.CustomValidation;
import messages.Messages;
import models.ExpedienteGenerico;
import models.Solicitud;
import models.SolicitudGenerica;
import config.InjectorConfig;
import controllers.gen.popups.PopupCrearSolicitudControllerGen;
import enumerado.fap.gen.EstadosSolicitudEnum;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;

public class PopupCrearSolicitudController extends PopupCrearSolicitudControllerGen {
	
	public static List<ComboItem> expediente() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		List<ExpedienteGenerico> expedientes = ExpedienteGenerico.findAll();
		for (ExpedienteGenerico exp: expedientes) {
			result.add(new ComboItem(exp.id, exp.idExpediente));
		}
		return result;
	}
	
	@Util
	public static void PopupCrearSolicitudValidateCopy(String accion, Solicitud dbSolicitud, Solicitud solicitud) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("solicitud", solicitud);
		CustomValidation.validValueFromTable("solicitud.expediente", solicitud.expediente);
		dbSolicitud.expediente = solicitud.expediente;
		CustomValidation.valid("solicitud.solicitante", solicitud.solicitante);
		CustomValidation.validValueFromTable("solicitud.solicitante.tipo", solicitud.solicitante.tipo);
		dbSolicitud.solicitante.tipo = solicitud.solicitante.tipo;
		if (Arrays.asList(new String[] { "fisica" }).contains(dbSolicitud.solicitante.tipo)) {
			CustomValidation.valid("solicitud.solicitante.fisica", solicitud.solicitante.fisica);
			dbSolicitud.solicitante.fisica.nombre = solicitud.solicitante.fisica.nombre;
			dbSolicitud.solicitante.fisica.primerApellido = solicitud.solicitante.fisica.primerApellido;
			dbSolicitud.solicitante.fisica.segundoApellido = solicitud.solicitante.fisica.segundoApellido;
			dbSolicitud.solicitante.fisica.nip = solicitud.solicitante.fisica.nip;
			dbSolicitud.solicitante.representado = solicitud.solicitante.representado;
			if ((solicitud.solicitante.representado != null) && (solicitud.solicitante.representado == true)) {
				CustomValidation.valid("solicitud.solicitante.representante", solicitud.solicitante.representante);
				CustomValidation.required("solicitud.solicitante.representante.tipo", solicitud.solicitante.representante.tipo);
				CustomValidation.validValueFromTable("solicitud.solicitante.representante.tipo", solicitud.solicitante.representante.tipo);
				dbSolicitud.solicitante.representante.tipo = solicitud.solicitante.representante.tipo;
				if (Arrays.asList(new String[] { "fisica" }).contains(dbSolicitud.solicitante.representante.tipo)) {
					CustomValidation.valid("solicitud.solicitante.representante.fisica", solicitud.solicitante.representante.fisica);
					CustomValidation.required("solicitud.solicitante.representante.fisica", solicitud.solicitante.representante.fisica);
					dbSolicitud.solicitante.representante.fisica.nombre = solicitud.solicitante.representante.fisica.nombre;
					dbSolicitud.solicitante.representante.fisica.primerApellido = solicitud.solicitante.representante.fisica.primerApellido;
					dbSolicitud.solicitante.representante.fisica.segundoApellido = solicitud.solicitante.representante.fisica.segundoApellido;
					dbSolicitud.solicitante.representante.fisica.nip = solicitud.solicitante.representante.fisica.nip;
				}
				if (Arrays.asList(new String[] { "juridica" }).contains(dbSolicitud.solicitante.representante.tipo)) {
					CustomValidation.valid("solicitud.solicitante.representante.juridica", solicitud.solicitante.representante.juridica);
					CustomValidation.required("solicitud.solicitante.representante.juridica", solicitud.solicitante.representante.juridica);
					dbSolicitud.solicitante.representante.juridica.cif = solicitud.solicitante.representante.juridica.cif;
					dbSolicitud.solicitante.representante.juridica.entidad = solicitud.solicitante.representante.juridica.entidad;
				}
			}
		}
		if (Arrays.asList(new String[] { "juridica" }).contains(dbSolicitud.solicitante.tipo)) {
			CustomValidation.valid("solicitud.solicitante.juridica", solicitud.solicitante.juridica);
			dbSolicitud.solicitante.juridica.cif = solicitud.solicitante.juridica.cif;
			dbSolicitud.solicitante.juridica.entidad = solicitud.solicitante.juridica.entidad;
		}
		
		// En la creación, podemos el estado de la solicitud como 'borrador'
		dbSolicitud.estado = EstadosSolicitudEnum.borrador.name();
	}

	
	@Util
	public static Long crearLogica(Solicitud solicitud) {
		checkAuthenticity();
		if (!permiso("crear")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		Solicitud dbSolicitud = PopupCrearSolicitudController.getSolicitud();

		PopupCrearSolicitudController.PopupCrearSolicitudBindReferences(solicitud);

		if (!Messages.hasErrors()) {

			PopupCrearSolicitudController.PopupCrearSolicitudValidateCopy("crear", dbSolicitud, solicitud);

		}

		if (!Messages.hasErrors()) {
			PopupCrearSolicitudController.crearValidateRules(dbSolicitud, solicitud);
		}
		Long idSolicitud = null;
		if (!Messages.hasErrors()) {

			dbSolicitud.save();
			idSolicitud = dbSolicitud.id;
			
			// Si ha seleccionado un expediente, lo relacionamos con esta solicitud
			if ( (solicitud.expediente != null) || !(solicitud.expediente.isEmpty()) ) {
				ExpedienteGenerico expediente = ExpedienteGenerico.find("select expediente from ExpedienteGenerico expediente where id = " + solicitud.expediente).first();
				expediente.solicitud.add(dbSolicitud);
				expediente.save();
			}

			log.info("Acción Crear de página: " + "gen/popups/PopupCrearSolicitud.html" + " , intentada con éxito");
		} else {
			log.info("Acción Crear de página: " + "gen/popups/PopupCrearSolicitud.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		return idSolicitud;
	}
}
