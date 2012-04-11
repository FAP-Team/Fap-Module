
package controllers.popups;

import java.util.Map;

import messages.Messages;
import models.SolicitudGenerica;
import models.VerificacionDocumento;
import play.mvc.Util;
import validation.CustomError;
import validation.CustomValidation;
import controllers.gen.popups.PopUpDocumentoVerificacionEditarControllerGen;
import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
			
public class PopUpDocumentoVerificacionEditarController extends PopUpDocumentoVerificacionEditarControllerGen {
	
	public static void editar(Long idSolicitud, Long idVerificacionDocumento,VerificacionDocumento verificacionDocumento) {
		checkAuthenticity();
		if (!permiso("update")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		VerificacionDocumento dbVerificacionDocumento = getVerificacionDocumento(idSolicitud,idVerificacionDocumento);
		if (!Messages.hasErrors()) {
			// Comprobación de que está todo correcto dependiendo de como haya puesto el estado de la verificacion del documento
			if (verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.valido.name())){
			   if (dbVerificacionDocumento.codigosRequerimiento.size() != 0)
				   CustomValidation.error("Con el estado Válido, no puede existir ningun código de requerimiento, vuelva al estado anterior y elimine los codigos de requerimiento", "dbVerificacionDocumento.codigosRequerimiento", dbVerificacionDocumento.codigosRequerimiento);
			   if (!verificacionDocumento.motivoRequerimiento.isEmpty())
				   CustomValidation.error("Con el estado Válido, no puede existir motivo de requerimiento, vuelva al estado anterior y elimine el motivo de requerimiento", "verificacionDocumento.motivoRequerimiento", verificacionDocumento.motivoRequerimiento);
			} else if ((verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noValido.name()))
			   && ((dbVerificacionDocumento.codigosRequerimiento.size() == 0) && (verificacionDocumento.motivoRequerimiento.isEmpty()))){
				CustomValidation.error("Con el estado No Válido, debe existir algún código o motivo de requerimiento", "verificacionDocumento.motivoRequerimiento", verificacionDocumento.motivoRequerimiento);
			} else if (verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noPresentado.name())
			   && (dbVerificacionDocumento.codigosRequerimiento.size() != 0)){
				CustomValidation.error("Con el estado No Presentado, no puede existir ningun código de requerimiento, sólo motivos, vuelva al estado anterior y elimine los códigos de requerimiento", "dbVerificacionDocumento.codigosRequerimiento", dbVerificacionDocumento.codigosRequerimiento);
			} else if ((verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noVerificado.name())
			   || (verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noProcede.name())))){
				if (dbVerificacionDocumento.codigosRequerimiento.size() != 0)
				   CustomValidation.error("Con el estado No Verificado o No Procede, no puede existir ningun código de requerimiento, vuelva al estado anterior y elimine los codigos de requerimiento", "dbVerificacionDocumento.codigosRequerimiento", dbVerificacionDocumento.codigosRequerimiento);
				if (!verificacionDocumento.motivoRequerimiento.isEmpty())
				   CustomValidation.error("Con el estado No Verificado o No Procede, no puede existir motivo de requerimiento, vuelva al estado anterior y elimine el motivo de requerimiento", "verificacionDocumento.motivoRequerimiento", verificacionDocumento.motivoRequerimiento);
			}
		}
		if (!Messages.hasErrors()) {
			PopUpDocumentoVerificacionEditarValidateCopy(dbVerificacionDocumento, verificacionDocumento);
		}

		if (!Messages.hasErrors()) {
			dbVerificacionDocumento.save();
		}

		if (!Messages.hasErrors()) {
			renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
		} else {
			Messages.keep();
			abrir("editar", idVerificacionDocumento, idSolicitud);
		}

	}

}
		