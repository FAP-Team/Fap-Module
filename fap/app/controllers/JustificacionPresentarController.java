package controllers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import messages.Messages;
import models.Agente;
import models.Justificacion;
import models.Documento;
import models.Firmante;
import models.SolicitudGenerica;
import models.TableKeyValue;
import platino.FirmaUtils;
import play.mvc.Util;
import properties.FapProperties;
import services.FirmaService;
import services.GestorDocumentalService;
import services.RegistroService;
import tramitacion.TramiteBase;
import controllers.fap.AgenteController;
import controllers.fap.JustificacionFapController;
import controllers.fap.PresentacionFapController;
import controllers.gen.JustificacionPresentarControllerGen;

public class JustificacionPresentarController extends JustificacionPresentarControllerGen {

    @Inject
    static FirmaService firmaService;

    @Inject
    static RegistroService registroService;

    @Inject
    static GestorDocumentalService gestorDocumentalService;

    public static void index(String accion, Long idSolicitud) {
        SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
        Justificacion justificacion = solicitud.justificaciones.actual;

        if (!justificacion.registro.fasesRegistro.borrador) {
            // Si la justificación no esta preparada, vuelve a la página para subir
            // documentos
            Messages.warning("Su justificación de documentación no está preparada para el registro. Pulse el botón 'Registrar justificacion'");
            Messages.keep();
            redirect("justificacionController.index", accion, idSolicitud);
        } else {
            renderTemplate("fap/Justificacion/JustificacionPresentar.html", accion, idSolicitud, solicitud);
        }
    }

    public static void modificarBorrador(Long idSolicitud) {
        checkAuthenticity();
        if (permisoModificarBorrador("editar") || permisoModificarBorrador("crear")) {
            try {
				TramiteBase tramite = JustificacionFapController.invoke("getTramiteObject", idSolicitud);
				tramite.deshacer();
				Messages.ok("Ahora puede modificar los datos de la solicitud de justificación.");
			} catch (Throwable e) {
				Messages.error("No se ha podido deshacer la Justificación.");
				play.Logger.info("No se ha podido deshacer la justificación de la solicitud: "+e.getMessage());
			}
        } else {
            Messages.fatal("No tiene permisos suficientes para realizar esta acción");
            Messages.keep();
        }
        modificarBorradorRender(idSolicitud);
    }
    
