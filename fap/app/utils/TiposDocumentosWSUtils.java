package utils;

import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;

public class TiposDocumentosWSUtils {


	public static models.TipoDocumento convertTipoAed2TipoFap(TipoDocumento tipoAed) {
		models.TipoDocumento tipoFap = models.TipoDocumento.find("byUri", tipoAed.getUri()).first();
		if (tipoFap == null) {
			tipoFap = new models.TipoDocumento();
			tipoFap.uri = tipoAed.getUri();
			tipoFap.nombre = tipoAed.getDescripcion();
		}
		return tipoFap;
	}

	public static TipoDocumento convertTipoFap2Aed(models.TipoDocumento tipoFap) {
		TipoDocumento tipoAed = new TipoDocumento();
		tipoAed.setUri(tipoFap.uri);
		tipoAed.setDescripcion(tipoFap.nombre);
		return tipoAed;
	}

}
