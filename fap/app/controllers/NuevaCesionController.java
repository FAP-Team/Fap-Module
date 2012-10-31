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
import properties.FapProperties;
import reports.Report;
import utils.AEATUtils;
import utils.ATCUtils;
import utils.INSSUtils;
import messages.Messages;
import models.PeticionCesiones;
import models.SolicitudGenerica;
import controllers.gen.NuevaCesionControllerGen;
import enumerado.fap.gen.EstadosPeticionEnum;


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
			peticionCesiones.estado = EstadosPeticionEnum.creada.name();
			peticionCesiones.fechaGen=DateTime.now();
			peticionCesiones.save();
			idPeticionCesiones = peticionCesiones.id;
			accion = "editar";
			//Parcheado con esto -> Render de abajo no funciona con editar
			NuevaCesionController.editarRender(idPeticionCesiones);

		} else if (!"borrado".equals(accion))
			peticionCesiones = NuevaCesionController.getPeticionCesiones(idPeticionCesiones);

		log.info("*Visitando página: " + "gen/NuevaCesion/NuevaCesion.html");
		renderTemplate("fap/NuevaCesion/NuevaCesion.html", accion, idPeticionCesiones, peticionCesiones);
		
	}
	

	
	public static void generarpeticion(Long id, List<Long> idsSeleccionados) {
		
		if (idsSeleccionados!=null){
			//Generacion del fichero de consulta
			PeticionCesiones pt = getPeticionCesiones(id);
			if (pt.tipo.equals("atc")){
				ATCUtils.peticionATC(pt, idsSeleccionados);
			}
			if (pt.tipo.equals("aeat")){
				AEATUtils.peticionAEAT(pt, idsSeleccionados);
			}
			if (pt.tipo.equals("inssA008")){
				//INSSUtils.peticionINSSA008(idsSeleccionados);
			}
			if (pt.tipo.equals("inssR001")){
				INSSUtils.peticionINSSR001(pt, idsSeleccionados);
			}
			redirect("GenerarFichCesionesController.index", "crear");
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
		//Recuperar la Peticion para saber el tipo
		PeticionCesiones pt = getPeticionCesiones(idPeticionCesiones); 
		java.util.List<SolicitudGenerica> rows = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud").fetch();
		
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudGenerica> rowsFiltered = new ArrayList<SolicitudGenerica>();		
		
		//Unificación de permisos: Desde que se autoriza en una solicitud, se autoriza en todas las del usuario
		//unificarPermisos();
		
		//Filtro de solicitudes
		for (SolicitudGenerica solGen : rows) {
			if((solGen.cesion.autorizacionCesion.atc != null) && (solGen.cesion.autorizacionCesion.atc) && (pt.tipo.equals("atc"))){
				rowsFiltered.add(solGen);
			}
			if((solGen.cesion.autorizacionCesion.aeat != null) && (solGen.cesion.autorizacionCesion.aeat) && (pt.tipo.equals("aeat"))){
				rowsFiltered.add(solGen);
			}
			if((solGen.cesion.autorizacionCesion.inssR001 != null) && (solGen.cesion.autorizacionCesion.inssR001) && (pt.tipo.equals("inssR001"))){
				rowsFiltered.add(solGen);
			}
			if((solGen.cesion.autorizacionCesion.inssA008 != null) && (solGen.cesion.autorizacionCesion.inssA008) && (pt.tipo.equals("inssA008"))){
				rowsFiltered.add(solGen);
			}
		}
		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);
		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estadoValue", "estado", "estadoUsuario", "solicitante.id", "solicitante.nombreCompleto"));
	}
	
	/*@Util
	public static void unificarPermisos(){
		java.util.List<SolicitudGenerica> rows = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud").fetch();
		java.util.List<SolicitudGenerica> rowsSol = new ArrayList<SolicitudGenerica>();
		for (SolicitudGenerica solGen : rows) {
			if (solGen.solicitante.isPersonaFisica()){ //Persona Fisica
				rowsSol = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud where solicitud.solicitante.fisica.nip.valor = ?", solGen.solicitante.fisica.nip.valor).fetch();
			}
			if (solGen.solicitante.isPersonaJuridica()){ //Persona Juridica
				rowsSol = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud where solicitud.solicitante.juridica.cif = ?", solGen.solicitante.juridica.cif).fetch();
			}
		}
		//Al ser del mismo solicitante, si autoriza en una solicitud, autoriza en todas
		if (rowsSol.size()>1){ //Más de una solicitud por solicitante
			Boolean atcS = false, aeatS = false, inssA008S = false, inssR001S = false;
			for (SolicitudGenerica solGen : rowsSol) { //Busco los permisos que hay en todas las solicitudes
				if((solGen.cesion.autorizacionCesion.atc != null) && (solGen.cesion.autorizacionCesion.atc))
					atcS = true;
				if((solGen.cesion.autorizacionCesion.aeat != null) && (solGen.cesion.autorizacionCesion.aeat))
					aeatS = true;
				if((solGen.cesion.autorizacionCesion.inssR001 != null) && (solGen.cesion.autorizacionCesion.inssR001))
					inssR001S = true;
				if((solGen.cesion.autorizacionCesion.inssA008 != null) && (solGen.cesion.autorizacionCesion.inssA008))
					inssA008S = true;
			}
			
			//Unifico los permisos entre todas las solicitudes del mismo solicitante
			for (SolicitudGenerica solGen : rowsSol) {
				if (atcS)
					solGen.cesion.autorizacionCesion.atc = true;
				if (aeatS)
					solGen.cesion.autorizacionCesion.aeat = true;
				if (inssR001S)
					solGen.cesion.autorizacionCesion.inssR001 = true;
				if (inssA008S)
					solGen.cesion.autorizacionCesion.inssA008 = true;
				
			}
		}
	}*/
	
}
