
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
import models.TiposCodigoRequerimiento;
import controllers.gen.popups.PopUpExclusionControllerGen;
			
public class PopUpExclusionController extends PopUpExclusionControllerGen {

    public static void crear(Long idSolicitud,CodigoExclusion codigoExclusion){
        checkAuthenticity();
        if(!permiso("create")){
            Messages.error("No tiene permisos suficientes para realizar la acción");
        }
        CodigoExclusion dbCodigoExclusion = new CodigoExclusion();
        SolicitudGenerica dbSolicitud = null;
        if(!Messages.hasErrors()){
        	dbSolicitud = getSolicitudGenerica(idSolicitud);
        }

        if(!Messages.hasErrors()){
			CustomValidation.clearValidadas();
			CustomValidation.valid("codigoExclusion.tipoCodigo", codigoExclusion.tipoCodigo);
			CustomValidation.valid("codigoExclusion", codigoExclusion);
			// A partir del codigo del tipo de código, hacemos lo demás.
			
			CustomValidation.validValueFromTable("codigoExclusion.tipoCodigo.descripcionCorta", codigoExclusion.tipoCodigo.descripcionCorta);
			
			for (CodigoExclusion codigoR: dbSolicitud.exclusion.codigos){
				if (codigoR.codigo.equals(codigoExclusion.codigo)){
					CustomValidation.error("El código de exclusión ya existe en este expediente", "codigoExclusion.tipoCodigo.descripcionCorta",codigoExclusion.tipoCodigo.descripcionCorta);
				}
			}
        }

        if(!Messages.hasErrors()){
			dbCodigoExclusion.codigo = codigoExclusion.codigo;
        	dbCodigoExclusion.save();
        	dbSolicitud.exclusion.codigos.add(dbCodigoExclusion);
        	dbSolicitud.save();
        }

        if(!Messages.hasErrors()){
            renderJSON(utils.RestResponse.ok("Registro creado correctamente"));
        }else{
            Messages.keep();
            abrir("crear",null,idSolicitud);
        }
    }
    
	public static void abrir(String accion,Long idCodigoExclusion,Long idSolicitud){
		CodigoExclusion codigoExclusion;
		if(accion.equals("crear")){
            codigoExclusion = new CodigoExclusion();
			
		}else{
		    codigoExclusion = getCodigoExclusion(idSolicitud, idCodigoExclusion);
		}

		if (!permiso(accion)){
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		renderArgs.put("controllerName", "PopUpExclusionControllerGen");
		renderTemplate("fap/Exclusion/PopUpExclusion.html",accion,idCodigoExclusion,codigoExclusion,idSolicitud);
	}
	
    public static List<ComboItem> descripcionCorta(){
    	List<ComboItem> result = new ArrayList<ComboItem>();
    	List<TipoCodigoExclusion> listaTipos = TipoCodigoExclusion.find("select tipoCodigoExclusion from TipoCodigoExclusion tipoCodigoExclusion").fetch();
    	for (TipoCodigoExclusion codigo: listaTipos) {
    		result.add(new ComboItem(codigo.descripcionCorta, codigo.descripcionCorta));
    	}
    	return result;
    }
}
		