    @Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formFirmaFH(Long idSolicitud, String firma, String firmarRegistrarFH) {
		checkAuthenticity();
		if (!permisoFormFirmaFH("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
        Justificacion justificacion = solicitud.justificaciones.actual;

        if (!justificacion.registro.fasesRegistro.borrador) {
            Messages.error("La solicitud no está preparada para registrar");
        }

        if (!Messages.hasErrors()) {
			try {
				JustificacionFapController.invoke("comprobarFechaLimitejustificacion", idSolicitud);
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar los métodos comprobarFechaLimiteJustificación: "+e1.getMessage());
				Messages.error("(1)Error al validar las comprobaciones de la Fecha Límite de Justificación");
			}
		}
		
		if (!Messages.hasErrors()) {
			try {
				JustificacionFapController.invoke("beforeFirma", idSolicitud);
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar los métodos beforeFirma: "+e1.getMessage());
				Messages.error("Error al validar elementos previos a la firma");
			}
		}
		
		if (!Messages.hasErrors()) {
			SolicitudGenerica dbSolicitud = SolicitudPresentarFAPController.getSolicitudGenerica(idSolicitud);
			try {
				TramiteBase tramite = JustificacionFapController.invoke("getTramiteObject", idSolicitud);
				JustificacionPresentarController.firmarRegistrarFHFormFirmaFH(idSolicitud, firma);
				
				if (!Messages.hasErrors()) {
					try {
						JustificacionFapController.invoke("afterFirma", idSolicitud);
					} catch (Throwable e1) {
						log.error("Hubo un problema al invocar los métodos afterFirma: "+e1.getMessage());
						Messages.error("Error al validar elementos posteriores a la firma");
					}
				}
				
				if (!Messages.hasErrors()) {
					try {
						JustificacionFapController.invoke("beforeRegistro", idSolicitud);
					} catch (Throwable e1) {
						log.error("Hubo un problema al invocar los métodos beforeRegistro: "+e1.getMessage());
						Messages.error("Error al validar elementos previos al registro");
					}
				}
				
				if (!Messages.hasErrors()) {
					try {
						tramite.registrar();
						if (justificacion.registro.fasesRegistro.clasificarAed){
							justificacion.estado = "finalizada";
			            	justificacion.save();
						} else{
							play.Logger.error("No se registro la justificacion correctamente por lo que no se cambiara el estado de la misma.");
							Messages.error("Error al intentar sólo registrar.");
						}
						if (!Messages.hasErrors()) {
							try {
								JustificacionFapController.invoke("afterRegistro", idSolicitud);
							} catch (Throwable e1) {
								log.error("Hubo un problema al invocar los métodos afterRegistro: "+e1.getMessage());
								Messages.error("Error al validar elementos posteriores al registro");
							}
						}
					} catch (Exception e) {
						log.error("Hubo un error al registrar la solicitud: "+ e.getMessage());
						Messages.error("No se pudo registrar la solicitud");
					}
				}
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar el metodo que devuelve la clase TramiteBase en la firma: "+e1.getMessage());
				Messages.error("Error al intentar firmar antes de registrar");
			}
		}
		
        if (!Messages.hasErrors()) {
        	justificacion.estado = "finalizada";
            justificacion.save();
            Messages.ok("Su solicitud de justificación de documentación se registró correctamente");
        }

        presentarRender(idSolicitud);
	}
    
    @Util
	public static void firmarRegistrarFHFormFirmaFH(Long idSolicitud, String firma) {
		SolicitudGenerica solicitud = JustificacionController.getSolicitudGenerica(idSolicitud);

		play.Logger.info("Metodo: firmarRegistrarFHFormFirmaFH");
		Agente agente = AgenteController.getAgente();
		if (agente.getFuncionario()){
			List<Firmante> firmantes = new ArrayList<Firmante>();
			firmantes.add(new Firmante(agente));
			FirmaUtils.firmar(solicitud.justificaciones.actual.registro.oficial, firmantes, firma, null);
		} else {
			//ERROR
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		if (!Messages.hasErrors()) {
			solicitud.justificaciones.actual.estado = "firmada";
			solicitud.justificaciones.actual.registro.fasesRegistro.firmada = true;
			solicitud.save();
		}
	}

    /**
     * Firma y registra la solicitud de justificación de documentación
     * 
     * @param idSolicitud
     * @param firma
     */
    public static void presentar(Long idSolicitud, String firma) {
        checkAuthenticity();

        if (permisoPresentar("editar") || permisoPresentar("crear")) {

            SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
            Justificacion justificacion = solicitud.justificaciones.actual;

            if (!justificacion.registro.fasesRegistro.borrador) {
                Messages.error("La solicitud no está preparada para registrar");
            }

            if (!Messages.hasErrors()) {
    			try {
    				JustificacionFapController.invoke("comprobarFechaLimiteJustificacion", idSolicitud);
    			} catch (Throwable e1) {
    				log.error("Hubo un problema al invocar los métodos comprobarFechaLimiteJustificación: "+e1.getMessage());
    				Messages.error("(2)Error al validar las comprobaciones de la Fecha Límite de Justificación");
    			}
    		}
    		
    		if (!Messages.hasErrors()) {
    			try {
    				JustificacionFapController.invoke("beforeFirma", idSolicitud);
    			} catch (Throwable e1) {
    				log.error("Hubo un problema al invocar los métodos beforeFirma: "+e1.getMessage());
    				Messages.error("Error al validar elementos previos a la firma");
    			}
    		}
    		
    		if (!Messages.hasErrors()) {
    			SolicitudGenerica dbSolicitud = SolicitudPresentarFAPController.getSolicitudGenerica(idSolicitud);
    			try {
    				TramiteBase tramite = JustificacionFapController.invoke("getTramiteObject", idSolicitud);
    				// Llamará a la implementación de la última clase que extienda de TramiteBase
    				tramite.firmar(firma);
    				
    				if (!Messages.hasErrors()) {
    					try {
    						JustificacionFapController.invoke("afterFirma", idSolicitud);
    					} catch (Throwable e1) {
    						log.error("Hubo un problema al invocar los métodos afterFirma: "+e1.getMessage());
    						Messages.error("Error al validar elementos posteriores a la firma");
    					}
    				}
    				
    				if (!Messages.hasErrors()) {
    					try {
    						JustificacionFapController.invoke("beforeRegistro", idSolicitud);
    					} catch (Throwable e1) {
    						log.error("Hubo un problema al invocar los métodos beforeRegistro: "+e1.getMessage());
    						Messages.error("Error al validar elementos previos al registro");
    					}
    				}
    				
    				if (!Messages.hasErrors()) {
    					try {
    						tramite.registrar();
    						if (justificacion.registro.fasesRegistro.clasificarAed){
    							justificacion.estado = "finalizada";
    			            	justificacion.save();
    						} else{
    							play.Logger.error("No se registro la justificacion correctamente por lo que no se cambiara el estado de la misma.");
    							Messages.error("Error al intentar sólo registrar.");
    						}
    						if (!Messages.hasErrors()) {
    							try {
    								JustificacionFapController.invoke("afterRegistro", idSolicitud);
    							} catch (Throwable e1) {
    								log.error("Hubo un problema al invocar los métodos afterRegistro: "+e1.getMessage());
    								Messages.error("Error al validar elementos posteriores al registro");
    							}
    						}
    					} catch (Exception e) {
    						log.error("Hubo un error al registrar la solicitud: "+ e.getMessage());
    						Messages.error("No se pudo registrar la solicitud");
    					}
    				}
    			} catch (Throwable e1) {
    				log.error("Hubo un problema al invocar el metodo que devuelve la clase TramiteBase en la firma: "+e1.getMessage());
    				Messages.error("Error al intentar firmar antes de registrar");
    			}
    		}
    		
            if (!Messages.hasErrors()) {
            	justificacion.estado = "finalizada";
                justificacion.save();
                Messages.ok("Su solicitud de justificación de documentación se registró correctamente");
            }

        } else {
            Messages.fatal("No tiene permisos suficientes para realizar esta acción");
        }
        presentarRender(idSolicitud);
    }

    /**
     * Redireccionamos a la página de documentos justificados, ya que por defecto
     * redireccionaba a la página de recibos
     * 
     * @param idSolicitud
     */
    @Util
    public static void presentarRender(Long idSolicitud) {
        if (!Messages.hasMessages()) {
            Messages.ok("Página guardada correctamente");
        }
        Messages.keep();
        if (Messages.hasErrors()) {
            redirect("justificacionPresentarController.index", "editar", idSolicitud);
        } else {
            redirect("justificacionRecibosController.index", "editar", idSolicitud);
        }
    }
    
    @Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formHabilitarFH(Long idSolicitud, String btnHabilitarFH) {
		checkAuthenticity();
		if (!permisoFormHabilitarFH("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = JustificacionPresentarController.getSolicitudGenerica(idSolicitud);
		if (!Messages.hasErrors()) {
			try {
				TramiteBase tramite = PresentacionFapController.invoke("getTramiteObject", idSolicitud);
				boolean encontrado = false;
				for (Documento doc: tramite.getDocumentos()){
					if (doc.tipo.equals(FapProperties.get("fap.firmaYRegistro.funcionarioHabilitado.tipoDocumento"))){
						encontrado = true;
						break;
					}
				}
				if (!encontrado){
					log.error("El documento que autoriza la firma de un funcionario habilitado no ha sido subido o su tipo no es correcto. Uri del tipo correcto: "+FapProperties.get("fap.firmaYRegistro.funcionarioHabilitado.tipoDocumento"));
					Messages.error("El documento que autoriza la firma de un funcionario habilitado no ha sido subido o su tipo no es correcto.");
					Messages.error("Asegurese de haber subido el documento pertinente con tipo: "+TableKeyValue.getValue("tiposDocumentos", FapProperties.get("fap.firmaYRegistro.funcionarioHabilitado.tipoDocumento")));
				}
			} catch (Throwable e) {
				log.error("Hubo un problema al intentar verificar la presencia del documento de autorizacion funcionario habilitado: "+e.getMessage());
				Messages.error("No se pudo habilitar la firma de un Funcionario");
			}
		}

		if (!Messages.hasErrors()) {
			JustificacionPresentarController.formHabilitarFHValidateRules();
		}
		if (!Messages.hasErrors()) {
			dbSolicitud.justificaciones.actual.habilitaFuncionario=true;
			dbSolicitud.save();
			log.info("Acción Editar de página: " + "gen/justificacionPresentar/justificacionPresentar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/justificacionPresentar/justificacionPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		JustificacionPresentarController.formHabilitarFHRender(idSolicitud);
	}

}
