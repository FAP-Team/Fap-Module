package services;

import java.util.List;

import models.Solicitante;

import es.gobcan.platino.servicios.localizaciones.IslaItem;
import es.gobcan.platino.servicios.localizaciones.MunicipioItem;
import es.gobcan.platino.servicios.localizaciones.PaisItem;
import es.gobcan.platino.servicios.localizaciones.ProvinciaItem;
import es.gobcan.platino.servicios.terceros.PageDataUriItem;
import es.gobcan.platino.servicios.terceros.TerceroItem;
import es.gobcan.platino.servicios.terceros.TerceroListItem;
import es.gobcan.platino.servicios.terceros.TerceroMinimalItem;

public interface TercerosService {
	
	public boolean isConfigured();
	
	public void mostrarInfoInyeccion();
	
	public Solicitante buscarTercerosDetalladosByNumeroIdentificacion(String numeroIdentificacion, String tipoIdentificacion) throws TercerosServiceException;
	
	public String crearTerceroMinimal(Solicitante tercero) throws TercerosServiceException;
	
}