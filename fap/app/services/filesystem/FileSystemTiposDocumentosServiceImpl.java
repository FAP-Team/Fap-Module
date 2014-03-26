package services.filesystem;

import java.util.ArrayList;
import java.util.List;

import org.xhtmlrenderer.css.parser.property.PrimitivePropertyBuilders.Src;

import models.DefinicionMetadatos;
import models.TipoDocumento;
import services.GestorDocumentalServiceException;
import services.TiposDocumentosService;

public class FileSystemTiposDocumentosServiceImpl implements
		TiposDocumentosService {

	@Override
	public TipoDocumento getTipoDocumento(String uri)
			throws GestorDocumentalServiceException {
		if(uri == null) {
			throw new NullPointerException("La uri no puede ser null");
		}
		return TipoDocumento.find("byUri", uri).first();
	}

	@Override
	public List<DefinicionMetadatos> getDefinicionesMetadatos(String uri) {
		if(uri == null) {
			throw new NullPointerException("La uri no puede ser null");
		}
		
		TipoDocumento doc = TipoDocumento.find("byUri", uri).first();
		List<DefinicionMetadatos> lista = new ArrayList<DefinicionMetadatos>();
		if (doc != null) {
			lista = doc.definicionMetadatos;
		}
		return lista;
	}

	
	

}
