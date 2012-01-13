package services;

import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.TiposDocumentosExcepcion;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;

public interface TiposDocumentosService extends WSService {
	
	public String getVersion() throws Exception;
	
	public TipoDocumento getTipoDocumento(String uri) throws TiposDocumentosExcepcion;
	
}
