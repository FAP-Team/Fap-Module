package utils;

import messages.Messages;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import play.mvc.Util;
import properties.FapProperties;

public class DateTimeUtils {
	public final static String FORMAT_DEFAULT_CONF;
	public final static String FECHA_CIERRE_CONVOCATORIA = FapProperties.get("convocatoria.fechaCierre.presentacionSolicitud");
	//TODO SMB 09/04/2012
	public final static String FECHA_FIN_DESISTIMIENTO = FapProperties.get("fap.tramitacion.desistimiento.fechafin");

	static {
		String format = "dd/MM/yyyy";
		try {
			format = FapProperties.get("date.format.fr");
		}
		catch (Exception ex1) {
			Messages.fatal("No se ha definido la variable 'date.format.fr' en el fichero de configuración.");
		}

		FORMAT_DEFAULT_CONF = format;
	}

	public static final DateTime parseDefaultConf(String fecha) {
		return DateTime.parse(fecha, DateTimeFormat.forPattern(FORMAT_DEFAULT_CONF));
	}

	public static final boolean cumplidaFechaCierreSolicitud() {
	 return DateTimeUtils.parseDefaultConf(FECHA_CIERRE_CONVOCATORIA).plusDays(1).isBeforeNow();
	}

	//TODO SMB 09/04/2012
	public static final boolean cumplidaFechaFinDesistimiento() {
		return DateTimeUtils.parseDefaultConf(FECHA_FIN_DESISTIMIENTO).plusDays(1).isBeforeNow();
	}

}