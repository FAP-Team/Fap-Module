package tramitacion;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityTransaction;

import config.InjectorConfig;
import controllers.fap.VerificacionFapController;
import emails.Mails;
import enumerado.fap.gen.SeleccionEnum;
import play.db.jpa.JPA;
import properties.FapProperties;
import services.GestorDocumentalServiceException;
import services.RegistroServiceException;
import services.TercerosService;
import services.VerificarDocumentacionService;
import services.platino.PlatinoGestorDocumentalService;
import messages.Messages;
import models.DeclaracionSubvenciones;
import models.Documento;
import models.DocumentoExterno;
import models.JustificanteRegistro;
import models.Registro;
import models.Solicitante;
import models.SolicitudGenerica;
import models.SubvencionFap;

public class TramiteAceptacionRenuncia extends TramiteBase {
	
	private final static String TIPO_TRAMITE_ACEPTACION = "Aceptacion";
	private final static String TIPO_TRAMITE_RENUNCIA = "Renuncia";
	private final static String NOMBRE_TRAMITE_ACEPTACION = FapProperties.get("fap.aed.procedimientos.tramiteaceptacionrenuncia.aceptacion.nombre"); 
	private final static String NOMBRE_TRAMITE_RENUNCIA = FapProperties.get("fap.aed.procedimientos.tramiteaceptacionrenuncia.renuncia.nombre");
	private final static String TIPO_REGISTRO_ACEPTACION = FapProperties.get("fap.aed.tiposdocumentos.aceptacionrenuncia.aceptacion");
	private final static String TIPO_REGISTRO_RENUNCIA = FapProperties.get("fap.aed.tiposdocumentos.aceptacionrenuncia.renuncia");
	private final static String BODY_REPORT_ACEPTACION = "reports/aceptacion.html";
	private final static String BODY_REPORT_RENUNCIA = "reports/renuncia.html";
	private final static String HEADER_REPORT = "reports/header.html";
	private final static String FOOTER_REPORT = "reports/footer-borrador.html";
	private final static String MAILACEPTACION = FapProperties.get("fap.tramitacion.aceptacion.identificadoremail");
	private final static String MAILRENUNCIA = FapProperties.get("fap.tramitacion.renuncia.identificadoremail");
	private final static String JUSTIFICANTE_ACEPTACION = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroAceptacionRenuncia.aceptacion");
	private final static String JUSTIFICANTE_RENUNCIA = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroAceptacionRenuncia.renuncia");
	private PlatinoGestorDocumentalService platinoGestorDocumentalService;
	
