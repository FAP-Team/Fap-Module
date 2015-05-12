package services.comunicacionesInternas;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.mockito.internal.invocation.UnusedStubsFinder;
import config.InjectorConfig;
import es.gobcan.platino.servicios.procedimientos.UnidadOrganicaWSItem;
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
import models.RespuestaCIFap;
import models.ReturnErrorFap;
import models.ReturnInteresadoCIFap;
import models.ReturnInteresadoFap;
import models.ReturnUnidadOrganicaFap;
import models.SolicitudGenerica;

public class ComunicacionesInternasUtils {

	/**
	 * Método que parsea los datos de la respuesta a la creación de una comunicación interna (Normal) en un modelo propio
	 * para almacenarlo en la base de datos.
	 * @param respuesta
	 * @return
	 */
	public static RespuestaCIFap respuestaComunicacionInterna2respuestaComunicacionInternaFap (ReturnComunicacionInterna respuesta){
		RespuestaCIFap respuestaFap = new RespuestaCIFap();
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
			ReturnInteresadoCIFap interesadoFAP = interesadoCI2interesadoFap(respuesta.getInteresado());
			if (interesadoFAP != null) {
				respuestaFap.interesado = new ReturnInteresadoCIFap();
				respuestaFap.interesado = interesadoFAP;
			}
			respuestaFap.tipoTransporte = respuesta.getTipoTransporte();
			respuestaFap.uris = urisCI2UrisFap (respuesta.getUris()); 
		}
		else{
			ReturnErrorFap errorFAP = errorCI2errorFap(respuesta.getError());
			if (errorFAP != null) {
				respuestaFap.error = new ReturnErrorFap();
				respuestaFap.error = errorFAP;
			}
		}
		
		return respuestaFap;
	}
	
	/**
	 * Método que parsea los datos de la respuesta a la creación de una comunicación interna (Ampliada) en un modelo propio
	 * para almacenarlo en la base de datos.
	 * @param respuesta
	 * @return
	 */
	public static RespuestaCIFap respuestaComunicacionInternaAmpliada2respuestaComunicacionInternaAmpliadaFap (ReturnComunicacionInternaAmpliada respuesta){
		RespuestaCIFap respuestaFap = new RespuestaCIFap();
		if (respuesta.getError().getDescripcion() == null){
			respuestaFap.usuario = respuesta.getUsuario();
			respuestaFap.resumen = respuesta.getResumen();
			respuestaFap.observaciones = respuesta.getObservaciones();
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
			ReturnInteresadoCIFap interesadoFAP = interesadoCI2interesadoFap(respuesta.getInteresado());
			if (interesadoFAP != null) {
				respuestaFap.interesado = new ReturnInteresadoCIFap();
				respuestaFap.interesado = interesadoFAP;
			}
			respuestaFap.tipoTransporte = respuesta.getTipoTransporte();
			respuestaFap.uris = urisCI2UrisFap (respuesta.getUris()); 
			respuestaFap.unidadOrganicaPropuesta = respuesta.getUnidadOrganicaPropuesta();
		}
		else {
			ReturnErrorFap errorFAP = errorCI2errorFap(respuesta.getError());
			if (errorFAP != null) {
				respuestaFap.error = new ReturnErrorFap();
				respuestaFap.error = errorFAP;
			}
		}
		
		return respuestaFap;
	}
	
	/**
	 * Método que parsea las uris de los documentos enviados a un modelo propio para ser almacenadas en la base de datos.
	 * @param listaUris
	 * @return
	 */
	public static List<ListaUris> urisCI2UrisFap (ArrayOfString listaUris){
		List<ListaUris> lstUrns = null;
		if (listaUris != null && !listaUris.getString().isEmpty()){
			lstUrns = new ArrayList<ListaUris>();
			for (String urn : listaUris.getString()){
				if (urn != null && !urn.isEmpty()){
					ListaUris uri = new ListaUris();
					uri.uri = urn;
					lstUrns.add(uri);
				}
			}
		}
		
		return lstUrns;
	}
	
	/**
	 * Método que parsea un posible error recibido al crear una comunicación interna a un modelo propio para ser almacenado.
	 * @param error
	 * @return
	 */
	public static ReturnErrorFap errorCI2errorFap (ReturnError error){
		ReturnErrorFap errorFap = null;
		
		if (error != null && error.getCodigo() != 0 && error.getDescripcion() != null) {
			errorFap = new ReturnErrorFap();
			errorFap.codigo = error.getCodigo();
			errorFap.descripcion = error.getDescripcion();
		}
		
		return errorFap;
	}
	
	/**
	 * Método que parsea el interesado de una comunicación interna a un modelo propio para ser almacenado
	 * @param interesado
	 * @return
	 */
	public static ReturnInteresadoCIFap interesadoCI2interesadoFap (ReturnInteresadoCI interesado) {
		ReturnInteresadoCIFap interesadoFap = null;
		if (interesado!= null && interesado.getNombre() != null && !interesado.getNombre().isEmpty()) {
			interesadoFap = new ReturnInteresadoCIFap();
			interesadoFap.nombre = interesado.getNombre();
		}

		return interesadoFap;
	}

}
