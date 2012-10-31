package controllers.popups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import play.mvc.Util;

import models.Cesiones;
import models.Documento;

import tags.ComboItem;
import tramitacion.TramiteSolicitud;
import tramitacion.TramiteSolicitudFap;
import validation.CustomValidation;
import controllers.gen.popups.PopupListadoCesionesControllerGen;
import enumerado.fap.gen.ListaOrigenEnum;

public class PopupListadoCesionesController extends PopupListadoCesionesControllerGen {

	public static List<ComboItem> comboDoc(){
		List<ComboItem> result = new ArrayList<ComboItem>();
		java.util.List<Documento> rows = Documento.find("select documento from SolicitudGenerica solicitud join solicitud.documentacion.documentos documento").fetch();
		for (Documento doc : rows) {
			result.add(new ComboItem(doc.uri, "("+doc.fechaRegistro+")-"+doc.tipo));
		}
		return result;
	}

	@Util
	public static void PopupListadoCesionesValidateCopy(String accion, Cesiones dbCesiones, Cesiones cesiones) {
		CustomValidation.clearValidadas();
		if (secure.checkGrafico("adminOrGestor", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("cesiones", cesiones);
			CustomValidation.required("cesiones.tipo", cesiones.tipo);
			CustomValidation.validValueFromTable("cesiones.tipo", cesiones.tipo);
			dbCesiones.tipo = cesiones.tipo;
			CustomValidation.required("cesiones.fechaPeticion", cesiones.fechaPeticion);
			dbCesiones.fechaPeticion = cesiones.fechaPeticion;
			CustomValidation.required("cesiones.fechaValidez", cesiones.fechaValidez);
			dbCesiones.fechaValidez = cesiones.fechaValidez;
			CustomValidation.required("cesiones.estado", cesiones.estado);
			CustomValidation.validValueFromTable("cesiones.estado", cesiones.estado);
			dbCesiones.estado = cesiones.estado;
			CustomValidation.valid("cesiones.documento", cesiones.documento);
			CustomValidation.validValueFromTable("cesiones.documento.uri", cesiones.documento.uri);
			dbCesiones.documento.uri = cesiones.documento.uri;
			dbCesiones.documento = cesiones.documento;
			dbCesiones.origen = ListaOrigenEnum.manual.name();
		}

	}
	
}
