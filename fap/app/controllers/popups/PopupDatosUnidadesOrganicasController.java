package controllers.popups;

import play.mvc.Util;
import validation.CustomValidation;
import messages.Messages;
import models.ReturnUnidadOrganicaFap;
import controllers.gen.popups.PopupDatosUnidadesOrganicasControllerGen;

public class PopupDatosUnidadesOrganicasController extends PopupDatosUnidadesOrganicasControllerGen {

	@Util
	public static void PopupDatosUnidadesOrganicasValidateCopy(String accion, ReturnUnidadOrganicaFap dbReturnUnidadOrganicaFap, ReturnUnidadOrganicaFap returnUnidadOrganicaFap) {
		CustomValidation.clearValidadas();

		CustomValidation.valid("returnUnidadOrganicaFap", returnUnidadOrganicaFap);
		CustomValidation.required("returnUnidadOrganicaFap.codigo", returnUnidadOrganicaFap.codigo);
		dbReturnUnidadOrganicaFap.codigo = returnUnidadOrganicaFap.codigo;
		dbReturnUnidadOrganicaFap.codigoCompleto = returnUnidadOrganicaFap.codigoCompleto;
		dbReturnUnidadOrganicaFap.codigoBDOrganizacion = returnUnidadOrganicaFap.codigoBDOrganizacion;
		CustomValidation.required("returnUnidadOrganicaFap.descripcion", returnUnidadOrganicaFap.descripcion);
		dbReturnUnidadOrganicaFap.descripcion = returnUnidadOrganicaFap.descripcion;
		dbReturnUnidadOrganicaFap.esBaja = returnUnidadOrganicaFap.esBaja;
		dbReturnUnidadOrganicaFap.esReceptora = returnUnidadOrganicaFap.esReceptora;
		dbReturnUnidadOrganicaFap.codigoReceptora = returnUnidadOrganicaFap.codigoReceptora;
		
		if ("crear".equals(accion)) {
			ReturnUnidadOrganicaFap unidadOrganica = ReturnUnidadOrganicaFap.find("Select unidadOrganica from ReturnUnidadOrganicaFap unidadOrganica where unidadOrganica.codigo = ?", returnUnidadOrganicaFap.codigo).first();
		    
			if (unidadOrganica != null){
				Messages.error("Ya existe una Unidad Orgánica identificada con el código: "+returnUnidadOrganicaFap.codigo);
				return;
			}
		}

	}
}
