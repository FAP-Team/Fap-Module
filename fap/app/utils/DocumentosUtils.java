package utils;

import java.util.ArrayList;
 
import models.TipoDocumento;
import models.Tramite;

import properties.FapProperties;
import verificacion.ObligatoriedadDocumentosFap;
 
 public class DocumentosUtils {

	/**
	 * Indica si el tipo de documento es múltiple mirando en todos los trámites del procedimiento. Con que haya uno de tipo multiple, ya asumirá que será multiple, da igual que hay otro de otro tramite que sea UNICO.
	 * 
	 * @param tipoUri
	 * @return
	 */
	static public boolean esTipoMultiple (String tipoUri) {
		TipoDocumento td = TipoDocumento.find("select td from TipoDocumento td where td.uri=? and td.cardinalidad=?", tipoUri, "MULTIPLE").first();
		if (td != null) {
			return true;
 		}
 		return false;
 	}
}