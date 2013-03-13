package controllers.fap;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import config.InjectorConfig;

import enumerado.fap.gen.EstadoNotificacionEnum;

import models.Agente;
import models.Interesado;
import models.Notificacion;
import models.Solicitante;
import models.SolicitudGenerica;
import models.TableKeyValue;
import models.TableKeyValueDependency;
import play.mvc.*;
import properties.FapProperties;
import services.TercerosService;
import services.TercerosServiceException;
import utils.DocumentosUtils;
import utils.TercerosUtils;
import validation.CifCheck;

@With(CheckAccessController.class)
public class UtilsController extends Controller {

    public static boolean documentoEsMultiple(String tipoUri) {
    	return DocumentosUtils.esTipoMultiple(tipoUri);
    }
    
    public static Integer getNuevasNotificaciones(String usuario){
    	List<Notificacion> notificaciones = Notificacion.find("select notificacion from Notificacion notificacion where notificacion.estado=?", EstadoNotificacionEnum.puestaadisposicion.name()).fetch();
    	List<Notificacion> misNotificaciones = new ArrayList<Notificacion>();
    	boolean esMiNotificacion=false;
    	if (notificaciones != null){
	    	for (Notificacion notificacion: notificaciones){
	    		for (Interesado interesado: notificacion.interesados){
	    			if ((interesado.persona.getNumeroId() != null) && (interesado.persona.getNumeroId().equals(usuario))){
	    				esMiNotificacion=true;
	    				break;
	    			}
	    		}
	    		if (esMiNotificacion){
	    			misNotificaciones.add(notificacion);
	    			esMiNotificacion=false;
	    		}
	    	}
    	} else {
    		return 0;
    	}
    	return misNotificaciones.size();
    }
    
    public static String filterDependency(String tabla, String dependencia){
    	if (!dependencia.isEmpty()){
	    	List<TableKeyValueDependency> tablaD = TableKeyValueDependency.find("select tkvd from TableKeyValueDependency tkvd where table=?", tabla).fetch(); 
			String js = "{";
			int tamLimite = tablaD.size();
			int contador=1;
			for (TableKeyValueDependency tkvd: tablaD){
				if (dependencia.equals(tkvd.dependency)){
					js += tkvd.key + ":" + tkvd.dependency;
					if(contador++ <= tamLimite){
						js += "%";
					}
				}
			}
			js += "}";
	    	return js;
    	}
    	return "{}";
    }
    
    public static String filterTKV(String tabla, String elementos) throws IOException{

    	elementos = elementos.replaceFirst("\\{", "");
    	elementos = elementos.replaceFirst("\\}", "");
    	String[] parseador = elementos.split(":.*?%");
    	List<String> mapaFilter = new ArrayList<String>();
    	for (String s : parseador) {
    	    mapaFilter.add(s);
    	}
    	if (!mapaFilter.isEmpty()){
    		Map<String, String> mapa = TableKeyValue.findByTableAsMap(tabla);
			String js = "{";
			int tamLimite = mapaFilter.size();
			int contador=1;
			for (String key: mapaFilter){
				if (mapa.containsKey(key)){
					js += key + ":" + mapa.get(key);
					if(contador++ <= tamLimite){
						js += "%";
					}
				}
			}
			js += "}";
	    	return js;
    	}
    	return "{}";
    }
    
    public static String getTerceroByNipOrCif (String numeroIdentificacion, String tipoIdentificacion) {
    	if (FapProperties.getBoolean("fap.platino.tercero.activa")){
    		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
    		SolicitudGenerica solicitud = null;
    		if ((ids != null) && (ids.get("idSolicitud") != null))
    			solicitud = SolicitudGenerica.findById((Long)ids.get("idSolicitud"));

	    	if ((numeroIdentificacion == null) || (numeroIdentificacion.isEmpty()) || (tipoIdentificacion == null) || (tipoIdentificacion.isEmpty())){
	    		return "{}";
	    	}
	    	if ((tipoIdentificacion.equals("cif")) || (AgenteController.getAgente().username.equals(numeroIdentificacion))){
	    		if (tipoIdentificacion.equals("cif")){
	    			if (!CifCheck.validaCif(numeroIdentificacion, new StringBuilder())){
	    				play.Logger.info("CIF no válido: ["+numeroIdentificacion+"]");
						return "{}";
	    			}
	    		}
		    	TercerosService tercerosService = InjectorConfig.getInjector().getInstance(TercerosService.class);
		    	try {
					Solicitante terceroEncontrado = tercerosService.buscarTercerosDetalladosByNumeroIdentificacion(numeroIdentificacion, tipoIdentificacion);
					if (terceroEncontrado == null){
						play.Logger.info("El Tercero no ha sido encontrado ["+numeroIdentificacion+" - "+tipoIdentificacion+"] en la BDD a Terceros.");
						return "{}";
					}
					return TercerosUtils.convertirSolicitanteAJS(terceroEncontrado);
				} catch (Exception e) {
					play.Logger.error("Hubo un problema al intentar recuperar el Tercero["+numeroIdentificacion+" - "+tipoIdentificacion+"] de la BDD a Terceros: "+e.getMessage());
					return "{}";
				}
	    	}
	    	else
	    		play.Logger.info("No se recuperaran los datos de Terceros de Platino porque el Agente: "+AgenteController.getAgente().username+" está rellenando la solicitud para: "+numeroIdentificacion);

	    	return "{}";
    	}
    	return "{}";
    }

}
