package services.filesystem;

import java.util.ArrayList;
import java.util.Arrays;
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
			//implementación falsa del servicio
            play.Logger.info("FileSystemTiposDocumentosService: nueva definición para %s", uri);
            DefinicionMetadatos def = new DefinicionMetadatos();
            def.nombre = "Metadato1";
            def.descripcion = "Definicion generada en FileSystemTiposDocumentosServiceImpl";
            def.autogenerado = true;
            def.valoresPosibles.addAll(Arrays.asList("valor1","valor2"));
            lista.add(def);
            def = new DefinicionMetadatos();
            def.nombre = "Metadato2";
            def.descripcion = "Definicion generada en FileSystemTiposDocumentosServiceImpl";
            def.valoresPosibles.addAll(Arrays.asList("valor21","valor22"));
            lista.add(def);
        }
		return lista;
	}

	@Override
	public boolean isConfigured() {
		return true;
	}

	
	

}
