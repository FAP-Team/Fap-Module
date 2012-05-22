package tramitacion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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
    protected static GestorDocumentalService gestorDocumentalService;
    
    @Inject
    protected static RegistroService registroService;
    
    @Inject
    private static FirmaService firmaService;
    
    protected SolicitudGenerica solicitud;
    
    protected Registro registro;
    
    public TramiteBase(SolicitudGenerica solicitud){
        this.solicitud = solicitud;
        this.registro = getRegistro();
        inicializar();
    }
    
    /**
	 * Sobreescribir para inicializar la clase. Es invocado desde el constructor
	 */
    protected void inicializar() {
    }
    
    protected abstract Registro getRegistro();
    protected abstract String getTipoRegistro();
    // Region Procedimientos y Funciones
 	protected abstract String getBodyReport();
 	protected abstract String getHeaderReport();
 	protected abstract String getFooterReport();

 	protected abstract String getMail(); // Cuando se registre un trámite
 	protected abstract String getJustificanteRegistro();
 	
 	/**
	 * Sobreescribir para asignar la descripción del justificante.
	 * El resultado final será la descripción del justificante más el nombre de la aplicación
	 * @return
	 */
	protected abstract String getDescripcionJustificante();
 	
 	public abstract String getTipoTramite();
 	
 	public SolicitudGenerica getSolicitud() {
		return this.solicitud;
	}
    
    protected abstract List<Documento> getDocumentos();
        
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
    
    protected void validar(){
        
    }
    
    /**
	 * Nombre del fichero del justificante
	 * @return el nombre del fichero para el justificante
	 */
	protected abstract String getPrefijoJustificantePdf();
    
    protected void eliminarBorrador(){
        if(!Messages.hasErrors()){
            // Borramos los documentos que se pudieron generar en una llamada previa al metodo, para no dejar basura en la BBDD
            if(registro.borrador != null){
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

    protected void eliminarOficial() {
        if(!Messages.hasErrors()){
            if(registro.oficial != null){
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
    
    protected File generarBorrador(){
        File borrador = null;
        if(!Messages.hasErrors()){
            try {
            	borrador = new Report(this.getBodyReport()).header(this.getHeaderReport()).footer(this.getFooterReport()).renderTmpFile(this.solicitud);
                registro.borrador = new Documento();
                registro.borrador.tipo = getTipoRegistro();
            } catch (Exception ex2) {
                Messages.error("Error generando el documento borrador");
            }
        }
        return borrador;
    }
    
    protected File generarOficial(){
        File oficial = null;
        if(!Messages.hasErrors()){
            try {
            	oficial = new Report(this.getBodyReport()).header(this.getHeaderReport()).registroSize().renderTmpFile(this.solicitud);
                registro.oficial = new Documento();
                registro.oficial.tipo = getTipoRegistro();
            } catch (Exception ex2) {
                Messages.error("Error generando el documento borrador");
            }
        }
        return oficial;
    }
    
    protected void almacenarEnGestorDocumental(File borrador, File oficial){
        if(!Messages.hasErrors()){
            try {
                gestorDocumentalService.saveDocumentoTemporal(registro.borrador, borrador);
                gestorDocumentalService.saveDocumentoTemporal(registro.oficial, oficial);
            }catch(Exception e){
                Messages.error("Error almacenando documentos en el aed");
            }
        }
    }
    
    protected void almacenarFirma(String firma, Documento documento, Firmante firmante) {
        try {
            gestorDocumentalService.agregarFirma(documento, new Firma(firma, firmante));
        } catch (Exception e) {
            Messages.error("Error guardando la firma del documento");
        }
    }
    
    protected void calcularFirmantes(){
        if(!Messages.hasErrors()){
            registro.firmantes = Firmantes.calcularFirmanteFromSolicitante(solicitud.solicitante);
        }
    }
    
    protected void avanzarFaseBorrador(){
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
    
    protected void avanzarFaseFirmada(){
        if(!Messages.hasErrors()){
            registro.fasesRegistro.firmada = true;
        }
    }
    
    
    /**
	 * Sobreescribir para guardar el tipo de trámite específico
	 */
	protected abstract void guardar();
	
	public void validarReglasConMensajes() {}

    /**
	 * Sobreescribir para registrar el tipo de trámite específico 
	 * @throws RegistroException
	 */
	protected void registrar() throws RegistroServiceException {
		//Registra la solicitud
		if (!registro.fasesRegistro.registro && registro.fasesRegistro.firmada) {
			try {
				//Registra la solicitud
				JustificanteRegistro justificante = registroService.registrarEntrada(this.solicitud.solicitante, registro.oficial, this.solicitud.expedientePlatino, null);
				play.Logger.info("Se ha registrado la solicitud %s en platino", solicitud.id);

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
				registro.fasesRegistro.save();
				Mails.enviar(this.getMail(), this.solicitud);
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

			} catch (Exception e) {
				Messages.error("Error al registrar de entrada la solicitud");
				throw new RegistroServiceException("Error al obtener el justificante del registro de entrada");
			}
		} else {
			play.Logger.debug("El trámite de '%s' de la solicitud %s ya está registrada", this.getTipoTramite(), this.solicitud.id);
		}

		//Clasifica los documentos en el AED
		if (!registro.fasesRegistro.clasificarAed && registro.fasesRegistro.registro) {
			//Clasifica los documentos sin registro
			List<Documento> documentos = new ArrayList<Documento>();
			documentos.add(registro.justificante);
			try {
				gestorDocumentalService.clasificarDocumentos(this.solicitud, documentos);
			} catch (GestorDocumentalServiceException e){
				Messages.error("Algunos documentos sin registro del trámite de '" + this.getTipoTramite() + "' no pudieron ser clasificados correctamente");
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
				}
			}
		} else {
			play.Logger.debug("Ya están clasificados todos los documentos del trámite de '%s' de la solicitud %s", this.getTipoTramite(), this.solicitud.id);
		}

		//Añade los documentos a la lista de documentos de la solicitud
		if (registro.fasesRegistro.clasificarAed){
			this.moverRegistradas();
			this.solicitud.documentacion.documentos.addAll(this.getDocumentos());
			this.prepararNuevo();
			solicitud.save();

			play.Logger.debug("Los documentos del trámite de '%s' se movieron correctamente", this.getTipoTramite());
		}
	}
	
	/**
	 * Mueve el trámite actual a la colección de trámites registrados
	 */
	protected void moverRegistradas() {
	}
	
	/**
	 * Prepara un nuevo trámite y lo añade a la variable actual
	 */
	protected void prepararNuevo() {
		
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
	protected abstract void crearExpedienteAed();

	/**
	 * Crea el expediente en Platino
	 */
	protected abstract void crearExpedientePlatino() throws RegistroServiceException;

	/**
	 * Añadir los documentos a la solicitud
	 */
	protected abstract void anadirDocumentosSolicitud();
	
	protected void cambiarEstadoSolicitud() {}

    
}

