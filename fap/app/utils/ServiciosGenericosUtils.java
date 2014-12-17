package utils;

import java.util.ArrayList;
import java.util.List;
import models.ReturnUnidadOrganicaFap;
import tags.ComboItem;

public class ServiciosGenericosUtils {
	
	/**
	 * Metodo que devuelve las Unidades Organicas mediante los parametros indicados con el motivo
	 * de usarlas en un elemento ComboBox.
	 * @param esReceptora
	 * @param esBaja
	 * @return
	 */
	public static List<ComboItem> unidadesOrganicasCombo (boolean esReceptora, boolean esBaja){
		List<ComboItem> resultado = new ArrayList<ComboItem>();
		List<ReturnUnidadOrganicaFap> unidadesOrganicas = ReturnUnidadOrganicaFap.findAll();
		
		for (ReturnUnidadOrganicaFap unidad : unidadesOrganicas)
			if (unidad.esReceptora.equals(esReceptora(esReceptora)) && unidad.esBaja.equals(esBaja(esBaja)))
				resultado.add(new ComboItem(unidad.codigo, unidad.codigoCompleto + " "  + unidad.descripcion));
		
		return resultado;
	}
	
	/**
	 * Metodo que recupera una lista de Unidades Organicas de un nivel determinado,
	 *  
	 * @param nivel
	 * @return
	 */
	public static List<ReturnUnidadOrganicaFap> unidadesOrganicasNivel(int nivel){
		final String patternStart = "^(";
		final String patternEnd = ")$";
		final String patternSubnivel = "[0-9A-Za-z][1-9A-Za-z]";
		final String patternNoDescendencia = "00";
		final String patternSeparadorSubnivel = ".";
		final int maxNiveles = 3;
		
		if (nivel > maxNiveles)
			nivel = maxNiveles;
		
		int poda = maxNiveles - nivel;
		String pattern = patternStart;
		for (int i = 0; i <= nivel; i++){
			if (poda >0)
				pattern += patternSubnivel + patternSeparadorSubnivel;
			else
				if (i == maxNiveles)
					pattern += patternSubnivel;
				else
					pattern += patternSubnivel + patternSeparadorSubnivel;
		}
		for (int j = 0; j < poda; j++){
			if (j == poda-1)
				pattern += patternNoDescendencia;
			else
				pattern += patternNoDescendencia + patternSeparadorSubnivel;
		}
		pattern += patternEnd;
		
		List<ReturnUnidadOrganicaFap> resultado = new ArrayList<ReturnUnidadOrganicaFap>();
		List<ReturnUnidadOrganicaFap> unidadesOrganicas = ReturnUnidadOrganicaFap.findAll();
		for (ReturnUnidadOrganicaFap unidad : unidadesOrganicas)
			if (unidad != null && unidad.codigoCompleto != null && !unidad.codigoCompleto.isEmpty())
				if (unidad.codigoCompleto.matches(pattern))
					resultado.add(unidad);
		
		return resultado;
	}
	
	/**
	 * Metodo para obtener registros de Unidades Organicas mediante jerarquia con el motivo
	 * de introducir dichos registros en un elemento ComboBox. 
	 * @param codigoRaiz
	 * @param profundidad
	 * @return
	 */
	public static List<ComboItem> unidadesOrganicasJerarquiaCombo(int codigoRaiz){
		Long codigo = new Long(codigoRaiz);
		ReturnUnidadOrganicaFap unidadOrganica = ReturnUnidadOrganicaFap.find("Select unidadOrganica from ReturnUnidadOrganicaFap unidadOrganica where unidadOrganica.codigo = ?", codigo).first();
		List<ComboItem> resultado = new ArrayList<ComboItem>();
		List<ReturnUnidadOrganicaFap> unidadesOrganicas = null;
		
		if (unidadOrganica != null){
			unidadesOrganicas = obtenerDescendeciaUO(unidadOrganica);
			for (ReturnUnidadOrganicaFap unidad : unidadesOrganicas)
				resultado.add(new ComboItem(unidad.codigo, unidad.codigoCompleto + " "  + unidad.descripcion));
		}
		
		return resultado;
	}
	
