
package controllers.popups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import messages.Messages;
import models.CodigoRequerimiento;
import models.SolicitudGenerica;
import models.TiposCodigoRequerimiento;
import models.VerificacionDocumento;
import play.mvc.Util;
import tags.ComboItem;
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
			} 
			if ((verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noValido.name()))
			   && ((dbVerificacionDocumento.codigosRequerimiento.size() == 0) && (verificacionDocumento.motivoRequerimiento.isEmpty()))){
				CustomValidation.error("Con el estado No Válido, debe existir algún código o motivo de requerimiento", "verificacionDocumento.motivoRequerimiento", verificacionDocumento.motivoRequerimiento);
			}
			if (verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noPresentado.name())
			   && (dbVerificacionDocumento.codigosRequerimiento.size() != 0)){
				CustomValidation.error("Con el estado No Presentado, no puede existir ningun código de requerimiento, sólo motivos, vuelva al estado anterior y elimine los códigos de requerimiento", "dbVerificacionDocumento.codigosRequerimiento", dbVerificacionDocumento.codigosRequerimiento);
			}
			if ((verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noVerificado.name())
			   || (verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noProcede.name())))){
				if (dbVerificacionDocumento.codigosRequerimiento.size() != 0)
				   CustomValidation.error("Con el estado No Verificado o No Procede, no puede existir ningun código de requerimiento, vuelva al estado anterior y elimine los codigos de requerimiento", "dbVerificacionDocumento.codigosRequerimiento", dbVerificacionDocumento.codigosRequerimiento);
				if (!verificacionDocumento.motivoRequerimiento.isEmpty())
				   CustomValidation.error("Con el estado No Verificado o No Procede, no puede existir motivo de requerimiento, vuelva al estado anterior y elimine el motivo de requerimiento", "verificacionDocumento.motivoRequerimiento", verificacionDocumento.motivoRequerimiento);
			}
			if ((dbVerificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noPresentado.name())) && (!verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noProcede.name())) && (!verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noPresentado.name()))){
				CustomValidation.error("El documento estaba en estado No Presentado. De ese estado sólo puede cambiar a No Procede", "verificacionDocumento.estadoDocumentoVerificacion", verificacionDocumento.estadoDocumentoVerificacion);			
			}
			if ((dbVerificacionDocumento.uriDocumento != null) && (verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noPresentado.name()))){
				CustomValidation.error("El documento ha sido presentado por el solicitante y existe. No puede ponerse en estado No Presentado", "verificacionDocumento.estadoDocumentoVerificacion", verificacionDocumento.estadoDocumentoVerificacion);
			}
		}
		if (!Messages.hasErrors()) {
			PopUpDocumentoVerificacionEditarValidateCopy(dbVerificacionDocumento, verificacionDocumento);
		}

		if (!Messages.hasErrors()) {
			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			dbSolicitud.verificacion.fechaUltimaActualizacion = new DateTime();
			dbVerificacionDocumento.save();
			dbSolicitud.save();
		}

		if (!Messages.hasErrors()) {
			renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
		} else {
			Messages.keep();
			abrir("editar", idVerificacionDocumento, idSolicitud);
		}

	}

}
		