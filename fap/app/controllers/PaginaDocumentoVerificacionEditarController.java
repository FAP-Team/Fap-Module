package controllers;

import java.util.Arrays;
import java.util.Map;

import org.joda.time.DateTime;

import play.mvc.Util;

import validation.CustomValidation;
import messages.Messages;
import models.SolicitudGenerica;
import models.Verificacion;
import models.VerificacionDocumento;
import controllers.gen.PaginaDocumentoVerificacionEditarControllerGen;
import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;

public class PaginaDocumentoVerificacionEditarController extends PaginaDocumentoVerificacionEditarControllerGen {
	
	public static void editar(Long idSolicitud, Long idVerificacionDocumento, VerificacionDocumento verificacionDocumento) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		VerificacionDocumento dbVerificacionDocumento = PaginaDocumentoVerificacionEditarController.getVerificacionDocumento(idSolicitud, idVerificacionDocumento);

		PaginaDocumentoVerificacionEditarController.PaginaDocumentoVerificacionEditarBindReferences(verificacionDocumento);

		if (!Messages.hasErrors()){
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
			if ((dbVerificacionDocumento.estadoDocumentoVerificacion != null) && (dbVerificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noPresentado.name())) && (!verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noProcede.name())) && (!verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noPresentado.name()))){
				CustomValidation.error("El documento estaba en estado No Presentado. De ese estado sólo puede cambiar a No Procede", "verificacionDocumento.estadoDocumentoVerificacion", verificacionDocumento.estadoDocumentoVerificacion);			
			}
			if ((dbVerificacionDocumento.uriDocumento != null) && (verificacionDocumento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noPresentado.name()))){
				CustomValidation.error("El documento ha sido presentado por el solicitante y existe. No puede ponerse en estado No Presentado", "verificacionDocumento.estadoDocumentoVerificacion", verificacionDocumento.estadoDocumentoVerificacion);
			}
		}
		
		if (!Messages.hasErrors()) {
			PaginaDocumentoVerificacionEditarController.PaginaDocumentoVerificacionEditarValidateCopy("editar", dbVerificacionDocumento, verificacionDocumento);
		}

		if (!Messages.hasErrors()) {
			PaginaDocumentoVerificacionEditarController.editarValidateRules(dbVerificacionDocumento, verificacionDocumento);
		}
		if (!Messages.hasErrors()) {
			SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
			solicitud.verificacionEnCurso.fechaUltimaActualizacion=new DateTime();
			dbVerificacionDocumento.save();
			solicitud.save();
			log.info("Acción Editar de página: " + "gen/PaginaDocumentoVerificacionEditar/PaginaDocumentoVerificacionEditar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaDocumentoVerificacionEditar/PaginaDocumentoVerificacionEditar.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaDocumentoVerificacionEditarController.editarRender(idSolicitud, idVerificacionDocumento);
	}
	
	@Util
	public static void PaginaDocumentoVerificacionEditarValidateCopy(String accion, VerificacionDocumento dbVerificacionDocumento, VerificacionDocumento verificacionDocumento) {
		CustomValidation.clearValidadas();
		if (verificacionDocumento.uriDocumento != null) {
		}
		if (secure.checkGrafico("noEditable", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("verificacionDocumento", verificacionDocumento);
			dbVerificacionDocumento.descripcion = verificacionDocumento.descripcion;
			dbVerificacionDocumento.nombreTipoDocumento = verificacionDocumento.nombreTipoDocumento;
			dbVerificacionDocumento.uriTipoDocumento = verificacionDocumento.uriTipoDocumento;
			dbVerificacionDocumento.fechaPresentacion = verificacionDocumento.fechaPresentacion;

		}
		CustomValidation.valid("verificacionDocumento", verificacionDocumento);
		CustomValidation.required("verificacionDocumento.estadoDocumentoVerificacion", verificacionDocumento.estadoDocumentoVerificacion);
		CustomValidation.validValueFromTable("verificacionDocumento.estadoDocumentoVerificacion", verificacionDocumento.estadoDocumentoVerificacion);
		dbVerificacionDocumento.estadoDocumentoVerificacion = verificacionDocumento.estadoDocumentoVerificacion;
		if (Arrays.asList(new String[] { "noValido", "noPresentado" }).contains(dbVerificacionDocumento.estadoDocumentoVerificacion)) {
			dbVerificacionDocumento.motivoRequerimiento = verificacionDocumento.motivoRequerimiento;
		} else {
			dbVerificacionDocumento.motivoRequerimiento = "";
		}

	}

}
