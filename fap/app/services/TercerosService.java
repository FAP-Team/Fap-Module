package services;

import java.util.List;

import models.Solicitante;

public interface TercerosService {
	
	public boolean isConfigured();
	
	public void mostrarInfoInyeccion();
	
	public Solicitante buscarTercerosDetalladosByNumeroIdentificacion(String numeroIdentificacion, String tipoIdentificacion) throws TercerosServiceException;
	
	public String crearTerceroMinimal(Solicitante tercero) throws TercerosServiceException;
	
}