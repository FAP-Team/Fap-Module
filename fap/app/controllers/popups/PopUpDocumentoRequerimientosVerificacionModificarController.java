
package controllers.popups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ivy.util.Message;

import play.mvc.Scope.Params;
import play.mvc.Util;

import messages.Messages;
import models.CodigoRequerimiento;
import models.SolicitudGenerica;
import models.TiposCodigoRequerimiento;
import models.Verificacion;
import models.VerificacionDocumento;
import tags.ComboItem;
import validation.CustomValidation;
import aed.TiposDocumentosClient;
import controllers.gen.popups.PopUpDocumentoRequerimientosVerificacionModificarControllerGen;
			
public class PopUpDocumentoRequerimientosVerificacionModificarController extends PopUpDocumentoRequerimientosVerificacionModificarControllerGen {
	
	public static void abrir(String accion,Long idCodigoRequerimiento,Long idVerificacionDocumento){
		CodigoRequerimiento codigoRequerimiento;
		if(accion.equals("crear")){
            codigoRequerimiento = new CodigoRequerimiento();
			
		}else{
		    codigoRequerimiento = getCodigoRequerimiento(idVerificacionDocumento, idCodigoRequerimiento);
		}

		if (!permiso(accion)){
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		renderArgs.put("controllerName", "PopUpDocumentoRequerimientosVerificacionModificarControllerGen");
		renderTemplate("fap/Verificacion/PopUpDocumentoRequerimientosVerificacionModificar.html",accion,idCodigoRequerimiento,codigoRequerimiento,idVerificacionDocumento);
	}
	
	public static List<ComboItem> codigo() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		Map <String, Long> parametrosUrl = (Map<String, Long>)tags.TagMapStack.top("idParams");
		VerificacionDocumento doc = VerificacionDocumento.findById(parametrosUrl.get("idVerificacionDocumento"));
		SolicitudGenerica sol = null;
		if (parametrosUrl.get("idSolicitud") == null){ // Fallo la validacion y es necesario recuperar la solicitud a traves del idVerificacionDocumento, ya que no está en los parametros de la URL dentro de un popup al darle al boton Guardar
			sol = SolicitudGenerica.find("select sol from SolicitudGenerica sol join sol.verificacion.documentos vdoc where vdoc.id=?", doc.id).first();
		} else {
			sol = SolicitudGenerica.findById(parametrosUrl.get("idSolicitud"));
		}
		List <TiposCodigoRequerimiento> tiposCodReq = TiposCodigoRequerimiento.find("select tcr from TiposCodigoRequerimiento tcr where tcr.uriTipoDocumento=? and tcr.uriTramite=?", doc.uriTipoDocumento, sol.verificacion.uriTramite).fetch();
		List <CodigoRequerimiento> codigosRequerimiento = utils.ModelUtils.getListCodigoRequerimientoFromTiposCodigoRequerimiento(tiposCodReq);
		for (CodigoRequerimiento codigo: codigosRequerimiento){
			result.add(new ComboItem(codigo.codigo, codigo.codigo));
		}
		return result;
	}
	
