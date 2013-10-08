package services.async;

import play.libs.F.Promise;
import models.Documento;
import models.ExpedientePlatino;
import models.JustificanteRegistro;
import models.Solicitante;
import services.RegistroServiceException;

public interface RegistroServiceAsync {
	
    public Promise<Boolean> isConfigured();
    
    public Promise<Integer> mostrarInfoInyeccion();

    public Promise<JustificanteRegistro> registrarEntrada(Solicitante solicitante, Documento documento, ExpedientePlatino expediente, String descripcion) throws RegistroServiceException;
    
	public Promise<JustificanteRegistro> registroDeSalida(Solicitante solicitante, Documento documento, ExpedientePlatino expediente, String descripcion) throws RegistroServiceException;
}
