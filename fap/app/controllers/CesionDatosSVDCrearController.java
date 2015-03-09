package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import messages.Messages;
import models.ParametrosServicio;
import models.PeticionSVDFAP;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import play.mvc.Util;
import services.SVDService;
import utils.SVDUtils;
import config.InjectorConfig;
import controllers.gen.CesionDatosSVDCrearControllerGen;

public class CesionDatosSVDCrearController extends CesionDatosSVDCrearControllerGen {

	public static void tablatablaSolicitudesIdentidadAutorizadas() {

		boolean consentimientoLey = ParametrosServicio.find("select consentimientoLey from ParametrosServicio parametrosServicio where nombreServicio=?", "identidad").first();

		java.util.List<SolicitudGenerica> rows;

		//Si existe un consentimiento por Ley, devuelve todos los expedientes
		//cuyo estado sea distinto a "Borrador", "Desistido" y "Excluído"
		if (consentimientoLey)
			rows = SolicitudGenerica.find(	"select solicitud from SolicitudGenerica solicitud "+
											" where (solicitud.estado != ?) and (solicitud.estado != ?)" +
											" and (solicitud.estado != ?)",
											"borrador", "desistido", "excluido").fetch();

		//Si no existe un consentimiento por Ley, se comprueba para cada expediente
		//si el usuario ha dado su consentimiento para realizar la consulta
		else
			rows = SolicitudGenerica.find(	"select solicitud from SolicitudGenerica solicitud, "+
											"Cesion cesion, AutorizacionCesion autorizacionCesion " +
											"where (solicitud.cesion = cesion.autorizacionCesion) " +
											" and (autorizacionCesion = cesion.autorizacionCesion) " +
											" and (solicitud.estado != ?) and (solicitud.estado != ?)" +
											" and (solicitud.estado != ?) and (autorizacionCesion.identidad=?)",
											"borrador", "desistido", "excluido", true).fetch();


		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudGenerica> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estado", "estadoValue", "estadoUsuario", "solicitante.numeroId", "solicitante.nombreCompleto", "estadoPeticionSVD"));
	}


	//Crear petición de Identidad a partir de los expedientes autorizados seleccionados
	public static void crearpeticionIdentidad(Long id, List<Long> idsSeleccionados) {

		SVDService svdService = InjectorConfig.getInjector().getInstance(SVDService.class);

		PeticionSVDFAP peticion = new PeticionSVDFAP();
		List<SolicitudTransmisionSVDFAP> listaSolicitudesTransmision = new ArrayList<SolicitudTransmisionSVDFAP>();

		for (Long idExpediente: idsSeleccionados) {
			SolicitudTransmisionSVDFAP solicitudTransmision = SVDUtils.crearSolicitudTransmisionSVDFAP("identidad", idExpediente);
			listaSolicitudesTransmision.add(solicitudTransmision);
		}

		svdService.crearPeticion(peticion, listaSolicitudesTransmision, "identidad");

		crearRender("editar");
	}


	public static void tablatablaSolicitudesResidenciaAutorizadas() {

		boolean consentimientoLey = ParametrosServicio.find("select consentimientoLey from ParametrosServicio parametrosServicio where nombreServicio=?", "residencia").first();

		java.util.List<SolicitudGenerica> rows;

		//Si existe un consentimiento por Ley, devuelve todos los expedientes
		//cuyo estado sea distinto a "Borrador", "Desistido" y "Excluído"
		if (consentimientoLey)
			rows = SolicitudGenerica.find(	"select solicitud from SolicitudGenerica solicitud "+
											" where (solicitud.estado != ?) and (solicitud.estado != ?)" +
											" and (solicitud.estado != ?)",
											"borrador", "desistido", "excluido").fetch();

		//Si no existe un consentimiento por Ley, se comprueba para cada expediente
		//si el usuario ha dado su consentimiento para realizar la consulta
		else
			rows = SolicitudGenerica.find(	"select solicitud from SolicitudGenerica solicitud, "+
											"Cesion cesion, AutorizacionCesion autorizacionCesion " +
											"where (solicitud.cesion = cesion.autorizacionCesion) " +
											" and (autorizacionCesion = cesion.autorizacionCesion) " +
											" and (solicitud.estado != ?) and (solicitud.estado != ?)" +
											" and (solicitud.estado != ?) and (autorizacionCesion.residencia=?)",
											"borrador", "desistido", "excluido", true).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudGenerica> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estado", "estadoValue", "estadoUsuario", "solicitante.numeroId", "solicitante.nombreCompleto", "estadoPeticionSVD"));
	}

	//Crear petición de Residencia a partir de los expedientes autorizados seleccionados
	public static void crearpeticionResidencia(Long id, List<Long> idsSeleccionados) {

	}

	@Util
	public static void crearRender(String accion) {
		if (!Messages.hasMessages()) {

			Messages.ok("Petición creada correctamente");
			Messages.keep();

		}
		Messages.keep();
		redirect("CesionDatosSVDCrearController.index", "editar", getComboNombreServicioSVDFAP());
	}

}
