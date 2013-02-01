package controllers.fap;

import enumerado.fap.gen.TipoCrearExpedienteAedEnum;
import models.SemillaExpediente;
import properties.FapProperties;

public class IdentificadorExpedientesController extends InvokeClassController {

	/**
	 * Devuelve el identificador de un nuevo expediente, según el tipo de expediente.
	 * 
	 * @param tipoExpediente
	 * @return
	 */
	public static String getNuevoIdExpediente (String tipoExpediente) {
		String idAed = "";
		if (tipoExpediente == null || tipoExpediente.isEmpty() || tipoExpediente.equalsIgnoreCase(TipoCrearExpedienteAedEnum.solicitud.name())) {
			SemillaExpediente semilla = new SemillaExpediente();
			semilla.save();
			Long id = (Long) semilla.id;
			java.text.NumberFormat formatter = new java.text.DecimalFormat("0000");
			String prefijo = FapProperties.get("fap.aed.expediente.prefijo");
			idAed = prefijo + formatter.format(id);
		} else if (tipoExpediente.equalsIgnoreCase(TipoCrearExpedienteAedEnum.convocatoria.name())) {
			idAed = FapProperties.get("fap.aed.expediente.convocatoria");
		}
		return idAed;
	}
	
}
