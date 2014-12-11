package utils;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.mockito.internal.invocation.UnusedStubsFinder;

import config.InjectorConfig;
import es.gobcan.platino.servicios.procedimientos.UnidadOrganicaWSItem;
import services.ComunicacionesInternasService;
import services.FirmaService;
import swhiperreg.ciservices.ArrayOfString;
import swhiperreg.ciservices.ReturnComunicacionInterna;
import swhiperreg.ciservices.ReturnComunicacionInternaAmpliada;
import swhiperreg.ciservices.ReturnError;
import swhiperreg.ciservices.ReturnInteresado;
import swhiperreg.ciservices.ReturnInteresadoCI;
import swhiperreg.service.ArrayOfReturnUnidadOrganica;
import swhiperreg.service.ReturnUnidadOrganica;
import tags.ComboItem;
import models.ComunicacionInterna;
import models.ListaUris;
import models.ReturnComunicacionInternaAmpliadaFap;
import models.ReturnComunicacionInternaFap;
import models.ReturnErrorFap;
import models.ReturnInteresadoCIFap;
import models.ReturnInteresadoFap;
import models.ReturnUnidadOrganicaFap;
import models.SolicitudGenerica;

public class ComunicacionesInternasUtils {

	public static ReturnComunicacionInternaFap respuestaComunicacionInterna2respuestaComunicacionInternaFap (ReturnComunicacionInterna respuesta){
		ReturnComunicacionInternaFap respuestaFap = new ReturnComunicacionInternaFap();
		if (respuesta.getError().getDescripcion() == null){
			respuestaFap.usuario = respuesta.getUsuario();
			respuestaFap.resumen = respuesta.getResumen();
			respuestaFap.observaciones = respuesta.getObservaciones();
			//respuestaFap.referencia  //Solo existe en la doc no en el WS
			respuestaFap.fecha = respuesta.getFecha();
			respuestaFap.hora = respuesta.getHora();
			respuestaFap.tipoComunicacion = respuesta.getTipoComunicacion();
			respuestaFap.ejercicio = Integer.toString(respuesta.getEjercicio());
			respuestaFap.numeroGeneral = respuesta.getNumeroGeneral();
			respuestaFap.contadorUO = respuesta.getContadorUO();
			respuestaFap.numeroRegistro = respuesta.getNumeroRegistro();
			respuestaFap.asunto = respuesta.getAsunto();
			respuestaFap.unidadOrganica = respuesta.getUnidadOrganica();
			//TODO REVISAR bien que devuelve esta parte -> ¿Solo un nombre?
			//respuestaFap.interesado = interesadoCI2interesadoFap(respuesta.getInteresado());
			respuestaFap.tipoTransporte = respuesta.getTipoTransporte();
			respuestaFap.uris = urisCI2UrisFap (respuesta.getUris()); //Falta
		}
		else{
			respuestaFap.error = errorCI2errorFap(respuesta.getError());
		}
		
		return respuestaFap;
	}
	
	public static ReturnComunicacionInternaAmpliadaFap respuestaComunicacionInternaAmpliada2respuestaComunicacionInternaAmpliadaFap (ReturnComunicacionInternaAmpliada respuesta){
		ReturnComunicacionInternaAmpliadaFap respuestaFap = new ReturnComunicacionInternaAmpliadaFap();
		if (respuesta.getError().getDescripcion() == null){
			respuestaFap.usuario = respuesta.getUsuario();
			respuestaFap.resumen = respuesta.getResumen();
			respuestaFap.observaciones = respuesta.getObservaciones();
			//respuestaFap.referencia  //Solo existe en la doc no en el WS
			respuestaFap.fecha = respuesta.getFecha();
			respuestaFap.hora = respuesta.getHora();
			respuestaFap.tipoComunicacion = respuesta.getTipoComunicacion();
			respuestaFap.ejercicio = Integer.toString(respuesta.getEjercicio());
			respuestaFap.numeroGeneral = respuesta.getNumeroGeneral();
			respuestaFap.contadorUO = respuesta.getContadorUO();
			respuestaFap.numeroRegistro = respuesta.getNumeroRegistro();
			respuestaFap.asunto = respuesta.getAsunto();
			respuestaFap.unidadOrganicaOrigen = respuesta.getUnidadOrganicaOrigen();
			respuestaFap.unidadOrganica = respuesta.getUnidadOrganica();
			//TODO Según el responsable del servicio solo devuelve un nombre
			respuestaFap.interesado = interesadoCI2interesadoFap(respuesta.getInteresado());
			respuestaFap.tipoTransporte = respuesta.getTipoTransporte();
			respuestaFap.uris = urisCI2UrisFap (respuesta.getUris()); //Falta
		}
		else {
			respuestaFap.error = errorCI2errorFap(respuesta.getError());
		}
		return respuestaFap;
	}
	
	public static List<ListaUris> urisCI2UrisFap (ArrayOfString uris){
		List<ListaUris> urisFap = new ArrayList<ListaUris>();
		if (uris != null) {
			for (Object listaUris : uris.getString().toArray()) {
				ListaUris nuevo = new ListaUris();
				nuevo.uri = listaUris.toString();
				System.out.println("Nuevo Uri: "+nuevo.uri);
				urisFap.add(nuevo);
			}
		}
		return urisFap;
	}
	
