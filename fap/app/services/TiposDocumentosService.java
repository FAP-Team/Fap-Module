package services;

import java.util.List;

import models.DefinicionMetadatos;
import models.TipoDocumento;


public interface TiposDocumentosService {
	
	public boolean isConfigured();
	
	public TipoDocumento getTipoDocumento(String uri) throws GestorDocumentalServiceException;
	
	public List<DefinicionMetadatos> getDefinicionesMetadatos(String uri);
}
