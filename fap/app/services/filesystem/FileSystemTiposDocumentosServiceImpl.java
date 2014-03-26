package services.filesystem;

import org.xhtmlrenderer.css.parser.property.PrimitivePropertyBuilders.Src;

import models.TipoDocumento;
import services.GestorDocumentalServiceException;
import services.TiposDocumentosService;

public class FileSystemTiposDocumentosServiceImpl implements
		TiposDocumentosService {

	@Override
	public TipoDocumento getTipoDocumento(String uri)
			throws GestorDocumentalServiceException {
		return TipoDocumento.find("byUri", uri).first();
	}


}
