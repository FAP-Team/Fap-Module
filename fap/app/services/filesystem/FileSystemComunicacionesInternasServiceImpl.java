package services.filesystem;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import com.sun.star.util.Date;
import com.sun.star.util.DateTime;

import models.AsientoCIFap;
import models.ListaUris;
import models.ReturnComunicacionInternaFap;
import models.ReturnInteresadoFap;
import services.ComunicacionesInternasService;
import tags.ComboItem;

public class FileSystemComunicacionesInternasServiceImpl implements ComunicacionesInternasService{

	@Override
	public ReturnComunicacionInternaFap crearNuevoAsiento(AsientoCIFap asientoFap) {
		ReturnComunicacionInternaFap respuesta = new ReturnComunicacionInternaFap();
		respuesta.usuario = asientoFap.userId;
		respuesta.resumen = asientoFap.resumen;
		respuesta.observaciones = asientoFap.observaciones;
		//respuesta.referencia //Solo existe en la doc no en el WS.
		respuesta.fecha = new DateTime().toString();
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
		respuesta.error = "0"; //Sin errores
		return null;
	}

	@Override
	public void mostrarInfoInyeccion() {
		play.Logger.info("El servicio de Comunicaciones Internas ha sido inyectado con FileSystem y está operativo.");
		
	}
	
	@Override
	public List<String> obtenerUnidadesOrganicas(String userId, String password){
		List<String> resultado = new ArrayList<String>();
		resultado.add("unidad 1");
		resultado.add("unidad 2");
		return resultado;
	}

}
