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

        if (aportacion.estado == null) {
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
            SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
            Aportacion aportacion = solicitud.aportaciones.actual;
            aportacion.estado = null;
            aportacion.save();
            Messages.ok("Ahora puede modificar los datos de la solicitud de aportación.");
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

        if (aportacion.estado == null) {
            Messages.error("La solicitud no está preparada para registrar");
        }

        almacenarFirmaAportacionFH(firma, solicitud, aportacion);
        registrarAportacion(solicitud, aportacion);
        clasificarDocumentosAportacionConRegistro(solicitud, aportacion);
        finalizarAportacion(solicitud, aportacion);

        if (!Messages.hasErrors()) {
            Messages.ok("Su solicitud de aportación de documentación se registró correctamente");
        }

        presentarRender(idSolicitud);
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

            if (aportacion.estado == null) {
                Messages.error("La solicitud no está preparada para registrar");
            }

            almacenarFirmaAportacion(firma, solicitud, aportacion);
            registrarAportacion(solicitud, aportacion);
            clasificarDocumentosAportacionConRegistro(solicitud, aportacion);
            finalizarAportacion(solicitud, aportacion);

            if (!Messages.hasErrors()) {
                Messages.ok("Su solicitud de aportación de documentación se registró correctamente");
            }

        } else {
            Messages.fatal("No tiene permisos suficientes para realizar esta acción");
        }
        presentarRender(idSolicitud);
    }

    /**
     * Valida que la firma sea correcta y que sea una de las personas que puede firmar la aportación
     * 
     * Cambia el estado a firmada
     * 
     * @param firma
     * @param solicitud
     * @param aportacion
     */
    private static void almacenarFirmaAportacion(String firma, SolicitudGenerica solicitud, Aportacion aportacion) {
        if (!Messages.hasErrors() && "borrador".equals(aportacion.estado)) {
            Firmante firmante = firmaService.getFirmante(firma, aportacion.oficial);

            if(!Messages.hasErrors()){
                if (isFirmanteValido(solicitud, firmante)) {
                    almacenarFirma(firma, aportacion, firmante);
                }
            }

            if (!Messages.hasErrors()) {
                // Firma válida y almacenada
                aportacion.estado = "firmada";
                aportacion.save();
            }
        }
    }
    
    private static void almacenarFirmaAportacionFH(String firma, SolicitudGenerica solicitud, Aportacion aportacion) {
        if (!Messages.hasErrors() && "borrador".equals(aportacion.estado)) {
            Firmante firmante = firmaService.getFirmante(firma, aportacion.oficial);
            Agente agente = AgenteController.getAgente();
            if(!Messages.hasErrors()){
                if ((firmante != null) && (agente.username.equals(firmante.idvalor)) && (agente.getFuncionario())) {
                    almacenarFirma(firma, aportacion, firmante);
                } else {
                	Messages.error("Firmante Funcionario Habilitado no Válido");
                	play.Logger.error("El firmante no es valido o no coincide con el agente conectado en FH");
                }
            }

            if (!Messages.hasErrors()) {
                // Firma válida y almacenada
                aportacion.estado = "firmada";
                aportacion.save();
            }
        }
    }

    private static void almacenarFirma(String firma, Aportacion aportacion, Firmante firmante) {
        try {
        	firmante.fechaFirma = new DateTime();
            gestorDocumentalService.agregarFirma(aportacion.oficial, new Firma(firma, firmante));
        } catch (Exception e) {
            Messages.error("Error guardando la firma del documento");
        }
    }

    /**
     * Comprueba si la firma se corresponde con uno de los firmantes válidos de
     * la solicitud
     * 
     * @param firma
     * @param solicitud
     * @return
     * @throws FirmaServiceException
     */
    private static boolean isFirmanteValido(SolicitudGenerica solicitud, Firmante firmante) {
        Firmantes firmantesValidos = Firmantes.calcularFirmanteFromSolicitante(solicitud.solicitante);
        boolean result = firmantesValidos.containsFirmanteConIdentificador(firmante.idvalor);

        if (!result){
        	String firmantes="{";
        	for (Firmante firm: firmantesValidos.todos){
        		firmantes+=firm.toString()+" | ";
        	}
        	firmantes+="}";
        	play.Logger.error("El certificado <"+firmante.idvalor+"> no se corresponde con uno que debe firmar la solicitud: "+firmantes);
            Messages.error("El certificado no se corresponde con uno que debe firmar la solicitud");
        }

        return result;
    }

    /**
     * Registra la solicitud
     * 
     * Cambia el estado a registrada
     * 
     * @param solicitud
     * @param aportacion
     */
    private static void registrarAportacion(SolicitudGenerica solicitud, Aportacion aportacion) {
        // Registro de entrada en platino
        if (aportacion.estado != null && "firmada".equals(aportacion.estado)) {
            try {
                // Registra la solicitud
                JustificanteRegistro justificante = registroService.registrarEntrada(solicitud.solicitante,
                        aportacion.oficial, solicitud.expedientePlatino, null);
                play.Logger.info("Se ha registrado la solicitud de aportacion %s de la solicitud %s en platino",
                        aportacion.id, solicitud.id);

                // Almacena la información de registro
                aportacion.informacionRegistro.setDataFromJustificante(justificante);
                play.Logger.info("Almacenada la información del registro en la base de datos");
                
                /// Establecemos la fecha de registro en todos los documentos de la aportación
				for (Documento doc: aportacion.documentos) {
					doc.fechaRegistro = aportacion.informacionRegistro.fechaRegistro;
					doc.save();
				}

                // Guarda el justificante en el AED
                play.Logger.info("Se procede a guardar el justificante de la solicitud %s en el AED", solicitud.id);
                guardarJustificanteEnGestorDocumental(solicitud, aportacion, justificante);

                // Cambia el estado
                aportacion.estado = "registrada";
                aportacion.save();
            } catch (Exception e) {
                Messages.error("Error al registrar de entrada la solicitud");
                return;
            }
            
            try {
                //Envia el email
                Mails.enviar("aportacionRealizada", solicitud);
            }catch(Exception e){
                play.Logger.info("Error enviando email de aportación realizada de la solicitud " + solicitud.id);
            }
        }
    }

    private static void guardarJustificanteEnGestorDocumental(SolicitudGenerica solicitud, Aportacion aportacion,
            JustificanteRegistro justificante) throws GestorDocumentalServiceException, IOException {
        Documento documento = aportacion.justificante;
        documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSolicitud");
        documento.descripcion = "Justificante de registro";
        documento.fechaRegistro = justificante.getFechaRegistro();
        documento.save();

        InputStream is = justificante.getDocumento().contenido.getInputStream();
        gestorDocumentalService.saveDocumentoTemporal(documento, is, "JustificanteSolicitud" + solicitud.id + ".pdf");
        play.Logger.info("Justificante almacenado en el AED");
    }

    /**
     * Clasifica los documentos de la aportación
     * 
     * Cambia el estado de la aportación a clasificada
     * 
     * @param solicitud
     * @param aportacion
     */
    private static void clasificarDocumentosAportacionConRegistro(SolicitudGenerica solicitud, Aportacion aportacion) {
        // Clasifica los documentos
        if (aportacion.estado.equals("registrada")) {
            boolean todosClasificados = true;

            // Clasifica los documentos sin registro
            List<Documento> documentos = new ArrayList<Documento>();
            documentos.add(aportacion.justificante);

            try {
                gestorDocumentalService.clasificarDocumentos(solicitud, documentos);
            } catch (GestorDocumentalServiceException e) {
                todosClasificados = false;
            }

            // Clasifica los documentos con registro de entrada
            List<Documento> documentosRegistrados = new ArrayList<Documento>();
            documentosRegistrados.add(aportacion.oficial);
            documentosRegistrados.addAll(aportacion.documentos);
            
            try {
                gestorDocumentalService.clasificarDocumentos(solicitud, documentosRegistrados,
                        aportacion.informacionRegistro);
            } catch (Exception e) {
                todosClasificados = false;
            }

            if (todosClasificados) {
                aportacion.estado = "clasificada";
                aportacion.save();
                play.Logger.info("Se clasificaron todos los documentos");
            } else {
                Messages.error("Algunos documentos no se pudieron clasificar correctamente");
                Messages.fatal("Algunos documentos no se pudieron clasificar correctamente: Solicitud("
                		+solicitud.id+") Aportación("+aportacion.id+")");
            }
        } else {
            play.Logger.debug("Ya están clasificados todos los documentos de la solicitud %s", solicitud.id);
        }
    }

    /**
     * Mueve la aportación a la lista de aportaciones clasificadas Añade los
     * documentos a la lista de documentos
     * 
     * Cambia el estado de la aportación a finalizada
     * 
     * @param solicitud
     * @param aportacion
     */
    private static void finalizarAportacion(SolicitudGenerica solicitud, Aportacion aportacion) {
        if (aportacion.estado.equals("clasificada")) {
            solicitud.aportaciones.registradas.add(aportacion);
            solicitud.documentacion.documentos.addAll(aportacion.documentos);
            solicitud.aportaciones.actual = new Aportacion();
            solicitud.save();
            aportacion.estado = "finalizada";
            aportacion.save();

            play.Logger.debug("Los documentos de la aportacion se movieron correctamente");
        }
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
