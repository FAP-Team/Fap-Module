package controllers.fap;

import enumerado.fap.gen.TipoCrearExpedienteAedEnum;
import models.Convocatoria;
import models.SemillaExpediente;
import properties.FapProperties;

import java.util.Calendar;

public class IdentificadorExpedientesController extends InvokeClassController {

    private static final String ANUAL = "anual";
    private static final String FAP_AED_EXPEDIENTE_PREFIJO = "fap.aed.expediente.prefijo";

	/**
	 * Devuelve el identificador de un nuevo expediente, según el tipo de expediente.
	 * 
	 * @param tipoExpediente
	 * @return
	 */
	public static String getNuevoIdExpediente (String tipoExpediente) {
		String idAed = "";
		if (tipoExpediente == null || tipoExpediente.isEmpty() || tipoExpediente.equalsIgnoreCase(TipoCrearExpedienteAedEnum.solicitud.name())) {
			idAed = getPrefijoCompletoExpediente();
		} else if (tipoExpediente.equalsIgnoreCase(TipoCrearExpedienteAedEnum.convocatoria.name())) {
			idAed = FapProperties.get("fap.aed.expediente.convocatoria");
		}
		return idAed;
	}

    private static String getPrefijoCompletoExpediente() {
        String prefijo = FapProperties.get(FAP_AED_EXPEDIENTE_PREFIJO);
        String anyo = getPrefijoAnyo();
        String secuencial = getNumeroSecuencial();

        String prefijoCompleto = String.format("%s%s%s",prefijo, anyo, secuencial);
        return prefijoCompleto;
    }

    private static String getNumeroSecuencial() {
        SemillaExpediente semilla = new SemillaExpediente();
        semilla.save();
        Long id = (Long) semilla.getValorSemilla();
        java.text.NumberFormat formatter = new java.text.DecimalFormat("0000");
        return formatter.format(id);
    }

    private static String getPrefijoAnyo() {
        String prefijoAnyo = "";
        if(Convocatoria.esAnual()) {
            prefijoAnyo = getAnyo();
        }
        return prefijoAnyo;
    }

    private static String getAnyo() {
        Calendar now = Calendar.getInstance();
        return String.valueOf(now.get(Calendar.YEAR));
    }

}
