package controllers;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;

import messages.Messages;
import models.Agente;
import models.Aportacion;
import models.Documento;
import models.Firma;
import models.Firmante;
import models.Firmantes;
import models.JustificanteRegistro;
import models.Registro;
import models.SolicitudGenerica;
import models.TableKeyValue;
import platino.FirmaUtils;
import platino.InfoCert;
import play.mvc.Util;
import properties.FapProperties;
import services.FirmaService;
import services.FirmaServiceException;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.RegistroServiceException;
import services.RegistroService;
import sun.util.logging.resources.logging;
import tramitacion.TramiteBase;
import validation.CustomValidation;
import controllers.fap.AgenteController;
import controllers.fap.AportacionFapController;
import controllers.fap.FirmaController;
import controllers.fap.PresentacionFapController;
import controllers.gen.AportacionPresentarControllerGen;
import emails.Mails;

public class AportacionPresentarController extends AportacionPresentarControllerGen {

    @Inject
    static FirmaService firmaService;

    @Inject
    static RegistroService registroService;

    @Inject
    static GestorDocumentalService gestorDocumentalService;

    public static void index(String accion, Long idSolicitud) {
        SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
        Aportacion aportacion = solicitud.aportaciones.actual;

        if (!aportacion.registro.fasesRegistro.borrador) {
            // Si la aportación no esta preparada, vuelve a la página para subir
            // documentos
            Messages.warning("Su aportación de documentación no está preparada para el registro. Pulse el botón 'Registrar Aportacion'");
            Messages.keep();
            redirect("AportacionController.index", accion, idSolicitud);
        } else {
            renderTemplate("fap/Aportacion/AportacionPresentar.html", accion, idSolicitud, solicitud);
        }
    }

