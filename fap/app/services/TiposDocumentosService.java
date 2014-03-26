package services;

import models.TipoDocumento;


public interface TiposDocumentosService {
	
	public TipoDocumento getTipoDocumento(String uri) throws GestorDocumentalServiceException;

}
