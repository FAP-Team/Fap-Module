package controllers.popups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import play.mvc.Util;

import tags.ComboItem;
import validation.CustomValidation;
import messages.Messages;
import models.CodigoExclusion;
import models.SolicitudGenerica;
import models.TipoCodigoExclusion;
import controllers.gen.popups.PopUpExclusionControllerGen;

public class PopUpExclusionController extends PopUpExclusionControllerGen {
	
	public static void index(String accion, Long idSolicitud, Long idCodigoExclusion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/Exclusion/PopUpExclusion.html");
		}

		SolicitudGenerica solicitud = PopUpExclusionController.getSolicitudGenerica(idSolicitud);

		CodigoExclusion codigoExclusion = null;
		if ("crear".equals(accion))
			codigoExclusion = PopUpExclusionController.getCodigoExclusion();
		else if (!"borrado".equals(accion))
			codigoExclusion = PopUpExclusionController.getCodigoExclusion(idSolicitud, idCodigoExclusion);

		log.info("Visitando página: " + "fap/Exclusion/PopUpExclusion.html");
		renderTemplate("fap/Exclusion/PopUpExclusion.html", accion, idSolicitud, idCodigoExclusion, solicitud, codigoExclusion);
	}
	
	public static Long crearLogica(Long idSolicitud, CodigoExclusion codigoExclusion) {
		checkAuthenticity();
		if (!permiso("crear")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		CodigoExclusion dbCodigoExclusion = PopUpExclusionController.getCodigoExclusion();
		SolicitudGenerica dbSolicitud = PopUpExclusionController.getSolicitudGenerica(idSolicitud);

		PopUpExclusionController.PopUpExclusionBindReferences(codigoExclusion);

		if (!Messages.hasErrors()) {
			CustomValidation.clearValidadas();
			CustomValidation.valid("codigoExclusion.tipoCodigo", codigoExclusion.tipoCodigo);
			CustomValidation.valid("codigoExclusion", codigoExclusion);
			for (CodigoExclusion codigoR: dbSolicitud.exclusion.codigos){
				if (codigoR.codigo.equals(codigoExclusion.codigo)){
					CustomValidation.error("El código de exclusión ya existe en este expediente", "codigoExclusion.tipoCodigo.descripcionCorta",codigoExclusion.tipoCodigo.descripcionCorta);
				}
			}
		}
		if (!Messages.hasErrors()) {
			PopUpExclusionController.PopUpExclusionValidateCopy("crear", dbCodigoExclusion, codigoExclusion);
		}
		if (!Messages.hasErrors()) {
			PopUpExclusionController.crearValidateRules(dbCodigoExclusion, codigoExclusion);
		}
		Long idCodigoExclusion = null;
		if (!Messages.hasErrors()) {
			dbCodigoExclusion.codigo = codigoExclusion.codigo;
			dbCodigoExclusion.save();
			idCodigoExclusion = dbCodigoExclusion.id;
			dbSolicitud.exclusion.codigos.add(dbCodigoExclusion);
			dbSolicitud.save();

			log.info("Acción Crear de página: " + "gen/popups/PopUpExclusion.html" + " , intentada con éxito");
		} else {
			log.info("Acción Crear de página: " + "gen/popups/PopUpExclusion.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		return idCodigoExclusion;
	}
	
	public static List<ComboItem> descripcionCorta(){
    	List<ComboItem> result = new ArrayList<ComboItem>();
    	List<TipoCodigoExclusion> listaTipos = TipoCodigoExclusion.find("select tipoCodigoExclusion from TipoCodigoExclusion tipoCodigoExclusion").fetch();
    	for (TipoCodigoExclusion codigo: listaTipos) {
    		result.add(new ComboItem(codigo.descripcionCorta, codigo.descripcionCorta));
    	}
    	return result;
    }

	@Util
	public static void PopUpExclusionValidateCopy(String accion, CodigoExclusion dbCodigoExclusion, CodigoExclusion codigoExclusion) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("codigoExclusion.tipoCodigo", codigoExclusion.tipoCodigo);
		CustomValidation.valid("codigoExclusion", codigoExclusion);
		CustomValidation.validValueFromTable("codigoExclusion.tipoCodigo.descripcionCorta", codigoExclusion.tipoCodigo.descripcionCorta);
		dbCodigoExclusion.codigo = codigoExclusion.codigo;
	}
}
