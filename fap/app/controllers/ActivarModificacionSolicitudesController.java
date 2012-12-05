package controllers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.gson.Gson;

import messages.Messages;
import models.JsonPeticionModificacion;
import models.Registro;
import models.RegistroModificacion;
import models.SolicitudGenerica;
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
		if (!permisoFormRestaurarModificacion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			Long idRegistroModificacion = Long.parseLong(solicitud.fechaARestaurarStr);
			restaurarSolicitud(idRegistroModificacion, idSolicitud);
			log.info("Acción Editar de página: " + "gen/ActivarModificacionSolicitudes/ActivarModificacionSolicitudes.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/ActivarModificacionSolicitudes/ActivarModificacionSolicitudes.html" + " , intentada sin éxito (Problemas de Validación)");
		ActivarModificacionSolicitudesController.formRestaurarModificacionRender(idSolicitud);
	}
	
	@Util
	public static void restaurarSolicitud(Long idRegistroModificacion, Long idSolicitud){
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		PeticionModificacion peticionModificacion;
		Gson gson = new Gson();
		for (JsonPeticionModificacion json: registroModificacion.jsonPeticionesModificacion){
			peticionModificacion = gson.fromJson(json.jsonPeticion, PeticionModificacion.class);
			aplicarCambios(idSolicitud, peticionModificacion);
		}
	}
	
	@Util 
	public static void aplicarCambios(Long idSolicitud, PeticionModificacion peticionModificacion){
		for (ValorCampoModificado valor: peticionModificacion.valoresModificado){
			int numeroCampos = valor.nombreCampo.split("\\.").length;
			Model modeloEntidad = null;
			Model modeloEntidadPrimera = null;
			Method metodo = null;
			Class claseEntidad = null;
			String entidad = "";
			int camposRecorridos=1;
			for (String campo : valor.nombreCampo.split("\\.")){
				if (camposRecorridos == 1){
					entidad = tags.StringUtils.firstUpper(campo);
					Long idEntidad = peticionModificacion.idSimples.get("id"+entidad);
					try {
						claseEntidad = Class.forName("models."+entidad);				
						Method findById = claseEntidad.getDeclaredMethod("findById", Object.class);
						modeloEntidad = (Model)findById.invoke(claseEntidad.newInstance(), idEntidad);
						modeloEntidadPrimera = (Model)findById.invoke(claseEntidad.newInstance(), idEntidad);
					} catch (Exception e) {
						play.Logger.error("Error recuperando por reflection la entidad "+entidad+" - "+e.getMessage());
					}
				} else {
					if (camposRecorridos == numeroCampos){ // LLEGAMOS AL SETER
						try {
							entidad = tags.StringUtils.firstUpper(campo);
							Field field = claseEntidad.getField(campo);
							ModelUtils.setValueFromTypeAttribute(claseEntidad, modeloEntidad, modeloEntidadPrimera, entidad, field, valor.valoresAntiguos);
							break;
						} catch (Exception e) {
							play.Logger.error("Error recuperando por reflection el campo "+entidad+" - "+e.getMessage());
						}
					} else { // VAMOS RECUPERANDO GETTERS
						try { 
							entidad = tags.StringUtils.firstUpper(campo);
							metodo = claseEntidad.getMethod("get"+entidad);
							modeloEntidad = (Model) metodo.invoke(modeloEntidad);
							claseEntidad = Class.forName(modeloEntidad.getClass().getName());
						} catch (Exception e) {
							play.Logger.error("Error recuperando por reflection la entidad "+entidad+" - "+e.getMessage());
						}
					}
				}
				camposRecorridos++;
			}
		}
	}
	
}
