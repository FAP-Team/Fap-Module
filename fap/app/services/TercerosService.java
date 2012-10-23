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
	
	public List<String> buscarTercero(String query) throws TercerosServiceException;
	
	public List<String> buscarTerceroByItem(TerceroMinimalItem tmi) throws TercerosServiceException;
	
	public List<TerceroListItem> buscarTercerosDetallados(String query) throws TercerosServiceException;
	
	public List<TerceroListItem> buscarTercerosDetalladosByItem(TerceroMinimalItem tmi) throws TercerosServiceException;
	
	public List<TerceroListItem> buscarTercerosDetalladosByItem(Solicitante solicitante) throws TercerosServiceException;
	
	public List<TerceroListItem> buscarTercerosDetalladosByNumeroIdentificacion(String numeroIdentificacion, String tipoIdentificacion) throws TercerosServiceException;
	
	public PageDataUriItem buscarTercerosPaginados(String query) throws TercerosServiceException;
	
	public TerceroItem consultarTercero(String uri) throws TercerosServiceException;
	
	public TerceroItem consultarTercero(TerceroMinimalItem tercero) throws TercerosServiceException;
	
	public TerceroItem consultarTercero(TerceroListItem tercero) throws TercerosServiceException;
	
	public ProvinciaItem recuperarProvincia(Long idProvincia);
	
	public PaisItem recuperarPais(Long idPais);
	
	public MunicipioItem recuperarMunicipio(Long idProvincia, Long idMunicipio);
	
	public IslaItem recuperarIsla(Long idIsla);
	
	public String crearTerceroMinimal(TerceroMinimalItem tercero) throws TercerosServiceException;
	
	public String crearTerceroMinimal(Solicitante tercero) throws TercerosServiceException;
	
}