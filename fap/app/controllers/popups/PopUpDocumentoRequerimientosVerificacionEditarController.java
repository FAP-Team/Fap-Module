package controllers.popups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import messages.Messages;
import models.CodigoRequerimiento;
import models.SolicitudGenerica;
import models.TiposCodigoRequerimiento;
import models.Verificacion;
import models.VerificacionDocumento;

import tags.ComboItem;

import controllers.gen.popups.PopUpDocumentoRequerimientosVerificacionEditarControllerGen;

public class PopUpDocumentoRequerimientosVerificacionEditarController extends PopUpDocumentoRequerimientosVerificacionEditarControllerGen {
	
	public static void index(String accion, Long idVerificacionDocumento, Long idCodigoRequerimiento) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("gen/popups/PopUpDocumentoRequerimientosVerificacionEditar.html");
		}

		VerificacionDocumento verificacionDocumento = PopUpDocumentoRequerimientosVerificacionEditarController.getVerificacionDocumento(idVerificacionDocumento);

		CodigoRequerimiento codigoRequerimiento = null;
		if ("crear".equals(accion))
			codigoRequerimiento = PopUpDocumentoRequerimientosVerificacionEditarController.getCodigoRequerimiento();
		else if (!"borrado".equals(accion))
			codigoRequerimiento = PopUpDocumentoRequerimientosVerificacionEditarController.getCodigoRequerimiento(idVerificacionDocumento, idCodigoRequerimiento);

		log.info("Visitando página: " + "gen/popups/PopUpDocumentoRequerimientosVerificacionEditar.html");
		renderTemplate("fap/Verificacion/PopUpDocumentoRequerimientosVerificacionModificar.html", accion, idVerificacionDocumento, idCodigoRequerimiento, verificacionDocumento, codigoRequerimiento);
	}
	
	public static List<ComboItem> descripcionCorta() {
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
			result.add(new ComboItem(codigo.descripcionCorta, codigo.descripcionCorta));
		}
		return result;
	}
	
	public static void editar(Long idVerificacionDocumento, Long idCodigoRequerimiento, CodigoRequerimiento codigoRequerimiento) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		CodigoRequerimiento dbCodigoRequerimiento = PopUpDocumentoRequerimientosVerificacionEditarController.getCodigoRequerimiento(idVerificacionDocumento, idCodigoRequerimiento);
		VerificacionDocumento verificacionDocumento = getVerificacionDocumento(idVerificacionDocumento);
		PopUpDocumentoRequerimientosVerificacionEditarController.PopUpDocumentoRequerimientosVerificacionEditarBindReferences(codigoRequerimiento);

		if (!Messages.hasErrors()) {
			PopUpDocumentoRequerimientosVerificacionEditarController.PopUpDocumentoRequerimientosVerificacionEditarValidateCopy("editar", dbCodigoRequerimiento, codigoRequerimiento);
		}

		if (!Messages.hasErrors()) {
			PopUpDocumentoRequerimientosVerificacionEditarController.editarValidateRules(dbCodigoRequerimiento, codigoRequerimiento);
		}
		if (!Messages.hasErrors()) {
			Verificacion verificacion = Verificacion.find("select verificacion from Verificacion verificacion inner join verificacion.documentos vDoc where vDoc.id=?", idVerificacionDocumento).first();
			TiposCodigoRequerimiento tipoCodReq = TiposCodigoRequerimiento.find("select tipoCodReq from TiposCodigoRequerimiento tipoCodReq where (tipoCodReq.descripcionCorta=? and tipoCodReq.uriTramite=? and tipoCodReq.uriTipoDocumento=?)", codigoRequerimiento.descripcionCorta, verificacion.uriTramite, verificacionDocumento.uriTipoDocumento).first();
			dbCodigoRequerimiento.descripcion = tipoCodReq.descripcion;
			dbCodigoRequerimiento.codigo = tipoCodReq.codigo;
			dbCodigoRequerimiento.save();
			log.info("Acción Editar de página: " + "gen/popups/PopUpDocumentoRequerimientosVerificacionEditar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/popups/PopUpDocumentoRequerimientosVerificacionEditar.html" + " , intentada sin éxito (Problemas de Validación)");
		PopUpDocumentoRequerimientosVerificacionEditarController.editarRender(idVerificacionDocumento, idCodigoRequerimiento);
	}

	public static Long crearLogica(Long idVerificacionDocumento, CodigoRequerimiento codigoRequerimiento) {
		checkAuthenticity();
		if (!permiso("crear")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		CodigoRequerimiento dbCodigoRequerimiento = PopUpDocumentoRequerimientosVerificacionEditarController.getCodigoRequerimiento();
		VerificacionDocumento dbVerificacionDocumento = PopUpDocumentoRequerimientosVerificacionEditarController.getVerificacionDocumento(idVerificacionDocumento);

		PopUpDocumentoRequerimientosVerificacionEditarController.PopUpDocumentoRequerimientosVerificacionEditarBindReferences(codigoRequerimiento);

		if (!Messages.hasErrors()) {
			PopUpDocumentoRequerimientosVerificacionEditarController.PopUpDocumentoRequerimientosVerificacionEditarValidateCopy("crear", dbCodigoRequerimiento, codigoRequerimiento);
		}

		if (!Messages.hasErrors()) {
			PopUpDocumentoRequerimientosVerificacionEditarController.crearValidateRules(dbCodigoRequerimiento, codigoRequerimiento);
		}
		Long idCodigoRequerimiento = null;
		if (!Messages.hasErrors()) {
			Verificacion verificacion = Verificacion.find("select verificacion from Verificacion verificacion inner join verificacion.documentos vDoc where vDoc.id=?", idVerificacionDocumento).first();
			TiposCodigoRequerimiento tipoCodReq = TiposCodigoRequerimiento.find("select tipoCodReq from TiposCodigoRequerimiento tipoCodReq where (tipoCodReq.descripcionCorta=? and tipoCodReq.uriTramite=? and tipoCodReq.uriTipoDocumento=?)", codigoRequerimiento.descripcionCorta, verificacion.uriTramite, dbVerificacionDocumento.uriTipoDocumento).first();
			dbCodigoRequerimiento.descripcion = tipoCodReq.descripcion;
			dbCodigoRequerimiento.codigo = tipoCodReq.codigo;
			dbCodigoRequerimiento.save();
			idCodigoRequerimiento = dbCodigoRequerimiento.id;
			dbVerificacionDocumento.codigosRequerimiento.add(dbCodigoRequerimiento);
			dbVerificacionDocumento.save();
			log.info("Acción Crear de página: " + "gen/popups/PopUpDocumentoRequerimientosVerificacionEditar.html" + " , intentada con éxito");
		} else {
			log.info("Acción Crear de página: " + "gen/popups/PopUpDocumentoRequerimientosVerificacionEditar.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		return idCodigoRequerimiento;
	}

}
