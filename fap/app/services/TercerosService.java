package services;

import java.util.List;

import models.Agente;
import models.Solicitante;

public interface TercerosService {
	
	public boolean isConfigured();
	
	public void mostrarInfoInyeccion();
	
	public Solicitante buscarTercerosDetalladosByNumeroIdentificacion(String numeroIdentificacion, String tipoIdentificacion) throws TercerosServiceException;
	
	public String crearTerceroMinimal(Solicitante tercero) throws TercerosServiceException;
	
	public Agente buscarTercerosAgenteByNumeroIdentificacion (String numeroIdentificacion, String tipoIdentificacion) throws TercerosServiceException;
	
}