	public static ReturnErrorFap errorCI2errorFap (ReturnError error){
		ReturnErrorFap errorFap = new ReturnErrorFap();
		errorFap.codigo = error.getCodigo();
		errorFap.descripcion = error.getDescripcion();
		return errorFap;
	}
	
	public static ReturnInteresadoCIFap interesadoCI2interesadoFap (ReturnInteresadoCI interesado) {
		ReturnInteresadoCIFap interesadoFap = new ReturnInteresadoCIFap();
		interesadoFap.nombre = interesado.getNombre();

		return interesadoFap;
	}
	
	public static ReturnErrorFap error2errorFap (swhiperreg.service.ReturnError error){
		ReturnErrorFap errorFap = new ReturnErrorFap();
		errorFap.codigo = error.getCodigo();
		errorFap.descripcion = error.getDescripcion();
		return errorFap;
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
	
//	public static List<String> ArrayOfReturnUnidadOrganica2List (ArrayOfReturnUnidadOrganica uo){
//		List<ReturnUnidadOrganica> unidades = uo.getReturnUnidadOrganica();
//		List<String> resultado = new ArrayList<String>();
//		for (ReturnUnidadOrganica unidad : unidades) {
//			if (unidad.getEsReceptora().equals("S")){
//				resultado.add(unidad.getDescripcion());
//			}
//		}
//		return resultado;
//	}

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
	 * Metodo que recupera una lista de Unidades Organicas de un nivel dentro de la jeraquia determinado,
	 * con motivo de ser introducido en un elemento ComboBox. 
	 * @param nivel
	 * @return
	 */
	public static List<ComboItem> unidadesOrganicasNivel(int nivel){
		final String patternStart = "^(";
		final String patternEnd = ")$";
		final String patternSubnivel = "[0-9A-Za-z][1-9A-Za-z]";
		final String patternNoDescendencia = "00";
		final String patternSeparadorSubnivel = ".";
		final int maxNiveles = 3;
		
		List<ComboItem> resultado = new ArrayList<ComboItem>();
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
		
		List<ReturnUnidadOrganicaFap> unidadesOrganicas = ReturnUnidadOrganicaFap.findAll();
		for (ReturnUnidadOrganicaFap unidad : unidadesOrganicas)
			if (unidad.codigoCompleto.matches(pattern))
				resultado.add(new ComboItem(unidad.codigo, unidad.codigoCompleto + " "  + unidad.descripcion));
		
		return resultado;
	}
	
	/**
	 * Metodo para obtener registros de Unidades Organicas mediante jerarquia con el motivo
	 * de introducir dichos registros en un elemento ComboBox. 
	 * @param codigoRaiz
	 * @param profundidad
	 * @return
	 */
	public static List<ComboItem> unidadesOrganicasJerarquiaCombo(int codigoRaiz, int profundidad){
		Long codigo = new Long(codigoRaiz);
		ReturnUnidadOrganicaFap unidadOrganica = ReturnUnidadOrganicaFap.find("Select unidadOrganica from ReturnUnidadOrganicaFap unidadOrganica where unidadOrganica.codigo = ?", codigo).first();
		List<ComboItem> resultado = new ArrayList<ComboItem>();
		List<ReturnUnidadOrganicaFap> unidadesOrganicas = null;
		
		if (unidadOrganica != null){
			unidadesOrganicas = obtenerDescendeciaUO(unidadOrganica, profundidad);
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
	public static List<ReturnUnidadOrganicaFap> obtenerDescendeciaUO(ReturnUnidadOrganicaFap unidad, int profundidad){
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
				String codigoUO = "";
				for (int k = 0; k <= nivel; k++) {
					if (k == maxNiveles)
						codigoUO += codigoUONiveles[k];
					else
						codigoUO += codigoUONiveles[k] + patternSeparadorSubnivel;
				}
				
				int niveles = nivel + profundidad;
				int poda = maxNiveles - niveles;
				String pattern = patternStart + codigoUO;
				if (niveles > maxNiveles){
					niveles = maxNiveles - nivel;
					poda = 0;
				}
					
				for (int i = 0; i < niveles; i++) {
					if (poda > 0)
						pattern += patternSubnivel + patternSeparadorSubnivel;
					else
						if (i == niveles - 1)
							pattern += patternSubnivel;
						else
							pattern += patternSubnivel + patternSeparadorSubnivel;
				}
				
				for (int j = 0; j < poda; j++) {
					if (j == poda - 1)
						pattern += patternNoDescendencia;
					else
						pattern += patternNoDescendencia + patternSeparadorSubnivel;
				}
				
				pattern += patternEnd;
					
				unidadesOrganicas = ReturnUnidadOrganicaFap.findAll();
				for (ReturnUnidadOrganicaFap unidadUO : unidadesOrganicas)
					if (unidadUO.codigoCompleto.matches(pattern))
						resultado.add(unidadUO);
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
