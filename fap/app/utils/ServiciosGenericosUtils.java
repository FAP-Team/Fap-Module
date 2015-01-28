package utils;

import java.util.ArrayList;
import java.util.List;

import models.MapeoUOBDOrganizacionHiperreg;
import models.ReturnErrorFap;
import models.ReturnUnidadOrganicaFap;
import swhiperreg.service.ArrayOfReturnUnidadOrganica;
import swhiperreg.service.ReturnUnidadOrganica;
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
		final String patternSubnivelLeft = "[0-9A-Za-z][1-9A-Za-z]";
		final String patternSubnivelRight = "[1-9A-Za-z][0-9A-Za-z]";
		final String patternNoDescendencia = "00";
		final String patternSeparadorSubnivel = ".";
		final int maxNiveles = 3;
		
		if (nivel > maxNiveles)
			nivel = maxNiveles;
		
		int poda = maxNiveles - nivel;
		String patternLeafLeft = patternStart;
		String patternLeafRight = patternStart;
		for (int i = 0; i <= nivel; i++){
			if (poda >0) {
				patternLeafLeft += patternSubnivelLeft + patternSeparadorSubnivel;
				patternLeafRight += patternSubnivelRight + patternSeparadorSubnivel;
			} else
				if (i == maxNiveles) {
					patternLeafLeft += patternSubnivelLeft;
					patternLeafRight += patternSubnivelRight;
				} else {
					patternLeafLeft += patternSubnivelLeft + patternSeparadorSubnivel;
					patternLeafRight += patternSubnivelRight + patternSeparadorSubnivel;
				}
		}
		
		for (int j = 0; j < poda; j++){
			if (j == poda-1) {
				patternLeafLeft += patternNoDescendencia;
				patternLeafRight += patternNoDescendencia;
			} else {
				patternLeafLeft += patternNoDescendencia + patternSeparadorSubnivel;
				patternLeafRight += patternNoDescendencia + patternSeparadorSubnivel;
			}
		}
		patternLeafLeft += patternEnd;
		patternLeafRight += patternEnd;
		
		List<ReturnUnidadOrganicaFap> resultado = new ArrayList<ReturnUnidadOrganicaFap>();
		List<ReturnUnidadOrganicaFap> unidadesOrganicas = ReturnUnidadOrganicaFap.findAll();
		for (ReturnUnidadOrganicaFap unidad : unidadesOrganicas)
			if (unidad != null && unidad.codigoCompleto != null && !unidad.codigoCompleto.isEmpty())
				if (unidad.codigoCompleto.matches(patternLeafLeft) || unidad.codigoCompleto.matches(patternLeafRight))
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
		final String patternSubnivelLeft = "[0-9A-Za-z][1-9A-Za-z]";
		final String patternSubnivelRight = "[1-9A-Za-z][0-9A-Za-z]";
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
					String[] patternLeafLeft = codigoUONiveles;
					String[] patternLeaflRight = codigoUONiveles;
					for (int i = nivel; i < maxNiveles; i++) {
						patternLeafLeft[i+1] = patternSubnivelLeft;
						patternLeaflRight[i+1] = patternSubnivelRight;
						String patternLeft = patternStart + StringUtils.join(patternSeparadorSubnivel, patternLeafLeft);
						int posLeft = patternLeft.lastIndexOf(patternSeparadorSubnivel);
						patternLeft = patternLeft.substring(0, posLeft) + patternEnd;
						String patternRight = patternStart + StringUtils.join(patternSeparadorSubnivel, patternLeaflRight);
						int posRight = patternRight.lastIndexOf(patternSeparadorSubnivel);
						patternRight = patternRight.substring(0, posRight) + patternEnd;
						
						for (ReturnUnidadOrganicaFap unidadUO : unidadesOrganicas)
							if (unidadUO != null && unidadUO.codigoCompleto != null && !unidadUO.codigoCompleto.isEmpty())
								if (unidadUO.codigoCompleto.matches(patternLeft) || unidadUO.codigoCompleto.matches(patternRight))
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
			
			if (codigoCompleto != null && !codigoCompleto.isEmpty()) {
				String[] arbolUO = codigoCompleto.split(patternSeparadorNivel);
				int niveles = arbolUO.length;
				while (descendencia && (nivel < niveles)){ 
					if (arbolUO[nivel].matches(patternNoDescendencia))
						descendencia = false;	
					else
						nivel++;
				}
			} else
				nivel = -1;
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
				else {
					if (unidadOrganica.codigoCompleto == null || (unidadOrganica.codigoCompleto != null && unidad.codigoCompleto != null && unidadOrganica.codigoCompleto != unidad.codigoCompleto))
						unidadOrganica.codigoCompleto = unidad.codigoCompleto;
					
					if (unidadOrganica.codigoBDOrganizacion == null || (unidadOrganica.codigoBDOrganizacion != null && unidad.codigoBDOrganizacion != null && unidadOrganica.codigoBDOrganizacion != unidad.codigoBDOrganizacion))
						unidadOrganica.codigoBDOrganizacion = unidad.codigoBDOrganizacion;
					
					if (unidadOrganica.codigoReceptora == null || (unidadOrganica.codigoReceptora != null && unidad.codigoReceptora != null && unidadOrganica.codigoReceptora != unidad.codigoReceptora))
						unidadOrganica.codigoReceptora = unidad.codigoReceptora;
					
					if (unidadOrganica.descripcion == null || (unidadOrganica.descripcion != null && unidad.descripcion != null && unidadOrganica.descripcion != unidad.descripcion))
						unidadOrganica.descripcion = unidad.descripcion;
					
					if (unidadOrganica.esBaja == null || (unidadOrganica.esBaja != null && unidad.esBaja != null && unidadOrganica.esBaja != unidad.esBaja))
						unidadOrganica.esBaja = unidad.esBaja;
					
					if (unidadOrganica.esReceptora == null || (unidadOrganica.esReceptora != null && unidad.esReceptora != null && unidadOrganica.esReceptora != unidad.esReceptora))
						unidadOrganica.esReceptora = unidad.esReceptora;
					
					unidadOrganica.save();
				}
			}
		}
	}
	
	/**
	 * Realiza el mapeado de Unidades Orgánicas de Hiperreg con BDOrganización
	 * @param lstUO
	 */
	public static void mapearUnidadesOrganicasBDOrganizacionHiperreg(List<MapeoUOBDOrganizacionHiperreg> lstUO){
		
		if (lstUO != null) {
			for (MapeoUOBDOrganizacionHiperreg mapeo : lstUO){
				ReturnUnidadOrganicaFap unidadOrganica = null;
		    	if (mapeo != null && mapeo.codigo != null && mapeo.unidadhiperreg != null){
		    		unidadOrganica = ReturnUnidadOrganicaFap.find("Select unidadOrganica from ReturnUnidadOrganicaFap unidadOrganica where unidadOrganica.codigo = ?", mapeo.unidadhiperreg).first();
		    		if (unidadOrganica != null) {
		    			unidadOrganica.codigoBDOrganizacion = mapeo.codigo;
		    			unidadOrganica.save();
		    		}
		    	}
		    }
		}
	}
	
	/**
	 * Recupera las unidades orgánicas de la base de datos simulando la llamada al servicio genérico
	 * @param codigo
	 * @return
	 */
	public static List<ReturnUnidadOrganicaFap> obtenerUnidadesOrganicasBD(Long codigo) {
		List<ReturnUnidadOrganicaFap> lstUO = null;
		try {
			if ((codigo != null) && (codigo == 0)) {
				lstUO = ServiciosGenericosUtils.unidadesOrganicasNivel(0);
			} else {
				ReturnUnidadOrganicaFap unidad = ReturnUnidadOrganicaFap.find("Select unidadOrganica from ReturnUnidadOrganicaFap unidadOrganica where unidadOrganica.codigo = ?", codigo).first();
				if (unidad != null)
					lstUO = ServiciosGenericosUtils.obtenerDescendeciaUO(unidad);
			}
			
		} catch (Exception e) {
			play.Logger.error("No se han podido recuperar las Unidades Orgánicas: " + e.getMessage());
		}
		
		return lstUO;
	}

	
	public static List<ReturnUnidadOrganicaFap> returnUnidadOrganica2returnUnidadOrganicaFap (ArrayOfReturnUnidadOrganica uo){
		List<ReturnUnidadOrganica> unidades = uo.getReturnUnidadOrganica();
		List<ReturnUnidadOrganicaFap> unidadesFap = new ArrayList<ReturnUnidadOrganicaFap>();
		for (ReturnUnidadOrganica unidad : unidades){
			
			ReturnUnidadOrganicaFap unidadOrganica = new ReturnUnidadOrganicaFap();
			unidadOrganica.codigo = unidad.getCodigo();
			unidadOrganica.codigoCompleto = unidad.getCodigoCompleto();
			unidadOrganica.descripcion = unidad.getDescripcion();
			unidadOrganica.esBaja = unidad.getEsBaja();
			unidadOrganica.esReceptora = unidad.getEsReceptora();
			unidadOrganica.codigoReceptora = unidad.getCodigoUOReceptora();
			unidadOrganica.error = error2errorFap(unidad.getError()); //Este ReturnError es del Services.amx no del CIServices.amx
			
			unidadesFap.add(unidadOrganica);
		}
		return unidadesFap;
	}
	
	public static ReturnErrorFap error2errorFap (swhiperreg.service.ReturnError error){
		ReturnErrorFap errorFap = null;
		
		if (error != null) {
			if (error.getCodigo() != 0){
				errorFap = new ReturnErrorFap();
				errorFap.codigo = error.getCodigo();
				errorFap.descripcion = error.getDescripcion();
			}
		}
		
		return errorFap;
	}
	
	private static String esReceptora(boolean esReceptora){
		return esReceptora ? "S" : "N";
	}
	
	private static String esBaja(boolean esBaja){
		return esBaja ? "S" : "N";
	}

}
