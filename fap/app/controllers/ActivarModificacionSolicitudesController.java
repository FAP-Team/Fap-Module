package controllers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityTransaction;

import org.joda.time.DateTime;

import com.google.gson.Gson;

import messages.Messages;
import models.JsonPeticionModificacion;
import models.Registro;
import models.RegistroModificacion;
import models.SolicitudGenerica;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.mvc.Util;
import tags.ComboItem;
import utils.ModelUtils;
import utils.PeticionModificacion;
import utils.PeticionModificacion.ValorCampoModificado;
import controllers.gen.ActivarModificacionSolicitudesControllerGen;

public class ActivarModificacionSolicitudesController extends ActivarModificacionSolicitudesControllerGen {
	
	public static List<ComboItem> fechaARestaurar() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		if ((ids == null) || (ids.get("idSolicitud") == null))
			return result;
		
		SolicitudGenerica solicitud = getSolicitudGenerica(ids.get("idSolicitud"));
		int i=1;
		ComboItem ultimoRegistrado = null;
		if ((solicitud.registro != null) && (solicitud.registro.fasesRegistro.registro) && (!solicitud.registroModificacion.isEmpty()))
			ultimoRegistrado = new ComboItem(solicitud.registroModificacion.get(0).id.toString(), solicitud.registro.informacionRegistro.fechaRegistro.toString("dd/MM/yyyy")+" Presentación Inicial");
		for (RegistroModificacion rm: solicitud.registroModificacion){
			if ((rm.registro != null) && (rm.registro.fasesRegistro.registro))
				ultimoRegistrado = new ComboItem(rm.id.toString(), rm.registro.informacionRegistro.fechaRegistro.toString("dd/MM/yyyy")+" "+String.valueOf(i)+"º Modificación");
			i++;
		}
		result.add(ultimoRegistrado);
		return result;
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formRestaurarModificacion(Long idSolicitud, SolicitudGenerica solicitud, String restaurarBtn) {
		checkAuthenticity();
		Long idRegistroModificacion = null;
		if (!permisoFormRestaurarModificacion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			Long idRecuperar = null;
			if (!solicitud.fechaARestaurarStr.isEmpty())
			 idRegistroModificacion = Long.parseLong(solicitud.fechaARestaurarStr);
			if (idRegistroModificacion != null) {
				SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(idSolicitud);
				boolean recuperarPresentacion = true;
				for (RegistroModificacion rm: dbSolicitud.registroModificacion){
					//Comprobar si se han creado elementos nuevos que haya que borrar
					if (rm.estado.equals("En Curso")){
						idRecuperar = rm.id;
						ModelUtils.restaurarBorrados(rm.id, idSolicitud);
						ModelUtils.restaurarSolicitud(idRecuperar, idSolicitud, false);
					}
				}			
				for (RegistroModificacion rm: dbSolicitud.registroModificacion){
					if (rm.estado.equals("En Curso")){
						ModelUtils.eliminarCreados(rm.id, idSolicitud);
					}
				}
				ModelUtils.finalizarDeshacerModificacion(idSolicitud);
				
				log.info("Acción Editar de página: " + "gen/ActivarModificacionSolicitudes/ActivarModificacionSolicitudes.html" + " , intentada con éxito");
			} else {
				Messages.error("Hubo un fallo al intentar recuperar el Registro correspondiente");
			}
		} else
			log.info("Acción Editar de página: " + "gen/ActivarModificacionSolicitudes/ActivarModificacionSolicitudes.html" + " , intentada sin éxito (Problemas de Validación)");
		ActivarModificacionSolicitudesController.formRestaurarModificacionRender(idSolicitud);
	}
	
}
