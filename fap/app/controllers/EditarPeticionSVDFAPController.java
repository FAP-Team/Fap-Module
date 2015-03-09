package controllers;

import java.util.List;
import java.util.Map;

import models.SolicitudTransmisionSVDFAP;
import controllers.gen.EditarPeticionSVDFAPControllerGen;

public class EditarPeticionSVDFAPController extends EditarPeticionSVDFAPControllerGen {


	//TODO: Falta filtrar tablas para que aparezcan sólo las solicitudes de transmisión
	//de la petición correspondiente
	public static void tablatablaSolicitudesTransmisionIdentidad(Long idPeticion) {

		java.util.List<SolicitudTransmisionSVDFAP> rows = SolicitudTransmisionSVDFAP.find("select solicitudTransmisionSVDFAP from SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudTransmisionSVDFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<SolicitudTransmisionSVDFAP> response = new tables.TableRenderResponse<SolicitudTransmisionSVDFAP>(rowsFiltered, true, false, true, "adminOrGestor", "", "adminOrGestor", getAccion(), ids);

		renderJSON(response.toJSON("id", "nombreServicio", "datosEspecificos.solicitud.id", "fechaCreacion", "estado", "respuesta.datosGenericos.transmision.fechaGeneracion", "descargarPDF"));
	}

	public static void tablatablaSolicitudesTransmisionResidencia(Long idPeticion) {

		java.util.List<SolicitudTransmisionSVDFAP> rows = SolicitudTransmisionSVDFAP.find("select solicitudTransmisionSVDFAP from SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudTransmisionSVDFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<SolicitudTransmisionSVDFAP> response = new tables.TableRenderResponse<SolicitudTransmisionSVDFAP>(rowsFiltered, true, false, true, "adminOrGestor", "", "adminOrGestor", getAccion(), ids);

		renderJSON(response.toJSON("id", "nombreServicio", "datosEspecificos.solicitud.id", "fechaCreacion", "estado", "respuesta.datosGenericos.transmision.fechaGeneracion", "descargarPDF"));
	}
}
