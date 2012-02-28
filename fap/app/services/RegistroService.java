package services;

import models.Documento;
import models.ExpedientePlatino;
import models.JustificanteRegistro;
import models.Solicitante;


public interface RegistroService {

    public boolean isConfigured();

    public JustificanteRegistro registrarEntrada(Solicitante solicitante, Documento documento, ExpedientePlatino expediente) throws RegistroServiceException;
	
}
