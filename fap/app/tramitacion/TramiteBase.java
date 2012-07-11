package tramitacion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityTransaction;

import org.joda.time.DateTime;

import controllers.fap.FirmaController;
import emails.Mails;

import messages.Messages;
import models.Aportacion;
import models.Documento;
import models.Firma;
import models.Firmante;
import models.Firmantes;
import models.JustificanteRegistro;
import models.Registro;
import models.SolicitudGenerica;

import platino.DatosRegistro;
import play.db.jpa.JPA;
import play.libs.Crypto;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import reports.Report;
import services.FirmaService;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.RegistroService;
import services.RegistroServiceException;

@InjectSupport
public abstract class TramiteBase {
    
    @Inject
    public static GestorDocumentalService gestorDocumentalService;
    
    @Inject
    public static RegistroService registroService;
    
    @Inject
    private static FirmaService firmaService;
    
    public SolicitudGenerica solicitud;
    
    public Registro registro;
    
    public TramiteBase(SolicitudGenerica solicitud){
        this.solicitud = solicitud;
        this.registro = getRegistro();
        inicializar();
    }
    
    /**
	 * Sobreescribir para inicializar la clase. Es invocado desde el constructor
	 */
    public void inicializar() {
    }
    
    public abstract Registro getRegistro();
    public abstract String getTipoRegistro();
    // Region Procedimientos y Funciones
 	public abstract String getBodyReport();
 	public abstract String getHeaderReport();
 	public abstract String getFooterReport();

 	public abstract String getMail(); // Cuando se registre un trámite
 	public abstract String getJustificanteRegistro();
 	
 	/**
	 * Sobreescribir para asignar la descripción del justificante.
	 * El resultado final será la descripción del justificante más el nombre de la aplicación
	 * @return
	 */
	public abstract String getDescripcionJustificante();
 	
 	public abstract String getTipoTramite();
 	
 	public SolicitudGenerica getSolicitud() {
		return this.solicitud;
	}
    
    public abstract List<Documento> getDocumentos();
        
    public void prepararFirmar(){
        if(registro.fasesRegistro.borrador){
            Messages.error("La solicitud ya está preparada");
        }
        
        validar();
        eliminarBorrador();
        eliminarOficial();
        File borrador = generarBorrador();
        File oficial = generarOficial();
        almacenarEnGestorDocumental(borrador, oficial);
        calcularFirmantes();
        avanzarFaseBorrador();
    }
    
    public void validar(){
        
    }
    
    /**
	 * Nombre del fichero del justificante
	 * @return el nombre del fichero para el justificante
	 */
	public abstract String getPrefijoJustificantePdf();
    
    public void eliminarBorrador(){
        if(!Messages.hasErrors()){
            // Borramos los documentos que se pudieron generar en una llamada previa al metodo, para no dejar basura en la BBDD
        	if ((registro.borrador != null) && (registro.borrador.uri != null)){
                Documento borradorOld = registro.borrador;
                registro.oficial = null; 
                registro.save();
                try{
                    gestorDocumentalService.deleteDocumento(borradorOld);
                }catch(Exception e){
                    play.Logger.error("Error eliminando borrador del gestor documental");
                };
            }
        }
    }

    public void eliminarOficial() {
        if(!Messages.hasErrors()){
            if((registro.oficial != null) && (registro.oficial.uri != null)){
                Documento oficialOld = registro.oficial;
                registro.oficial = null;
                registro.save();
                try {
                    gestorDocumentalService.deleteDocumento(oficialOld);
                }catch(Exception e){
                    play.Logger.error("Error eliminando documento oficial del gestor documental");
                };
            }
        }
    }
    
    public File generarBorrador(){
    	File borrador = null;
        borrador = new File (this.getBodyReport());
        if(!Messages.hasErrors()){
            try {
            	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", solicitud);
            	borrador = new Report(this.getBodyReport()).header(this.getHeaderReport()).footer(this.getFooterReport()).renderTmpFile(solicitud);
                registro.borrador = new Documento();
                registro.borrador.tipo = getTipoRegistro();
                registro.save();
            } catch (Exception ex2) {
                Messages.error("Error generando el documento borrador: "+ex2.getMessage());
            }
        }

        return borrador;
    }
    
