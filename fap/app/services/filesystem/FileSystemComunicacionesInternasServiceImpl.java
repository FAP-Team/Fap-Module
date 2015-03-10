package services.filesystem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;

import properties.FapProperties;
import properties.PropertyPlaceholder;

import com.sun.star.util.Date;
import com.sun.star.util.DateTime;

import models.AsientoCIFap;
import models.ListaUris;
import models.ReturnComunicacionInternaAmpliadaFap;
import models.ReturnComunicacionInternaFap;
import models.ReturnErrorFap;
import models.ReturnInteresadoFap;
import models.ReturnUnidadOrganicaFap;
import services.ComunicacionesInternasService;
import services.ComunicacionesInternasServiceException;
import swhiperreg.ciservices.ReturnComunicacionInternaAmpliada;
import models.AsientoAmpliadoCIFap;
import tags.ComboItem;

public class FileSystemComunicacionesInternasServiceImpl implements ComunicacionesInternasService{
	
	private PropertyPlaceholder propertyPlaceholder;
	public final String USUARIOHIPERREG;
	public final String PASSWORDHIPERREG;
	
	@Inject
	public FileSystemComunicacionesInternasServiceImpl (PropertyPlaceholder propertyPlaceholder){
		this.propertyPlaceholder = propertyPlaceholder;
		
		USUARIOHIPERREG = FapProperties.get("fap.platino.registro.username");
		PASSWORDHIPERREG = FapProperties.get("fap.platino.registro.password");
	}

	@Override
	public ReturnComunicacionInternaFap crearNuevoAsiento(AsientoCIFap asientoFap) throws ComunicacionesInternasServiceException {
		ReturnComunicacionInternaFap respuesta = new ReturnComunicacionInternaFap();
		respuesta.usuario = asientoFap.userId;
		respuesta.resumen = asientoFap.resumen;
		respuesta.observaciones = asientoFap.observaciones;
		//respuesta.referencia //Solo existe en la doc no en el WS.
		respuesta.fecha = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
		respuesta.hora = Long.toString(new java.util.Date().getTime());
		respuesta.tipoComunicacion = asientoFap.tipoTransporte;
		respuesta.ejercicio =Long.toString(new java.util.Date().getYear());
		respuesta.numeroGeneral = (long) Math.random();
		respuesta.contadorUO = asientoFap.unidadOrganicaDestino.toString();
		respuesta.numeroRegistro = (long) Math.random();
		respuesta.asunto = asientoFap.asuntoCodificado;
		respuesta.unidadOrganica = "Descripcion de la unidad organica "+respuesta.contadorUO;
		respuesta.interesado = new ReturnInteresadoFap();
			respuesta.interesado.nombre = "Nombre del Interesado";
			respuesta.interesado.save();
		respuesta.tipoTransporte = "Tipo de transporte";
		respuesta.uris = new ArrayList<ListaUris>();
			//¿Añadir uris de documentos o vacio para probar???
		respuesta.error = new ReturnErrorFap();
		respuesta.error.codigo = 0;
		respuesta.error.descripcion = null;
		return respuesta;
	}
	
	public ReturnComunicacionInternaAmpliadaFap crearNuevoAsientoAmpliado(AsientoAmpliadoCIFap asientoAmpliadoFap) throws ComunicacionesInternasServiceException{
		ReturnComunicacionInternaFap respuesta = new ReturnComunicacionInternaFap();
		respuesta.usuario = asientoAmpliadoFap.userId;
		respuesta.resumen = asientoAmpliadoFap.resumen;
		respuesta.observaciones = asientoAmpliadoFap.observaciones;
		//respuesta.referencia //Solo existe en la doc no en el WS.
		respuesta.fecha = new DateTime().toString();
		respuesta.hora = Long.toString(new java.util.Date().getTime());
		respuesta.tipoComunicacion = asientoAmpliadoFap.tipoTransporte;
		respuesta.ejercicio =Long.toString(new java.util.Date().getYear());
		respuesta.numeroGeneral = (long) Math.random();
		respuesta.contadorUO = asientoAmpliadoFap.unidadOrganicaDestino.toString();
		respuesta.numeroRegistro = (long) Math.random();
		respuesta.asunto = asientoAmpliadoFap.asuntoCodificado;
		respuesta.unidadOrganica = "Descripcion de la unidad organica "+respuesta.contadorUO;
		respuesta.interesado = new ReturnInteresadoFap();
			respuesta.interesado.nombre = "Nombre del Interesado";
			respuesta.interesado.save();
		respuesta.tipoTransporte = "Tipo de transporte";
		respuesta.uris = new ArrayList<ListaUris>();
			//¿Añadir uris de documentos o vacio para probar???
		respuesta.error.descripcion = "0"; //Sin errores
		return null;
		
	}

	@Override
	public void mostrarInfoInyeccion() {
		play.Logger.info("El servicio de Comunicaciones Internas ha sido inyectado con FileSystem y está operativo.");
		
	}

	public List<ReturnUnidadOrganicaFap> obtenerUnidadesOrganicas(Long codigo) throws ComunicacionesInternasServiceException{
		List<ReturnUnidadOrganicaFap> resultado = new ArrayList<ReturnUnidadOrganicaFap>();
		resultado.add(new ReturnUnidadOrganicaFap());
		resultado.add(new ReturnUnidadOrganicaFap());
		return resultado;
	}

}
