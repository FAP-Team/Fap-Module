package controllers;

import java.util.List;
import messages.Messages;
import models.Agente;
import models.SolicitudGenerica;
import models.SubvencionFap;
import play.mvc.Util;
import tramitacion.Documentos;
import tramitacion.TramiteAceptacionRenuncia;
import controllers.fap.AgenteController;
import controllers.gen.PaginaAceptacionRenunciaControllerGen;

public class PaginaAceptacionRenunciaController extends PaginaAceptacionRenunciaControllerGen {
	
	public static void index(String accion, Long idSolicitud) {
        if (accion == null)
               accion = getAccion();
        if (!permiso(accion)) {
               Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
               renderTemplate("gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html");
        }

        SolicitudGenerica solicitud = null;
        if ("crear".equals(accion)) {
               solicitud = PaginaAceptacionRenunciaController.getSolicitudGenerica();
               if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

                      solicitud.save();
                      idSolicitud = solicitud.id;

                      accion = "editar";
               }

        } else if (!"borrado".equals(accion)){
               solicitud = PaginaAceptacionRenunciaController.getSolicitudGenerica(idSolicitud);

               if (solicitud.aceptarRenunciar.seleccion!=null && solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase("acepta")){
                      if (solicitud.aceptarRenunciar.declaracionSubvenciones.nombreTramite==null
                                   || solicitud.aceptarRenunciar.declaracionSubvenciones.nombreTramite.equals("")){
                             //copiarDeclaracionSubvenciones(solicitud);

                             solicitud.aceptarRenunciar.declaracionSubvenciones.nombreTramite="Aceptación";
                             //Copio las subvenciones:
                             for (SubvencionFap sub : solicitud.declaracionSubvenciones.subvenciones) {
                                   SubvencionFap subNew=new SubvencionFap();
                                   subNew.entidad=sub.entidad;
                                   subNew.fechaAprobacion=sub.fechaAprobacion;
                                   subNew.fechaSolicitud=sub.fechaSolicitud;
                                   subNew.fondo=sub.fondo;
                                   subNew.importe=sub.importe;
                                   subNew.objeto=sub.objeto;
                                   subNew.programa=sub.programa;
                                   subNew.reglamento=sub.reglamento;
                                   subNew.situacion=sub.situacion;
                                   subNew.tipo=sub.tipo;
                                    solicitud.aceptarRenunciar.declaracionSubvenciones.subvenciones.add(subNew);
                             }
                             solicitud.aceptarRenunciar.declaracionSubvenciones.save();
                             solicitud.save();
                             //PaginaAceptacionRenunciaAppController.formAceptarRenunciarRender(idSolicitud);
                      }
               }

        }
        Agente logAgente = AgenteController.getAgente();
        log.info("Visitando página: " + "gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html" + " Agente: " + logAgente);
        renderTemplate("gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html", accion, idSolicitud, solicitud);
    }
	
	@Util
	public static void formAceptarRenunciar(Long idSolicitud, SolicitudGenerica solicitud) {
		checkAuthenticity();
		if (!permisoFormAceptarRenunciar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = PaginaAceptacionRenunciaController.getSolicitudGenerica(idSolicitud);
		PaginaAceptacionRenunciaController.formAceptarRenunciarBindReferences(dbSolicitud);

		if (!Messages.hasErrors()) {
			
			if (dbSolicitud.aceptarRenunciar.seleccion != null) {
				if (dbSolicitud.aceptarRenunciar.seleccion.equalsIgnoreCase("acepta"))
					dbSolicitud.aceptarRenunciar.motivoRenuncia = "";
				play.Logger.info("sdsdfsdfsd");
				if (dbSolicitud.aceptarRenunciar.seleccion.equalsIgnoreCase("renuncia")) {
//					Documentos.borrarDocumento(dbSolicitud.aceptarRenunciar, "documentos");
					for (int i = 0; i < dbSolicitud.aceptarRenunciar.documentos.size(); i++) {
						dbSolicitud.aceptarRenunciar.documentos.remove(i);
					}
				}
			}
			
			PaginaAceptacionRenunciaController.formAceptarRenunciarValidateCopy("editar", dbSolicitud, solicitud);

		}

		if (!Messages.hasErrors()) {
			PaginaAceptacionRenunciaController.formAceptarRenunciarValidateRules(dbSolicitud, solicitud);
		}
		
		if (!Messages.hasErrors()) {
			dbSolicitud.save();
			log.info("Acción Editar de página: " + "gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html" + " , intentada sin éxito (Problemas de Validación)");
		
		PaginaAceptacionRenunciaController.formAceptarRenunciarRender(idSolicitud);
	}
	
	@Util
	public static void prepararFirmar(Long idSolicitud, String botonPrepararFirmar) {
		checkAuthenticity();
		if (!permisoPrepararFirmar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica dbSolicitud = PaginaAceptacionRenunciaController.getSolicitudGenerica(idSolicitud);
		TramiteAceptacionRenuncia trAceptacionRenuncia = new TramiteAceptacionRenuncia(dbSolicitud);

		if (!Messages.hasErrors()) {
			trAceptacionRenuncia.prepararFirmar();
		}

		if (!Messages.hasErrors()) {
			PaginaAceptacionRenunciaController.prepararFirmarValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html" + " , intentada sin éxito (Problemas de Validación)");
		
		PaginaAceptacionRenunciaController.prepararFirmarRender(idSolicitud);
	}
	
	@Util
	public static void deshacer(Long idSolicitud, String botonModificar) {
		checkAuthenticity();
		if (!permisoDeshacer("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		SolicitudGenerica dbSolicitud = PaginaAceptacionRenunciaController.getSolicitudGenerica(idSolicitud);
		TramiteAceptacionRenuncia trAceptacionRenuncia = new TramiteAceptacionRenuncia(dbSolicitud);

		if (!Messages.hasErrors()) {
			trAceptacionRenuncia.deshacer();
		}

		if (!Messages.hasErrors()) {
			PaginaAceptacionRenunciaController.deshacerValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaAceptacionRenuncia/PaginaAceptacionRenuncia.html" + " , intentada sin éxito (Problemas de Validación)");
		
		PaginaAceptacionRenunciaController.deshacerRender(idSolicitud);
	}	
}
