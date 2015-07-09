package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import play.mvc.Util;
import services.verificacionDatos.SVDService;
import services.verificacionDatos.SVDUtils;
import messages.Messages;
import models.ParametrosServicioSVDFAP;
import models.PeticionSVDFAP;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import config.InjectorConfig;
import controllers.gen.CesionDatosSVDCrearControllerGen;
import enumerado.fap.gen.NombreServicioSVDFAPEnum;

public class CesionDatosSVDCrearController extends CesionDatosSVDCrearControllerGen {

	public static void tablatablaSolicitudesIdentidadAutorizadas() {

		boolean consentimientoLey = ParametrosServicioSVDFAP.find("select consentimientoLey from ParametrosServicioSVDFAP parametrosServicio where nombreServicio=?", "identidad").first();

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

		try {
			PeticionSVDFAP peticion = new PeticionSVDFAP();
			List<SolicitudTransmisionSVDFAP> listaSolicitudesTransmision = new ArrayList<SolicitudTransmisionSVDFAP>();

			//Se comprueba que se haya seleccionado al menos un expediente
			if (idsSeleccionados != null) {
				for (Long idExpediente: idsSeleccionados) {
					SolicitudTransmisionSVDFAP solicitudTransmision = SVDUtils.crearSolicitudTransmisionSVDFAP(NombreServicioSVDFAPEnum.identidad.name(), idExpediente);
					listaSolicitudesTransmision.add(solicitudTransmision);
				}

				SVDUtils.crearPeticion(peticion, listaSolicitudesTransmision, NombreServicioSVDFAPEnum.identidad.name());

				for(SolicitudTransmisionSVDFAP solicitudTransmision: listaSolicitudesTransmision) {
					solicitudTransmision.solicitud.estadoPeticionSVD = "creada";
					solicitudTransmision.solicitud.save();
				}

				peticion.atributos.numElementos = listaSolicitudesTransmision.size();
				peticion.save();
			} else {
				Messages.error("Tiene que seleccionar al menos un expediente");
				play.Logger.error("Error: no se ha seleccionado ningún expediente");
			}
		} catch (Exception e) {
			Messages.error("Error creando la petición");
			play.Logger.error("Se ha producido un error al crear la petición");
		}

		CesionDatosSVDCrearController.crearRender("editar");
	}


	public static void tablatablaSolicitudesResidenciaAutorizadas() {

		boolean consentimientoLey = ParametrosServicioSVDFAP.find("select consentimientoLey from ParametrosServicioSVDFAP parametrosServicio where nombreServicio=?", "residencia").first();

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

		SVDService svdService = InjectorConfig.getInjector().getInstance(SVDService.class);

		try {
			PeticionSVDFAP peticion = new PeticionSVDFAP();
			List<SolicitudTransmisionSVDFAP> listaSolicitudesTransmision = new ArrayList<SolicitudTransmisionSVDFAP>();

			//Se comprueba que se haya seleccionado al menos un expediente
			if (idsSeleccionados != null) {
				for (Long idExpediente: idsSeleccionados) {
					SolicitudTransmisionSVDFAP solicitudTransmision = SVDUtils.crearSolicitudTransmisionSVDFAP(NombreServicioSVDFAPEnum.residencia.name(), idExpediente);
					listaSolicitudesTransmision.add(solicitudTransmision);
				}

				SVDUtils.crearPeticion(peticion, listaSolicitudesTransmision, NombreServicioSVDFAPEnum.residencia.name());

				for(SolicitudTransmisionSVDFAP solicitudTransmision: listaSolicitudesTransmision) {
					solicitudTransmision.solicitud.estadoPeticionSVD = "creada";
					solicitudTransmision.solicitud.save();
				}

				peticion.atributos.numElementos = listaSolicitudesTransmision.size();
				peticion.save();
			} else {
				Messages.error("Tiene que seleccionar al menos un expediente");
				play.Logger.error("Error: no se ha seleccionado ningún expediente");
			}
		} catch (Exception e) {
			Messages.error("Error creando la petición");
			play.Logger.error("Se ha producido un error al crear la petición");
		}

		CesionDatosSVDCrearController.crearRender("editar");
	}

	@Util
	public static void crearRender(String accion) {
		if (!Messages.hasMessages()) {
			Messages.keep();
			redirect("CesionDatosSVDListarController.index", "editar", getComboNombreServicioSVDFAP());
		}
		Messages.keep();
		redirect("CesionDatosSVDCrearController.index", "editar", getComboNombreServicioSVDFAP());
	}

}
