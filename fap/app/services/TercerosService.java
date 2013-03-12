package services;

import java.util.List;

import models.Solicitante;
import models.Agente;

public interface TercerosService {
	
	public boolean isConfigured();
	
	public void mostrarInfoInyeccion();
	
	public Solicitante buscarTercerosDetalladosByNumeroIdentificacion(String numeroIdentificacion, String tipoIdentificacion) throws TercerosServiceException;
	
	public Agente buscarTercerosAgenteByNumeroIdentificacion(String numeroIdentificacion, String tipoIdentificacion) throws TercerosServiceException;
	
	public String crearTerceroMinimal(Solicitante tercero) throws TercerosServiceException;
	
}