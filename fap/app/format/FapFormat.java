package format;

public class FapFormat {

	public static String format(Object value) {
		if (value == null)
			return "0,00";
		if (value instanceof Double) {
			// Obtenemos los decimales que queremos mostrar (máximo y mínimo)
			Integer minDecimales = properties.FapProperties.getInt("fap.format.double.min");
			Integer maxDecimales = properties.FapProperties.getInt("fap.format.double.max");

      		java.text.DecimalFormat decim = (java.text.DecimalFormat) java.text.NumberFormat.getInstance(java.util.Locale.US);
			java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols();
			char ch = ',';
			dfs.setDecimalSeparator(ch);
			ch = '.';
			dfs.setGroupingSeparator(ch);
			decim.setMaximumIntegerDigits(308);
			decim.setDecimalFormatSymbols(dfs);
			if ((maxDecimales != null) && (minDecimales != null)) {
				decim.setMaximumFractionDigits(maxDecimales);
				decim.setMinimumFractionDigits(minDecimales);
			}
			return decim.format(value);
		}

		return value.toString();
	}
	
	/**
	 * Formatea al número de decimales indicados en:
	 * <b>fap.format."tipo".min</b> y <b>fap.format."tipo".min</b> 
	 * @param value
	 * @param tipo string contenido en la property en la que mira para formatear
	 * @return
	 */
	public static String format(Double value, String tipo) {
		if (value == null)
			return "0,00";
		
		Integer minDecimales = properties.FapProperties.getInt("fap.format."+tipo+".min");
		Integer maxDecimales = properties.FapProperties.getInt("fap.format."+tipo+".max");
		if (minDecimales == null && maxDecimales == null) {
			return format(value);
		}
		
  		java.text.DecimalFormat decim = (java.text.DecimalFormat) java.text.NumberFormat.getInstance(java.util.Locale.US);
		java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols();
		char ch = ',';
		dfs.setDecimalSeparator(ch);
		ch = '.';
		dfs.setGroupingSeparator(ch);
		decim.setMaximumIntegerDigits(308);
		decim.setDecimalFormatSymbols(dfs);
		if ((maxDecimales != null) && (minDecimales != null)) {
			decim.setMaximumFractionDigits(maxDecimales);
			decim.setMinimumFractionDigits(minDecimales);
		}
		return decim.format(value);
	}
	
	/**
	 * Formateamos a tipo moneda
	 * @param value
	 * @return
	 */
	public static String formatMoneda (Double value) {
		return format(value, "moneda");
	}
}
