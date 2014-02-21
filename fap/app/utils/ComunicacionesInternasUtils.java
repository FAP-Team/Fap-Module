package utils;

import java.util.ArrayList;
import java.util.List;

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
		
		return respuestaFap;
	}
	
	public static ReturnComunicacionInternaAmpliadaFap respuestaComunicacionInternaAmpliada2respuestaComunicacionInternaAmpliadaFap (ReturnComunicacionInternaAmpliada respuesta){
		ReturnComunicacionInternaAmpliadaFap respuestaFap = new ReturnComunicacionInternaAmpliadaFap();
		if (respuesta.getError() == null){
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
		for (ReturnUnidadOrganica unidad:unidades){
			ReturnUnidadOrganicaFap unidadOrganica = new ReturnUnidadOrganicaFap();
			unidadOrganica.codigo = unidad.getCodigo();
			unidadOrganica.codigoCompleto = unidad.getCodigoCompleto();
			unidadOrganica.descripcion = unidad.getDescripcion();
			unidadOrganica.esBaja = unidad.getEsBaja();
			unidadOrganica.esReceptora = unidad.getEsReceptora();
			unidadOrganica.codigoReceptora = unidad.getCodigoUOReceptora();
			unidadOrganica.error = error2errorFap(unidad.getError()); //Este ReturnError es del Services.amx no del CIServices.amx
			//esta comprobación estaba hecha en la implementación anterior, habrá que preguntar por qué
			if (unidadOrganica.esReceptora.equals("S")){
				unidadesFap.add(unidadOrganica);
			}
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

	public static List<ComboItem> unidadesOrganicas2Combo (String userId, String password){
		List<ComboItem> resultado = new ArrayList<ComboItem>();
		ComunicacionesInternasService comunicacionesService = InjectorConfig.getInjector().getInstance(ComunicacionesInternasService.class);
		List<ReturnUnidadOrganicaFap> unidades = comunicacionesService.obtenerUnidadesOrganicas(userId, password);
		for (ReturnUnidadOrganicaFap unidad : unidades) {
			ReturnUnidadOrganicaFap unidadOrganica = ReturnUnidadOrganicaFap.find("Select unidadOrganica from ReturnUnidadOrganicaFap unidadOrganica where unidadOrganica.codigo = ?", unidad.codigo).first();
			if (unidadOrganica  == null && unidad != null)
				unidad.save();
			resultado.add(new ComboItem(unidad.codigo, unidad.codigo + " "  + unidad.descripcion));
			System.out.println("key 0: " + resultado.get(resultado.size()-1).getKey().toString());
		}
		return resultado;
	}
}
