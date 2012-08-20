package services;

import platino.DatosRegistro;
import models.Documento;
import models.ExpedientePlatino;
import models.JustificanteRegistro;
import models.Solicitante;


public interface RegistroService {

    public boolean isConfigured();
    
    public void mostrarInfoInyeccion();

    @Deprecated
    public JustificanteRegistro registrarEntrada(Solicitante solicitante, Documento documento, ExpedientePlatino expediente) throws RegistroServiceException;
    public JustificanteRegistro registrarEntrada(Solicitante solicitante, Documento documento, ExpedientePlatino expediente, String descripcion) throws RegistroServiceException;
	public JustificanteRegistro registroDeSalida(Solicitante solicitante, Documento documento, ExpedientePlatino expediente, String descripcion) throws RegistroServiceException;
}