    public File generarOficial(){
        File oficial = null;
        if(!Messages.hasErrors()){
            try {
            	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", solicitud);
            	oficial = new Report(this.getBodyReport()).header(this.getHeaderReport()).registroSize().renderTmpFile(solicitud);
                registro.oficial = new Documento();
                registro.oficial.tipo = getTipoRegistro();
                registro.save();
            } catch (Exception ex2) {
                Messages.error("Error generando el documento oficial");
            }
        }
        return oficial;
    }
    
    public void almacenarEnGestorDocumental(File borrador, File oficial){
        if(!Messages.hasErrors()){
            try {
                gestorDocumentalService.saveDocumentoTemporal(registro.borrador, borrador);
                gestorDocumentalService.saveDocumentoTemporal(registro.oficial, oficial);
            }catch(Exception e){
                Messages.error("Error almacenando documentos en el aed");
            }
        }
    }
    
    public void almacenarFirma(String firma, Documento documento, Firmante firmante) {
        try {
            gestorDocumentalService.agregarFirma(documento, new Firma(firma, firmante));
        } catch (Exception e) {
            Messages.error("Error guardando la firma del documento");
        }
    }
    
    public void calcularFirmantes(){
        if(!Messages.hasErrors()){
            registro.firmantes = Firmantes.calcularFirmanteFromSolicitante(solicitud.solicitante);
        }
    }
    
    public void avanzarFaseBorrador(){
        if(!Messages.hasErrors()){
            registro.fasesRegistro.borrador = true;
            registro.save();
        }
    }
   
    /**
     * Reinicia las fases del registro y lo salva
     */
    public void deshacer() {
        registro.fasesRegistro.reiniciar();
        registro.fasesRegistro.save();
    }
    
    public void firmar(String firma){
        if(registro.fasesRegistro.borrador && !registro.fasesRegistro.firmada){
            String identificadorFirmante = FirmaController.getIdentificacionFromFirma(firma);
            if(registro.firmantes.containsFirmanteConIdentificador(identificadorFirmante)){
            	Firmante firmante = firmaService.getFirmante(firma, registro.oficial);
            	firmante.fechaFirma = new DateTime();
                almacenarFirma(firma, registro.oficial, firmante);
                firmante.save();
                if(registro.firmantes.hanFirmadoTodos()){
                    avanzarFaseFirmada();
                }
            }else{
                Messages.error("El certificado no se corresponde con uno que debe firmar la solicitud");
            }
        }
    }
    
    public void avanzarFaseFirmada(){
        if(!Messages.hasErrors()){
            registro.fasesRegistro.firmada = true;
        }
    }
    
    
    /**
	 * Sobreescribir para guardar el tipo de trámite específico
	 */
	public abstract void guardar();
	
	public abstract void validarReglasConMensajes();

