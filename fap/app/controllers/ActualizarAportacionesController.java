package controllers;

import java.util.List;

import play.mvc.Util;

import messages.Messages;
import models.Aportacion;
import models.Firmantes;
import models.Registro;
import models.SolicitudGenerica;
import controllers.gen.ActualizarAportacionesControllerGen;

public class ActualizarAportacionesController extends ActualizarAportacionesControllerGen {
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void botonActualizarAportaciones(String actualizar) {
		checkAuthenticity();
		if (!permisoBotonActualizarAportaciones("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			actualizarAportaciones();
		}

		if (!Messages.hasErrors()) {
			ActualizarAportacionesController.botonActualizarAportacionesValidateRules();
		}
		if (!Messages.hasErrors()) {
			Messages.ok("Aportaciones migradas correctamente");
			Messages.keep();
			log.info("Acción Editar de página: " + "gen/actualizarAportaciones/actualizarAportaciones.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/actualizarAportaciones/actualizarAportaciones.html" + " , intentada sin éxito (Problemas de Validación)");
		ActualizarAportacionesController.botonActualizarAportacionesRender();
	}
	
	@Util
	public static void actualizarAportaciones(){
		// TODO: PROBAR TODO ESTO EN UNA APLICACION REAL EN PRE, Y ASEGURARSE QUE ESTO SOLO SE EJECUTA UNA VEZ, LA PRIMERA VEZ DE LA MIGRACION A ESTE NUEVO METODO Y NUNCA MAS
		// Para cambio de Antiguas Aportaciones con la nueva forma a traves del TrámiteAportación
		List <SolicitudGenerica> solicitudes = SolicitudGenerica.findAll();
		int oficial=0, borrador=0, justificante=0;
		for (SolicitudGenerica solicitud: solicitudes){
			// Recupero los documentos anteriores y los paso al nuevo sistema (registro)
			for (Aportacion aportacion: solicitud.aportaciones.registradas){
				
				if (aportacion.registro == null)
					aportacion.registro = new Registro();
				
				// Oficial
				if ((aportacion.oficial != null) && (aportacion.oficial.uri != null) && (!aportacion.oficial.uri.isEmpty())){
					aportacion.registro.oficial = aportacion.oficial;
					oficial++;
				} else
					play.Logger.error("Aportacion: "+aportacion.id+" no encontrado el documento Oficial");
				
				// Borrador
				if ((aportacion.borrador != null) && (aportacion.borrador.uri != null) && (!aportacion.borrador.uri.isEmpty())) {
					aportacion.registro.borrador = aportacion.borrador;
					borrador++;
				} else
					play.Logger.error("Aportacion: "+aportacion.id+" no encontrado el documento Borrador");
				
				// Justificante entrada
				if ((aportacion.justificante != null) && (aportacion.justificante.uri != null) && (!aportacion.justificante.uri.isEmpty())) {
					aportacion.registro.justificante = aportacion.justificante;
					justificante++;
				} else
					play.Logger.warn("Aportacion: "+aportacion.id+" no encontrado el Justificante");
				
				aportacion.save();
			}
			Aportacion aportacion = solicitud.aportaciones.actual;
			if ((aportacion.registro == null) || ((aportacion.estado != null) && (!aportacion.estado.isEmpty()) && (!aportacion.registro.fasesRegistro.borrador))){
				play.Logger.info("Aportación Actual "+aportacion.id+" con sistema antiguo de presentación Encontrada en la solicitud: "+solicitud.id);
				play.Logger.info("Aportacion: "+aportacion.id+" estado: "+aportacion.estado);
				
				if (aportacion.registro == null)
					aportacion.registro = new Registro();
				
				aportacion.registro.fasesRegistro.borrador=true;
				
				if (solicitud.registro.fasesRegistro.expedienteAed)
					aportacion.registro.fasesRegistro.expedienteAed=true;
				
				if ((solicitud.expedientePlatino != null) && (solicitud.expedientePlatino.uri != null) && ((!solicitud.expedientePlatino.uri.isEmpty())))
					aportacion.registro.fasesRegistro.expedientePlatino=true;
				
				// Firmantes
				aportacion.registro.firmantes = Firmantes.calcularFirmanteFromSolicitante(solicitud.solicitante);
				
				// Oficial
				if ((aportacion.oficial != null) && (aportacion.oficial.uri != null) && (!aportacion.oficial.uri.isEmpty()))
					aportacion.registro.oficial = aportacion.oficial;
				else
					play.Logger.error("Aportacion: "+aportacion.id+" no encontrado el documento Oficial");
				
				// Borrador
				if ((aportacion.borrador!= null) && (aportacion.borrador.uri != null) && (!aportacion.borrador.uri.isEmpty()))
					aportacion.registro.borrador = aportacion.borrador;
				else
					play.Logger.error("Aportacion: "+aportacion.id+" no encontrado el documento Borrador");
				
				// Justificante entrada
				if ((aportacion.justificante != null) && (aportacion.justificante.uri != null) && (!aportacion.justificante.uri.isEmpty()))
					aportacion.registro.justificante = aportacion.justificante;
				else
					play.Logger.warn("Aportacion: "+aportacion.id+" no encontrado el Justificante");
				
				if (aportacion.estado.equals("firmada")){
					aportacion.registro.fasesRegistro.firmada=true;
				} else if ((aportacion.estado.equals("registrada")) || (aportacion.estado.equals("finalizada"))){
					aportacion.registro.fasesRegistro.firmada=true;
					aportacion.registro.fasesRegistro.clasificarAed=true;
					aportacion.registro.fasesRegistro.registro=true;
				}
				play.Logger.info("Actualizando en la solicitud "+solicitud.id+" la aportación 'actual' "+aportacion.id+" creando y almacenando el registro y firmantes adecuados");
				aportacion.save();
			}
		}
		play.Logger.info("Actualizados: "+oficial+" documentos 'oficial', "+borrador+" documentos 'borrador' y "+justificante+" documentos 'justificante' de aportaciones registradas");
		// ------------------------------------------------------------------------------------------------------------------------------------
	}
	
}