	public TramiteAceptacionRenuncia(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	/**
	 * Retorna el registro del trámite aceptación
	 */
	@Override
	public Registro getRegistro() {
		return this.solicitud.aceptarRenunciar.registro;
	}

	@Override
	public String getTipoRegistro() {
		if ((solicitud.aceptarRenunciar.seleccion != null) && (solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase(SeleccionEnum.acepta.name())))
			return TramiteAceptacionRenuncia.TIPO_REGISTRO_ACEPTACION;
		else
			return TramiteAceptacionRenuncia.TIPO_REGISTRO_RENUNCIA;
	}

	@Override
	public String getBodyReport() {
		if ((solicitud.aceptarRenunciar.seleccion != null) && (solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase(SeleccionEnum.acepta.name())))
			return TramiteAceptacionRenuncia.BODY_REPORT_ACEPTACION;
		else
			return TramiteAceptacionRenuncia.BODY_REPORT_RENUNCIA;
	}

	@Override
	public String getHeaderReport() {
		return TramiteAceptacionRenuncia.HEADER_REPORT;
	}

	@Override
	public String getFooterReport() {
		return TramiteAceptacionRenuncia.FOOTER_REPORT;
	}

	@Override
	public String getMail() {
		if ((solicitud.aceptarRenunciar.seleccion != null) && (solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase(SeleccionEnum.acepta.name())))
			return TramiteAceptacionRenuncia.MAILACEPTACION;
		else
			return TramiteAceptacionRenuncia.MAILRENUNCIA;
	}

	@Override
	public String getJustificanteRegistro() {
		if ((solicitud.aceptarRenunciar.seleccion != null) && (solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase(SeleccionEnum.acepta.name())))
			return TramiteAceptacionRenuncia.JUSTIFICANTE_ACEPTACION;
		else
			return TramiteAceptacionRenuncia.JUSTIFICANTE_RENUNCIA;
	}

	@Override
	public String getDescripcionJustificante() {
		if ((solicitud.aceptarRenunciar.seleccion != null) && (solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase(SeleccionEnum.acepta.name())))
			return TramiteAceptacionRenuncia.TIPO_TRAMITE_ACEPTACION;
		else
			return TramiteAceptacionRenuncia.TIPO_REGISTRO_RENUNCIA;
	}

	@Override
	public String getTipoTramite() {
		if ((solicitud.aceptarRenunciar.seleccion != null) && (solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase(SeleccionEnum.acepta.name())))
			return TramiteAceptacionRenuncia.TIPO_TRAMITE_ACEPTACION;
		else
			return TramiteAceptacionRenuncia.TIPO_TRAMITE_RENUNCIA;
	}

	/**
	 * Retorna los documentos del trámite aceptación
	 */
	@Override
	public List<Documento> getDocumentos() {
		return this.solicitud.aceptarRenunciar.documentos;
	}

	/**
	 * Nombre del fichero del justificante para el trámite aceptación
	 */
	@Override
	public String getPrefijoJustificantePdf() {
		return FapProperties.get("fap.tramitacion.aceptacion.prefijojustificantepdf");
	}
	
	public String getNombreTramite() {
		if ((solicitud.aceptarRenunciar.seleccion != null) && (solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase(SeleccionEnum.acepta.name())))
			return TramiteAceptacionRenuncia.NOMBRE_TRAMITE_ACEPTACION;
		else
			return TramiteAceptacionRenuncia.NOMBRE_TRAMITE_RENUNCIA;
	}

	/**
	 * Crea el expediente en el AED
	 */
	@Override
	public void crearExpedienteAed() {
		if (!this.solicitud.aceptarRenunciar.registro.fasesRegistro.expedienteAed){
			try {
				gestorDocumentalService.crearExpediente(this.solicitud);
				this.solicitud.aceptarRenunciar.registro.fasesRegistro.expedienteAed = true;
				this.solicitud.aceptarRenunciar.registro.fasesRegistro.save();
			} catch (GestorDocumentalServiceException e) {
				play.Logger.debug("Error creando el expediente en el Gestor Documental", e.getMessage());
				Messages.error("Error creando el expediente en el Gestor Documental");
			}
		}
		else {
			play.Logger.debug("El expediente del aed para la solicitud %s ya está creado", this.solicitud.aceptarRenunciar.id);
		}

		//Cambiamos el estado de la solicitud
		if (this.solicitud.aceptarRenunciar.motivoRenuncia != null) {
			if (!this.solicitud.estado.equals("aceptadoRSLPROV")) {
				this.solicitud.estado = "aceptadoRSLPROV";
				this.solicitud.save();
				Mails.enviar(this.getMail(), this.solicitud);
			}
		}
		else {
			if (!this.solicitud.estado.equals("renunciadoRSLPROV")) {
				this.solicitud.estado = "renunciadoRSLPROV";
				this.solicitud.save();
				Mails.enviar(this.getMail(), this.solicitud);
			}
		}
		
	}

	/**
	 * Crea el expediente en el archivo electrónico de platino
	 */
	@Override
	public void crearExpedientePlatino() throws RegistroServiceException {
		
		if (!this.solicitud.aceptarRenunciar.registro.fasesRegistro.expedientePlatino){
			try {
				platinoGestorDocumentalService.crearExpediente(this.solicitud.expedientePlatino);

				this.solicitud.aceptarRenunciar.registro.fasesRegistro.expedientePlatino = true;
				this.solicitud.aceptarRenunciar.registro.fasesRegistro.save();
			} catch (Exception e) {
				Messages.error("Error creando expediente en el gestor documental de platino");
				throw new RegistroServiceException("Error creando expediente en el gestor documental de platino");
			}
		}
		else {
			play.Logger.debug("El expediente de platino para la solicitud %s ya está creado", solicitud.id);
		}
	}

	/**
	 *
	 */
	@Override
	public void validarReglasConMensajes() {
		this.validarDocumentacion();
	}

	/**
	 * Validar los documentos condicionados automaticamente
	 */
	public void validarDocumentacion() {
		
		if (solicitud.aceptarRenunciar.seleccion.equalsIgnoreCase("aceptar")) {
			VerificarDocumentacionService verificar = new VerificarDocumentacionService("solicitud", this.getDocumentos(), this.getDocumentosExternos());
			List<String> condicionadosAutomaticosNoAportados;
			try {
				condicionadosAutomaticosNoAportados = VerificacionFapController.getDocumentosNoAportadosCondicionadosAutomaticos(getNombreTramite(), solicitud.aceptarRenunciar.id);
				verificar.preparaPresentacionTramite(condicionadosAutomaticosNoAportados);
			} catch (Throwable e) {
				play.Logger.debug("Error validando la documentacion aportada", e.getMessage());
				Messages.error("Error validando la documentacion aportada");
			}
		}
		
	}

	/**
	 * No realiza cambios de estado
	 */
	@Override
	public
	final void cambiarEstadoSolicitud() {
		
	}

	@Override
	public List<DocumentoExterno> getDocumentosExternos() {
		// TODO Auto-generated method stub
		return solicitud.aceptarRenunciar.documentosExternos;
	}

	@Override
	public boolean hanFirmadoTodos() {
		// TODO Auto-generated method stub
		return registro.firmantes.hanFirmadoTodos();
	}
	
    @Override
    public void validar() {
        super.validar();
        comprobarDocumentosAnexos();
    }

    private void comprobarDocumentosAnexos() {
        VerificarDocumentacionService.comprobarFirmasDocumentos(this.getDocumentos());
    }
    
    @Override
    public void registrar() throws RegistroServiceException {
          EntityTransaction tx = JPA.em().getTransaction();
          tx.commit();
          //Registra la solicitud
          if (!Messages.hasErrors()){
                 if (!registro.fasesRegistro.registro && registro.fasesRegistro.firmada) {
                        try {
                           play.Logger.info("Iniciando el proceso de registro de la solicitud: "+this.solicitud.id);
                           tx.begin();
                           //Registra la solicitud
                           JustificanteRegistro justificante = registroService.registrarEntrada(this.solicitud.solicitante, registro.oficial, this.solicitud.expedientePlatino, null);
                           play.Logger.info("Se ha registrado la solicitud %s en platino (Algo relativo a ella)", solicitud.id);
                           tx.commit();
                           registro.refresh();
                           tx.begin();
                           //Almacena la información de registro
                           registro.informacionRegistro.setDataFromJustificante(justificante);
                           play.Logger.info("Almacenada la información del registro en la base de datos");

                           //Guarda el justificante en el AED
                           play.Logger.info("Se procede a guardar el justificante de la solicitud %s en el Gestor Documental", solicitud.id);
                           Documento documento = registro.justificante;
                           documento.tipo = this.getJustificanteRegistro();
                           documento.descripcion = this.getDescripcionJustificante();
                           documento.save();
                           gestorDocumentalService.saveDocumentoTemporal(documento, justificante.getDocumento().contenido.getInputStream(), this.getNombreFicheroPdf());
                           play.Logger.info("Justificante Registro del trámite de '%s' almacenado en el Gestor Documental", this.getTipoTramite());

                           registro.fasesRegistro.registro = true;
                           getRegistro().fasesRegistro.registro=true;

                           registro.fasesRegistro.save();

                           // Establecemos las fechas de registro para todos los documentos de la solicitud
                           List<Documento> documentos = new ArrayList<Documento>();
                           documentos.addAll(this.getDocumentos());
                           documentos.add(registro.justificante);
                           documentos.add(registro.oficial);
                           for (Documento doc: documentos) {
                                 if (doc.fechaRegistro == null) {
                                        doc.fechaRegistro = registro.informacionRegistro.fechaRegistro;
                                        doc.save();
                                 }
                           }
                           play.Logger.info("Fechas de registro establecidas a " + this.getRegistro().informacionRegistro.fechaRegistro);
                           tx.commit();
                    } catch (Exception e) {
                           Messages.error("Error al registrar de entrada la solicitud");
                           play.Logger.error("Error al registrar de entrada la solicitud: "+e.getMessage());
                           throw new RegistroServiceException("Error al obtener el justificante del registro de entrada");
                    }
                 } else {
                        play.Logger.info("El trámite de '%s' de la solicitud %s ya está registrada", this.getTipoTramite(), this.solicitud.id);
                 }
                 registro.refresh();
                 //Crea el expediente en el Gestor Documental
                 tx.begin();
                 crearExpediente();
                 tx.commit();

                 //Ahora el estado de la solicitud se cambia después de registrar.

                 //Clasifica los documentos en el Gestor Documental
                 if (!registro.fasesRegistro.clasificarAed && registro.fasesRegistro.registro) {
                        play.Logger.info("Iniciando el proceso de clasificación de los documentos de la solicitud: "+this.solicitud.id);
                        //Clasifica los documentos sin registro
                        tx.begin();
                        List<Documento> documentos = new ArrayList<Documento>();
                        documentos.add(registro.justificante);
                        try {
                               play.Logger.info("Clasificando documentos sin registro de la solicitud: "+this.solicitud.id);
                               gestorDocumentalService.clasificarDocumentos(this.solicitud, documentos);
                        } catch (GestorDocumentalServiceException e){
                               play.Logger.fatal("No se clasificaron algunos documentos sin registro: "+e.getMessage());
                               Messages.error("Algunos documentos sin registro del trámite de '" + this.getTipoTramite() + "' no pudieron ser clasificados correctamente");
                               throw new RegistroServiceException("Error al clasificar documentos sin registros");
                        }

                        if (!Messages.hasErrors()){
                               //Clasifica los documentos con registro de entrada
                               play.Logger.info("Clasificando documentos con registro de la solicitud: "+this.solicitud.id);
                               List<Documento> documentosRegistrados = new ArrayList<Documento>();
                               documentosRegistrados.addAll(this.getDocumentos());
                               documentosRegistrados.add(registro.oficial);
                               try {
                                     gestorDocumentalService.clasificarDocumentos(this.solicitud, documentosRegistrados, registro.informacionRegistro);
                                     registro.fasesRegistro.clasificarAed = true;
                                     registro.fasesRegistro.save();
                                     play.Logger.info("Se clasificaron todos los documentos del trámite de '%s'", this.getTipoTramite());
                               } catch (GestorDocumentalServiceException e){
                                     play.Logger.fatal("No se clasificaron algunos documentos con registro de entrada: "+e.getMessage());
                                     Messages.error("Algunos documentos con registro de entrada del trámite de '" + this.getTipoTramite() + "' no pudieron ser clasificados correctamente");
                                     throw new RegistroServiceException("Error al clasificar documentos con registros");
                               }

                        }
                        tx.commit();
                 } else {
                        play.Logger.info("Ya están clasificados todos los documentos del trámite de '%s' de la solicitud %s", this.getTipoTramite(), this.solicitud.id);
                 }
                 registro.refresh();
                 //Añade los documentos a la lista de documentos de la solicitud
                 if (registro.fasesRegistro.clasificarAed){
                        tx.begin();
                        this.moverRegistradas();
                        for (Documento doc: this.getDocumentos()) {
                               if (!this.solicitud.documentacion.documentos.contains(doc))
                                     this.solicitud.documentacion.documentos.add(doc);
                        }
                        this.prepararNuevo();
                        solicitud.save();
                        play.Logger.info("Los documentos del trámite de '%s' se movieron correctamente", this.getTipoTramite());


                        // Creamos el nuevo tercero, si no existe
                        if ((FapProperties.getBoolean("fap.platino.tercero.activa")) && ((solicitud.solicitante.uriTerceros == null) || (solicitud.solicitante.uriTerceros.isEmpty()))) {
                               try {
                                     String tipoNumeroIdentificacion;
                                     if (solicitud.solicitante.isPersonaFisica()){
                                            tipoNumeroIdentificacion = solicitud.solicitante.fisica.nip.tipo;
                                     } else {
                                            tipoNumeroIdentificacion = "cif";
                                     }
                                     TercerosService tercerosService = InjectorConfig.getInjector().getInstance(TercerosService.class);
                                     Solicitante existeTercero = tercerosService.buscarTercerosDetalladosByNumeroIdentificacion(solicitud.solicitante.getNumeroId(), tipoNumeroIdentificacion);
                                     if (existeTercero == null){
                                            String uriTercero = tercerosService.crearTerceroMinimal(solicitud.solicitante);
                                            solicitud.solicitante.uriTerceros = uriTercero;
                                            solicitud.save();
                                     } else {
                                            String uriTercero = existeTercero.uriTerceros;
                                            solicitud.solicitante.uriTerceros = uriTercero;
                                            solicitud.save();
                                            play.Logger.warn("El Tercero ya existe en la BDD a Terceros de Platino: "+solicitud.solicitante.getNumeroId()+" - "+tipoNumeroIdentificacion+". Se ha seteado la uriTerceros a: "+uriTercero);
                                     }
                               } catch (Exception e){
                                     play.Logger.fatal("No se pudo crear el Tercero en Platino con id: "+solicitud.solicitante.getNumeroId()+" : "+e.getMessage());
                               }
                        }

                        //Salva las subvenciones a historico para persistir cambios de los gestores
                        try {
                               this.salvarDeclaracionSubvenciones(this.solicitud);
                        } catch (Exception e) {
                               e.printStackTrace();

                               play.Logger.fatal("No se guardó el histórico de subvenciones por: "+e.getMessage());
                               Messages.error("No se guardó el histórico de subvenciones en el trámite '" + this.getTipoTramite() + "' correctamente");
                        }

                        //Mandar el correo electronico
                        try {
                               play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", solicitud);
                               Mails.enviar(this.getMail(), solicitud);
                        } catch (Exception e){
                               play.Logger.error("Envío del Mail de registro del trámite fallido "+this.getMail()+": "+e.getMessage());
                        }
                        play.Logger.info("Correo Registro del trámite de '%s' enviado", this.getTipoTramite());



                        tx.commit();
                 }

          }
          tx.begin();
    }

   private static void salvarDeclaracionSubvenciones(SolicitudGenerica solicitud){
          //Copio subvenciones de la solicitud al histórico
          DeclaracionSubvenciones dclHist = new DeclaracionSubvenciones();
          dclHist.fecha=solicitud.aceptarRenunciar.registro.informacionRegistro.fechaRegistro;
          dclHist.nombreTramite="Aceptación";

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
                 dclHist.subvenciones.add(subNew);
          }
          dclHist.save();
          dclHist.refresh();
          solicitud.historicoDeclaracionSubvenciones.declaraciones.add(dclHist);

//        //Copio las subvenciones de aceptación en la solicitud
//        DeclaracionSubvenciones dclSol = new DeclaracionSubvenciones();

          //Borro todas las subvenciones de la solicitud
          solicitud.declaracionSubvenciones.subvenciones.clear();

          //Cambios:
          solicitud.declaracionSubvenciones.save();
          solicitud.declaracionSubvenciones.refresh();
          //Copio las subvenciones de aceptacion en declaraciones:
          for (SubvencionFap sub : solicitud.aceptarRenunciar.declaracionSubvenciones.subvenciones) {
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
                 solicitud.declaracionSubvenciones.subvenciones.add(subNew);
          }
          //dclNew.save();
          //dclNew.refresh();
          //solicitud.declaracionSubvenciones=dclNew;
          solicitud.declaracionSubvenciones.save();
          solicitud.save();
    }
}