    /**
	 * Sobreescribir para registrar el tipo de trámite específico 
	 * @throws RegistroException
	 */
	public void registrar() throws RegistroServiceException {
		validarReglasConMensajes();
		EntityTransaction tx = JPA.em().getTransaction();
		tx.commit();
		//Registra la solicitud
		if (!Messages.hasErrors()){
			if (!registro.fasesRegistro.registro && registro.fasesRegistro.firmada) {
				try {
					tx.begin();
					//Registra la solicitud
					JustificanteRegistro justificante = registroService.registrarEntrada(this.solicitud.solicitante, registro.oficial, this.solicitud.expedientePlatino, null);
					play.Logger.info("Se ha registrado la solicitud %s en platino", solicitud.id);
					tx.commit();
					registro.refresh();
					tx.begin();
					//Almacena la información de registro
					registro.informacionRegistro.setDataFromJustificante(justificante);
					play.Logger.info("Almacenada la información del registro en la base de datos");

					//Guarda el justificante en el AED
					play.Logger.info("Se procede a guardar el justificante de la solicitud %s en el AED", solicitud.id);
					Documento documento = registro.justificante;
					documento.tipo = this.getJustificanteRegistro();
					documento.descripcion = "Justificante de registro de la solicitud";
					documento.save();
					gestorDocumentalService.saveDocumentoTemporal(documento, justificante.getDocumento().contenido.getInputStream(), this.getNombreFicheroPdf());
					play.Logger.info("Justificante Registro del trámite de '%s' almacenado en el AED", this.getTipoTramite());
					
					registro.fasesRegistro.registro = true;
					getRegistro().fasesRegistro.registro=true;
					
					registro.fasesRegistro.save();
					try {
						play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", solicitud);
						Mails.enviar(this.getMail(), solicitud);
					} catch (Exception e){
						play.Logger.error("Envío del Mail de registro del trámite fallido "+this.getMail());
					}
					play.Logger.info("Correo Registro del trámtite de '%s' enviado", this.getTipoTramite());				


					// Establecemos las fechas de registro para todos los documentos de la solicitud
					List<Documento> documentos = new ArrayList<Documento>();
					documentos.addAll(this.getDocumentos());
					documentos.add(registro.justificante);
					documentos.add(registro.oficial);
					for (Documento doc: documentos) {
						if (doc.fechaRegistro == null) {
							doc.fechaRegistro = registro.informacionRegistro.fechaRegistro;
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
				play.Logger.debug("El trámite de '%s' de la solicitud %s ya está registrada", this.getTipoTramite(), this.solicitud.id);
			}
			registro.refresh();
			//Crea el expediente en el AED
			if(!getRegistro().fasesRegistro.expedienteAed){
				tx.begin();
				try {
					gestorDocumentalService.crearExpediente(solicitud);
				} catch (GestorDocumentalServiceException e) {
					Messages.error("Error al crear el expediente");
					throw new RegistroServiceException("Error al crear el expediente");
				}
				getRegistro().fasesRegistro.expedienteAed = true;
				getRegistro().fasesRegistro.save();
				tx.commit();
			}else{
				play.Logger.debug("El expediente del aed para la solicitud %s ya está creado", solicitud.id);
			}
			registro.refresh();
			
			//Ahora el estado de la solicitud se cambia después de registrar.
			
			//Clasifica los documentos en el AED
			if (!registro.fasesRegistro.clasificarAed && registro.fasesRegistro.registro) {
				//Clasifica los documentos sin registro
				tx.begin();
				List<Documento> documentos = new ArrayList<Documento>();
				documentos.add(registro.justificante);
				try {
					gestorDocumentalService.clasificarDocumentos(this.solicitud, documentos);
				} catch (GestorDocumentalServiceException e){
					Messages.error("Algunos documentos sin registro del trámite de '" + this.getTipoTramite() + "' no pudieron ser clasificados correctamente");
					throw new RegistroServiceException("Error al clasificar documentos sin registros");
				}
	
				if (!Messages.hasErrors()){
					//Clasifica los documentos con registro de entrada
					List<Documento> documentosRegistrados = new ArrayList<Documento>();
					documentosRegistrados.addAll(this.getDocumentos());
					documentosRegistrados.add(registro.oficial);
					try {
						gestorDocumentalService.clasificarDocumentos(this.solicitud, documentosRegistrados, registro.informacionRegistro);
						registro.fasesRegistro.clasificarAed = true;
						registro.fasesRegistro.save();
						play.Logger.info("Se clasificaron todos los documentos del trámite de '%s'", this.getTipoTramite());
					} catch (GestorDocumentalServiceException e){
						Messages.error("Algunos documentos con registro de entrada del trámite de '" + this.getTipoTramite() + "' no pudieron ser clasificados correctamente");
						throw new RegistroServiceException("Error al clasificar documentos con registros");
					}
				}
				tx.commit();
			} else {
				play.Logger.debug("Ya están clasificados todos los documentos del trámite de '%s' de la solicitud %s", this.getTipoTramite(), this.solicitud.id);
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
				play.Logger.debug("Los documentos del trámite de '%s' se movieron correctamente", this.getTipoTramite());
				tx.commit();
			}
			
		}
		tx.begin();
	}
	
	/**
	 * Mueve el trámite actual a la colección de trámites registrados
	 */
	public void moverRegistradas() {
	}
	
	/**
	 * Prepara un nuevo trámite y lo añade a la variable actual
	 */
	public void prepararNuevo() {
		
	}
	
	/**
	 * Obtiene el nombre del justificante del fichero pdf a partir del prefijo del justificante y
	 * del id de la solicitud más la extensión del fichero
	 * @return
	 */
	private final String getNombreFicheroPdf() {
		return this.getPrefijoJustificantePdf() + this.solicitud.id + ".pdf";
	}
	
	/**
	 * Crea el expediente del Aed
	 */
	public abstract void crearExpedienteAed();

	/**
	 * Crea el expediente en Platino
	 */
	public abstract void crearExpedientePlatino() throws RegistroServiceException;

	/**
	 * Añadir los documentos a la solicitud
	 */
	public abstract void anadirDocumentosSolicitud();
	
	public void cambiarEstadoSolicitud() {}

    
}

