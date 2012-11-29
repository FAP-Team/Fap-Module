package controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.jamonapi.utils.FileUtils;

import play.libs.IO;
import play.mvc.Util;
import play.utils.PThreadFactory;
import properties.FapProperties;
import reports.Report;
import utils.AEATUtils;
import utils.ATCUtils;
import utils.INSSUtils;
import messages.Messages;
import models.Cesiones;
import models.PeticionCesiones;
import models.SolicitudGenerica;
import controllers.gen.NuevaCesionControllerGen;
import enumerado.fap.gen.EstadosPeticionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.ListaCesionesEnum;
import enumerado.fap.gen.ListaEstadosEnum;
import enumerado.fap.gen.SeleccionExpedientesCesionEnum;


public class NuevaCesionController extends NuevaCesionControllerGen {
	
	public static void index(String accion, Long idPeticionCesiones) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/NuevaCesion/NuevaCesion.html");
		}

		PeticionCesiones peticionCesiones = null;
		if ("crear".equals(accion)) {
			peticionCesiones = NuevaCesionController.getPeticionCesiones();
			peticionCesiones.fechaGen=DateTime.now();
			peticionCesiones.estado = EstadosPeticionEnum.sinTipo.name();
			peticionCesiones.save();
			idPeticionCesiones = peticionCesiones.id;
			accion = "editar";
			
			//Parcheado con esto -> Render de abajo no funciona con editar
			NuevaCesionController.editarRender(idPeticionCesiones);

		} else if (!"borrado".equals(accion))
			peticionCesiones = NuevaCesionController.getPeticionCesiones(idPeticionCesiones);

		log.info("Visitando página: " + "fap/NuevaCesion/NuevaCesion.html");
		renderTemplate("fap/NuevaCesion/NuevaCesion.html", accion, idPeticionCesiones, peticionCesiones);
		
	}
	
	public static void generarpeticion(Long id, List<Long> idsSeleccionados) {
		
		if (idsSeleccionados!=null){
			//Generacion del fichero de consulta
			PeticionCesiones pt = getPeticionCesiones(id);
			if (pt.tipo.equals(ListaCesionesEnum.atc.name())){
				ATCUtils.peticionATC(pt, idsSeleccionados);
			}
			if (pt.tipo.equals(ListaCesionesEnum.aeat.name())){
				AEATUtils.peticionAEAT(pt, idsSeleccionados);
			}
			if (pt.tipo.equals(ListaCesionesEnum.inssA008.name())){
				INSSUtils.peticionINSSA008(pt, idsSeleccionados);
			}
			if (pt.tipo.equals(ListaCesionesEnum.inssR001.name())){
				INSSUtils.peticionINSSR001(pt, idsSeleccionados);
			}
			EditarCesionController.index("editar", pt.id);
		}
		else{
			Messages.error("Debe seleccionar al menos una solicitud de la lista");
			NuevaCesionController.editarRender(id);
		}
	}
	
	@Util
	public static void editarRender(Long idPeticionCesiones) {
		if (!Messages.hasMessages()) {
			Messages.keep();
			redirect("NuevaCesionController.index", "editar", idPeticionCesiones);
		}
		Messages.keep();
		redirect("NuevaCesionController.index", "editar", idPeticionCesiones);
	}
	
	
	//Necesito saber el tipo para filtrar
	public static void tablatblSolicitudes(Long idPeticionCesiones) {
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudGenerica> rowsFiltered = filtroSolicitudes(idPeticionCesiones);
		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);
		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estadoValue", "estado", "estadoUsuario", "solicitante.id", "solicitante.nombreCompleto"));
	}
	

	public static List<SolicitudGenerica> filtroSolicitudes(Long idPeticionCesiones){
		//Filtro dependiendo del valor del combo
		PeticionCesiones pt = getPeticionCesiones(idPeticionCesiones);

		java.util.List<SolicitudGenerica> rows = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud").fetch();
		List<SolicitudGenerica> rowsFiltered = new ArrayList<SolicitudGenerica>();		
		//Filtro de solicitudes (tipo y filtro de combo)
		for (SolicitudGenerica solGen : rows) {
			if((solGen.estado.equals(EstadosSolicitudEnum.iniciada.name())) && (solGen.cesion.autorizacionCesion.atc != null) && (solGen.cesion.autorizacionCesion.atc) && (pt.tipo.equals(ListaCesionesEnum.atc.name()))){
				rowsFiltered.add(solGen);
			}
			if((solGen.estado.equals(EstadosSolicitudEnum.iniciada.name())) && (solGen.cesion.autorizacionCesion.aeat != null) && (solGen.cesion.autorizacionCesion.aeat) && (pt.tipo.equals(ListaCesionesEnum.aeat.name()))){
				rowsFiltered.add(solGen);
			}
			if((solGen.estado.equals(EstadosSolicitudEnum.iniciada.name())) && (solGen.cesion.autorizacionCesion.inssR001 != null) && (solGen.cesion.autorizacionCesion.inssR001) && (pt.tipo.equals(ListaCesionesEnum.inssR001.name()))){
				rowsFiltered.add(solGen);
			}
			if((solGen.estado.equals(EstadosSolicitudEnum.iniciada.name())) && (solGen.cesion.autorizacionCesion.inssA008 != null) && (solGen.cesion.autorizacionCesion.inssA008) && (pt.tipo.equals(ListaCesionesEnum.inssA008.name()))){
				rowsFiltered.add(solGen);
			}
		}
		
		return rowsFiltered;
	}
	
	//Llamado desde ajax
	public static List<Long> filtroCombo (Long idPeticionCesiones, String combo, String fecha){
		List<SolicitudGenerica> rows = filtroSolicitudes(idPeticionCesiones);
		List<Long> id = new ArrayList<Long>();
		if (combo.equals(SeleccionExpedientesCesionEnum.todos.name())){ 
			for (Long i = (long)0; i < rows.size(); i++) {
				id.add(i);
			}
		} else {
			PeticionCesiones pt = getPeticionCesiones(idPeticionCesiones); 
			Long index = (long) 0;
			for (SolicitudGenerica sol : rows) {
				if (combo.equals(SeleccionExpedientesCesionEnum.certificadoFecha.name())){
					java.util.List<Cesiones> cesiones = Cesiones.find("select cesiones from SolicitudGenerica solicitud join solicitud.cesion.cesiones cesiones where solicitud.id=?", sol.id).fetch();
					//Si tengo certificados de ese tipo -> fecha 
					if (filtroFecha(pt, cesiones, fecha)){
						id.add(index);
					}
				} else if (combo.equals(SeleccionExpedientesCesionEnum.noCertificado.name())) { //Sin certificado o caducado
					java.util.List<Cesiones> cesiones = Cesiones.find("select cesiones from SolicitudGenerica solicitud join solicitud.cesion.cesiones cesiones where solicitud.id=?", sol.id).fetch();
					//SI cesiones tiene algo -> Hay certificado no se de q -> comprobar tipo y si fecha pasada
					if (filtroNoCertificado (pt, cesiones)){ //Vacia o caducada -> se muestra
						id.add(index);
					}
				}
				index++;
			}
		}
		return id;
	}
	
	public static boolean filtroNoCertificado (PeticionCesiones pt, List<Cesiones> cesiones){
		Cesiones cesion = null;
		for (Cesiones c : cesiones) {
			if ((c.fechaValidez.isAfterNow()) && (pt.tipo.equals(c.tipo))) //Fecha de validez posterior a hoy
				cesion = c; //Si es valida la igualo
		}
		if (cesion != null)
			return false; //No caducada
		else //Caducada
			return true;
	}
	
	public static boolean filtroFecha (PeticionCesiones pt, List<Cesiones> cesiones, String fecha){
		//Si es anterior a fecha -> Devuelve true -> lo añado
		Cesiones cesion = null;
		for (Cesiones c : cesiones) {
			if ((!fecha.isEmpty()) && (pt.tipo.equals(c.tipo)) && (c.fechaValidez.isBefore(obtenerFecha(fecha))) ) //Fecha de validez posterior a hoy
				cesion = c; //Si es valida la igualo
		}
		if (cesion != null)
			return true; //La fecha es anterior
		else //Caducada
			return false; //La fecha NO es anterior
	}
	
	public static DateTime obtenerFecha(String fecha){
		int dia = Integer.parseInt(fecha.substring(0, 2));
		int mes = Integer.parseInt(fecha.substring(3, 5));
		int anio = Integer.parseInt(fecha.substring(6, 10));
		DateTime fechaGeneracion = new DateTime(anio, mes, dia, 0, 0);
		return fechaGeneracion;
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formTipoCesion(Long idPeticionCesiones, PeticionCesiones peticionCesiones, String btnGuardar) {
		checkAuthenticity();
		if (!permisoFormTipoCesion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		PeticionCesiones dbPeticionCesiones = NuevaCesionController.getPeticionCesiones(idPeticionCesiones);

		NuevaCesionController.formTipoCesionBindReferences(peticionCesiones);

		if (!Messages.hasErrors()) {
			NuevaCesionController.formTipoCesionValidateCopy("editar", dbPeticionCesiones, peticionCesiones);
		}

		if (!Messages.hasErrors()) {
			NuevaCesionController.formTipoCesionValidateRules(dbPeticionCesiones, peticionCesiones);
		}
		if (!Messages.hasErrors()) {
			dbPeticionCesiones.save();
			log.info("Acción Editar de página: " + "fap/NuevaCesion/NuevaCesion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "fap/NuevaCesion/NuevaCesion.html" + " , intentada sin éxito (Problemas de Validación)");
		NuevaCesionController.formTipoCesionRender(idPeticionCesiones);
	}
	
}
