
package controllers.popups;

import java.util.Map;

import messages.Messages;
import models.SolicitudGenerica;
import models.VerificacionDocumento;
import play.mvc.Util;
import validation.CustomValidation;
import controllers.gen.popups.PopUpDocumentoVerificacionEditarControllerGen;
import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
			
public class PopUpDocumentoVerificacionEditarController extends PopUpDocumentoVerificacionEditarControllerGen {
	
	public static void editar(Long idSolicitud, Long idVerificacionDocumento,VerificacionDocumento verificacionDocumento) {
		checkAuthenticity();
		if (!permiso("update")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		VerificacionDocumento dbVerificacionDocumento = null;
		SolicitudGenerica solicitud = null;
		if (!Messages.hasErrors()) {
			dbVerificacionDocumento = getVerificacionDocumento(idSolicitud,idVerificacionDocumento);
			solicitud = getSolicitudGenerica(idSolicitud);
			// Comprobación de que está todo correcto dependiendo de como haya puesto el estado de la verificacion del documento
			if (verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.valido.name())
			   && ((dbVerificacionDocumento.codigosRequerimiento.size() != 0) || (!verificacionDocumento.motivoRequerimiento.isEmpty()))){
				Messages.error("Si el documento es Válido, no debe tener ningún código de requerimiento y/o motivo de requerimiento");
			} else if ((verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noValido.name()))
			   && ((dbVerificacionDocumento.codigosRequerimiento.size() == 0) && (verificacionDocumento.motivoRequerimiento.isEmpty()))){
				Messages.error("Si el documento es No Válido, debe existir al menos un código de requerimiento y/o motivo de requerimiento");
			} else if (verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noPresentado.name())
			   && (dbVerificacionDocumento.codigosRequerimiento.size() != 0)){
				Messages.error("Si el documento es No Presentado, sólo puede existir un motivo de requerimiento, pero no codigo/s de requerimiento/s");
			} else if ((verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noVerificado.name())
			   || (verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noProcede.name())))
			   && ((dbVerificacionDocumento.codigosRequerimiento.size() != 0) || (!verificacionDocumento.motivoRequerimiento.isEmpty()))){
				Messages.error("Si el documento es No Verificado o No Procede, no debe tener ningún código de requerimiento y/o motivo de requerimiento");	   
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
		