	/**
	 * Recupera una lista de Unidades Organicas que son la descendencia de una unidad dada y hasta 
	 * una profundidad dada.
	 * @param unidad
	 * @param profundidad
	 * @return
	 */
	public static List<ReturnUnidadOrganicaFap> obtenerDescendeciaUO(ReturnUnidadOrganicaFap unidad){
		final String patternStart = "^(";
		final String patternEnd = ")$";
		final String patternSubnivel = "[0-9A-Za-z][1-9A-Za-z]";
		final String patternNoDescendencia = "00";
		final String splitSeparadorSubnivel = "\\.";
		final String patternSeparadorSubnivel = ".";
		final int maxNiveles;
		List<ReturnUnidadOrganicaFap> unidadesOrganicas = null;
		List<ReturnUnidadOrganicaFap> resultado = new ArrayList<ReturnUnidadOrganicaFap>();
		
		if (unidad != null){
			int nivel = calcularNivelUO(unidad);
			
			if (nivel != -1) {
				String[] codigoUONiveles = unidad.codigoCompleto.split(splitSeparadorSubnivel);
				maxNiveles = codigoUONiveles.length-1;
	
				unidadesOrganicas = ReturnUnidadOrganicaFap.findAll();
				if (unidadesOrganicas != null && !unidadesOrganicas.isEmpty()) {
					String[] patterSubNivel = codigoUONiveles;
					for (int i = nivel; i < maxNiveles; i++) {
						patterSubNivel[i+1] = patternSubnivel;
						String pattern = patternStart + StringUtils.join(patternSeparadorSubnivel, patterSubNivel);
						int pos = pattern.lastIndexOf(patternSeparadorSubnivel);
						pattern = pattern.substring(0, pos) + patternEnd;
						
						for (ReturnUnidadOrganicaFap unidadUO : unidadesOrganicas)
							if (unidadUO != null && unidadUO.codigoCompleto != null && !unidadUO.codigoCompleto.isEmpty())
								if (unidadUO.codigoCompleto.matches(pattern))
									resultado.add(unidadUO);
					}
					
					
				}
			}
		}
		
		return resultado;
	}
	
	/**
	 * Devuelve un entero con el nivel dentro de la jerarquia de una Unidad Organica.
	 * Actualmente existen 4 niveles.
	 * @param unidad
	 * @return
	 */
	public static int calcularNivelUO(ReturnUnidadOrganicaFap unidad){
		int nivel = 0;
		final String patternNoDescendencia = "00";
		final String patternSeparadorNivel = "\\.";
		boolean descendencia = true;
		
		if (unidad != null){
			String codigoCompleto = unidad.codigoCompleto;
			String[] arbolUO = codigoCompleto.split(patternSeparadorNivel);
			int niveles = arbolUO.length;
			while (descendencia && (nivel < niveles)){ 
				if (arbolUO[nivel].matches(patternNoDescendencia))
					descendencia = false;	
				else
					nivel++;
			}
		}
		
		return nivel-1;
	}
	
	/**
	 * Almacena las Unidades Orgánicas recibidas en la Base de Datos si no existian previamente
	 * @param lstUO
	 */
	public static void cargarUnidadesOrganicas(List<ReturnUnidadOrganicaFap> lstUO) {
		
		if (lstUO != null) {
			for (ReturnUnidadOrganicaFap unidad : lstUO) {
				ReturnUnidadOrganicaFap unidadOrganica = null;
				if (unidad != null && unidad.codigo != null)
				  unidadOrganica = ReturnUnidadOrganicaFap.find("Select unidadOrganica from ReturnUnidadOrganicaFap unidadOrganica where unidadOrganica.codigo = ?", unidad.codigo).first();
				
				if (unidadOrganica == null && unidad != null && unidad.codigo != null)
					unidad.save();
			}
		}
	}
	
	private static String esReceptora(boolean esReceptora){
		return esReceptora ? "S" : "N";
	}
	
	private static String esBaja(boolean esBaja){
		return esBaja ? "S" : "N";
	}

}
