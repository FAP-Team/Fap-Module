package services.filesystem;

import javax.inject.Inject;

import org.joda.time.DateTime;

import platino.DatosRegistro;
import play.libs.Codec;
import play.modules.guice.InjectSupport;

import models.Documento;
import models.ExpedientePlatino;
import models.JustificanteRegistro;
import models.Solicitante;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.RegistroService;
import services.RegistroServiceException;
import utils.BinaryResponse;

@InjectSupport
public class FileSystemRegistroService implements RegistroService {

    private final GestorDocumentalService gestorDocumentalService;

    @Inject
    public FileSystemRegistroService(GestorDocumentalService gestorDocumentalService) {
        this.gestorDocumentalService = gestorDocumentalService;
    }

    @Override
    public boolean isConfigured() {
        return true;
    }
    
    @Override
    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Registro ha sido inyectado con FileSystem y está operativo.");
		else
			play.Logger.info("El servicio de Registro ha sido inyectado con FileSystem y NO está operativo.");
    }
    
    @Override
    public JustificanteRegistro registrarEntrada(Solicitante solicitante, Documento documento,
            ExpedientePlatino expediente) throws RegistroServiceException {
    	return registrarEntrada(solicitante, documento, expediente, null);
    }
    /**
     * @param solicitante No se utiliza
     * @param documento Documento que se va a registrar
     * @param expediente No se utiliza
     * @return Justificante con la fecha actual, numero de registro aleatorio y el documento el mismo 
     *         que se quería registrar
     */
    @Override
    public JustificanteRegistro registrarEntrada(Solicitante solicitante, Documento documento,
            ExpedientePlatino expediente, String descripcion) throws RegistroServiceException {

        String numeroRegistro = "FileSystemRegistro - " + Codec.UUID();
        DateTime fechaRegistro = new DateTime();
        BinaryResponse binaryResponse = getDocumentoFromGestorDocumental(documento);
        String unidadOrganica = "fs";
        String numeroRegistroRegeneral = "FileSystemNRegistroGeneral - " + Codec.UUID();
        
        JustificanteRegistro justificante = new JustificanteRegistro(binaryResponse, fechaRegistro, unidadOrganica, numeroRegistro, numeroRegistroRegeneral);
        return justificante;
    }

    private BinaryResponse getDocumentoFromGestorDocumental(Documento documento) throws RegistroServiceException {
        try {
            return gestorDocumentalService.getDocumento(documento);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RegistroServiceException("Error recuperando el documento del gestor documental", e);
        }
    }

	@Override
	public JustificanteRegistro registroDeSalida(Solicitante solicitante,
			Documento documento, ExpedientePlatino expediente,
			String descripcion) throws RegistroServiceException {
			return new JustificanteRegistro(getDocumentoFromGestorDocumental(documento), new DateTime(), "fs", "FileSystemRegistro - " + Codec.UUID(), "FileSystemNRegistroGeneral - " + Codec.UUID());
	}

}