    public static void modificarBorrador(Long idSolicitud) {
        checkAuthenticity();
        if (permisoModificarBorrador("editar") || permisoModificarBorrador("crear")) {
            try {
				//Reinicia el estado de la aportación
            	SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
                Aportacion aportacion = solicitud.aportaciones.actual;
            	aportacion.estado = null;
				aportacion.save();
				
				TramiteBase tramite = AportacionFapController.invoke("getTramiteObject", idSolicitud);
				tramite.deshacer();
				Messages.ok("Ahora puede modificar los datos de la solicitud de aportación.");
			} catch (Throwable e) {
				Messages.error("No se ha podido deshacer la Aportación.");
				play.Logger.info("No se ha podido deshacer la aportación de la solicitud: "+e.getMessage());
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
        Aportacion aportacion = solicitud.aportaciones.actual;

        if (!aportacion.registro.fasesRegistro.borrador) {
            Messages.error("La solicitud no está preparada para registrar");
        }

        if (!Messages.hasErrors()) {
			try {
				AportacionFapController.invoke("comprobarFechaLimiteAportacion", idSolicitud);
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar los métodos comprobarFechaLimiteAportación: "+e1.getMessage());
				Messages.error("Error al validar las comprobaciones de la Fecha Límite de Aportación");
			}
		}
		
		if (!Messages.hasErrors()) {
			try {
				AportacionFapController.invoke("beforeFirma", idSolicitud);
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar los métodos beforeFirma: "+e1.getMessage());
				Messages.error("Error al validar elementos previos a la firma");
			}
		}
		
		if (!Messages.hasErrors()) {
			SolicitudGenerica dbSolicitud = SolicitudPresentarFAPController.getSolicitudGenerica(idSolicitud);
			try {
				TramiteBase tramite = AportacionFapController.invoke("getTramiteObject", idSolicitud);
				AportacionPresentarController.firmarRegistrarFHFormFirmaFH(idSolicitud, firma);
				
				if (!Messages.hasErrors()) {
					try {
						AportacionFapController.invoke("afterFirma", idSolicitud);
					} catch (Throwable e1) {
						log.error("Hubo un problema al invocar los métodos afterFirma: "+e1.getMessage());
						Messages.error("Error al validar elementos posteriores a la firma");
					}
				}
				
				if (!Messages.hasErrors()) {
					try {
						AportacionFapController.invoke("beforeRegistro", idSolicitud);
					} catch (Throwable e1) {
						log.error("Hubo un problema al invocar los métodos beforeRegistro: "+e1.getMessage());
						Messages.error("Error al validar elementos previos al registro");
					}
				}
				
				if (!Messages.hasErrors()) {
					try {
						tramite.registrar();
						if (aportacion.registro.fasesRegistro.clasificarAed){
							aportacion.estado = "finalizada";
			            	aportacion.save();
						} else{
							play.Logger.error("No se registro la aportacion correctamente por lo que no se cambiara el estado de la misma.");
							Messages.error("Error al intentar sólo registrar.");
						}
						if (!Messages.hasErrors()) {
							try {
								AportacionFapController.invoke("afterRegistro", idSolicitud);
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
        	aportacion.estado = "finalizada";
            aportacion.save();
            Messages.ok("Su solicitud de aportación de documentación se registró correctamente");
        }

        presentarRender(idSolicitud);
	}
    
    @Util
	public static void firmarRegistrarFHFormFirmaFH(Long idSolicitud, String firma) {
		SolicitudGenerica solicitud = AportacionController.getSolicitudGenerica(idSolicitud);

		play.Logger.info("Metodo: firmarRegistrarFHFormFirmaFH");
		Agente agente = AgenteController.getAgente();
		if (agente.getFuncionario()){
			List<Firmante> firmantes = new ArrayList<Firmante>();
			firmantes.add(new Firmante(agente));
			FirmaUtils.firmar(solicitud.aportaciones.actual.registro.oficial, firmantes, firma, null);
		} else {
			//ERROR
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		if (!Messages.hasErrors()) {
			solicitud.aportaciones.actual.estado = "firmada";
			solicitud.aportaciones.actual.registro.fasesRegistro.firmada = true;
			solicitud.save();
		}
	}

    /**
     * Firma y registra la solicitud de aportación de documentación
     * 
     * @param idSolicitud
     * @param firma
     */
    public static void presentar(Long idSolicitud, String firma) {
        checkAuthenticity();

        if (permisoPresentar("editar") || permisoPresentar("crear")) {

            SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
            Aportacion aportacion = solicitud.aportaciones.actual;

            if (!aportacion.registro.fasesRegistro.borrador) {
                Messages.error("La solicitud no está preparada para registrar");
            }

            if (!Messages.hasErrors()) {
    			try {
    				AportacionFapController.invoke("comprobarFechaLimiteAportacion", idSolicitud);
    			} catch (Throwable e1) {
    				log.error("Hubo un problema al invocar los métodos comprobarFechaLimiteAportación: "+e1.getMessage());
    				Messages.error("Error al validar las comprobaciones de la Fecha Límite de Aportación");
    			}
    		}
    		
    		if (!Messages.hasErrors()) {
    			try {
    				AportacionFapController.invoke("beforeFirma", idSolicitud);
    			} catch (Throwable e1) {
    				log.error("Hubo un problema al invocar los métodos beforeFirma: "+e1.getMessage());
    				Messages.error("Error al validar elementos previos a la firma");
    			}
    		}
    		
    		if (!Messages.hasErrors()) {
    			SolicitudGenerica dbSolicitud = SolicitudPresentarFAPController.getSolicitudGenerica(idSolicitud);
    			try {
    				TramiteBase tramite = AportacionFapController.invoke("getTramiteObject", idSolicitud);
    				// Llamará a la implementación de la última clase que extienda de TramiteBase
    				tramite.firmar(firma);
    				if (aportacion.registro.fasesRegistro.firmada){
    					aportacion.estado = "firmada";
    					aportacion.save();
    				}
    				if (!Messages.hasErrors()) {
    					try {
    						AportacionFapController.invoke("afterFirma", idSolicitud);
    					} catch (Throwable e1) {
    						log.error("Hubo un problema al invocar los métodos afterFirma: "+e1.getMessage());
    						Messages.error("Error al validar elementos posteriores a la firma");
    					}
    				}
    				
    				if (!Messages.hasErrors()) {
    					try {
    						AportacionFapController.invoke("beforeRegistro", idSolicitud);
    					} catch (Throwable e1) {
    						log.error("Hubo un problema al invocar los métodos beforeRegistro: "+e1.getMessage());
    						Messages.error("Error al validar elementos previos al registro");
    					}
    				}
    				
    				if (!Messages.hasErrors()) {
    					try {
    						tramite.registrar();
    						if (aportacion.registro.fasesRegistro.clasificarAed){
    							aportacion.estado = "finalizada";
    			            	aportacion.save();
    						} else{
    							play.Logger.error("No se registro la aportacion correctamente por lo que no se cambiara el estado de la misma.");
    							Messages.error("Error al intentar sólo registrar.");
    						}
    						if (!Messages.hasErrors()) {
    							try {
    								AportacionFapController.invoke("afterRegistro", idSolicitud);
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
            	aportacion.estado = "finalizada";
                aportacion.save();
                Messages.ok("Su solicitud de aportación de documentación se registró correctamente");
            }

        } else {
            Messages.fatal("No tiene permisos suficientes para realizar esta acción");
        }
        presentarRender(idSolicitud);
    }

    /**
     * Redireccionamos a la página de documentos aportados, ya que por defecto
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
            redirect("AportacionPresentarController.index", "editar", idSolicitud);
        } else {
            redirect("AportacionRecibosController.index", "editar", idSolicitud);
        }
    }
    
    @Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formHabilitarFH(Long idSolicitud, String btnHabilitarFH) {
		checkAuthenticity();
		if (!permisoFormHabilitarFH("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = AportacionPresentarController.getSolicitudGenerica(idSolicitud);
		if (!Messages.hasErrors()) {
			try {
				TramiteBase tramite = PresentacionFapController.invoke(PresentacionFapController.class, "getTramiteObject", idSolicitud);
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
			AportacionPresentarController.formHabilitarFHValidateRules();
		}
		if (!Messages.hasErrors()) {
			dbSolicitud.aportaciones.actual.habilitaFuncionario=true;
			dbSolicitud.save();
			log.info("Acción Editar de página: " + "gen/AportacionPresentar/AportacionPresentar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/AportacionPresentar/AportacionPresentar.html" + " , intentada sin éxito (Problemas de Validación)");
		AportacionPresentarController.formHabilitarFHRender(idSolicitud);
	}

}