	public static void crear(Long idVerificacionDocumento, CodigoRequerimiento codigoRequerimiento) {
		checkAuthenticity();
		if (!permiso("create")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		CodigoRequerimiento dbCodigoRequerimiento = new CodigoRequerimiento();

		VerificacionDocumento dbVerificacionDocumento = null;
		if (!Messages.hasErrors()) {
			dbVerificacionDocumento = getVerificacionDocumento(idVerificacionDocumento);
		}

		if (!Messages.hasErrors()) {
			PopUpDocumentoRequerimientosVerificacionModificarValidateCopy(dbCodigoRequerimiento, codigoRequerimiento);
		}

		if (!Messages.hasErrors()) {
			Verificacion verificacion = Verificacion.find("select verificacion from Verificacion verificacion inner join verificacion.documentos vDoc where vDoc.id=?", idVerificacionDocumento).first();
			TiposCodigoRequerimiento tipoCodReq = TiposCodigoRequerimiento.find("select tipoCodReq from TiposCodigoRequerimiento tipoCodReq where (tipoCodReq.codigo=? and tipoCodReq.uriTramite=? and tipoCodReq.uriTipoDocumento=?)", codigoRequerimiento.codigo, verificacion.uriTramite, dbVerificacionDocumento.uriTipoDocumento).first();
			dbCodigoRequerimiento.descripcion = tipoCodReq.descripcion;
			dbCodigoRequerimiento.descripcionCorta = tipoCodReq.descripcionCorta;
			dbCodigoRequerimiento.save();
			dbVerificacionDocumento.codigosRequerimiento.add(dbCodigoRequerimiento);
			dbVerificacionDocumento.save();

		}

		if (!Messages.hasErrors()) {
			renderJSON(utils.RestResponse.ok("Registro creado correctamente"));
		} else {
			Messages.keep();
			abrir("crear", null, idVerificacionDocumento);
		}
	}

	public static void editar(Long idVerificacionDocumento,Long idCodigoRequerimiento, CodigoRequerimiento codigoRequerimiento) {
		checkAuthenticity();
		if (!permiso("update")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		CodigoRequerimiento dbCodigoRequerimiento = null;
		VerificacionDocumento verificacionDocumento = null;
		if (!Messages.hasErrors()) {
			dbCodigoRequerimiento = getCodigoRequerimiento(idVerificacionDocumento, idCodigoRequerimiento);
			verificacionDocumento = getVerificacionDocumento(idVerificacionDocumento);
		}

		if (!Messages.hasErrors()) {
			PopUpDocumentoRequerimientosVerificacionModificarValidateCopy(dbCodigoRequerimiento, codigoRequerimiento);
		}

		if (!Messages.hasErrors()) {
			Verificacion verificacion = Verificacion.find("select verificacion from Verificacion verificacion inner join verificacion.documentos vDoc where vDoc.id=?", idVerificacionDocumento).first();
			TiposCodigoRequerimiento tipoCodReq = TiposCodigoRequerimiento.find("select tipoCodReq from TiposCodigoRequerimiento tipoCodReq where (tipoCodReq.codigo=? and tipoCodReq.uriTramite=? and tipoCodReq.uriTipoDocumento=?)", codigoRequerimiento.codigo, verificacion.uriTramite, verificacionDocumento.uriTipoDocumento).first();
			dbCodigoRequerimiento.descripcion = tipoCodReq.descripcion;
			dbCodigoRequerimiento.descripcionCorta = tipoCodReq.descripcionCorta;
			dbCodigoRequerimiento.save();
		}

		if (!Messages.hasErrors()) {
			renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
		} else {
			Messages.keep();
			abrir("editar", idCodigoRequerimiento, idVerificacionDocumento);
		}

	}
	
	@Util
	protected static void PopUpDocumentoRequerimientosVerificacionModificarValidateCopy(CodigoRequerimiento dbCodigoRequerimiento, CodigoRequerimiento codigoRequerimiento) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("codigoRequerimiento", codigoRequerimiento);
		CustomValidation.validValueFromTable("codigoRequerimiento.codigo",codigoRequerimiento.codigo);
		CustomValidation.required("codigoRequerimiento.codigo",codigoRequerimiento.codigo);
		// Comprobar que no se introduzca ningun codigo Requerimiento que ya esté repetido
		Map <String, Long> parametrosUrl = (Map<String, Long>)tags.TagMapStack.top("idParams");
		VerificacionDocumento doc = VerificacionDocumento.findById(parametrosUrl.get("idVerificacionDocumento"));
		for (CodigoRequerimiento codigoR: doc.codigosRequerimiento){
			if (codigoR.codigo.equals(codigoRequerimiento.codigo)){
				CustomValidation.error("El código de requerimiento ya existe en este documento", "codigoRequerimiento.codigo",codigoRequerimiento.codigo);
			}
		}
		dbCodigoRequerimiento.codigo = codigoRequerimiento.codigo;
	}
	
}
		