package controllers.fap;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import tags.ComboItem;
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
    
    /**
     * Método que permite filtrar la lista combos dependientes y devuelve un objeto JSON para ser manipulado por la vista.
     * @param tabla
     * @param dependencia
     * @return
     */
    public static String filterDependency(String tabla, String dependencia){
    	List<ComboItem> lstfilterDependency = new ArrayList<ComboItem>();
    	String resultados = null;
    	try  {
    		if (tabla != null && !tabla.isEmpty() && dependencia != null && !dependencia.isEmpty()){
        		List<TableKeyValueDependency> tablaD = TableKeyValueDependency.em().createQuery("select tkvd from TableKeyValueDependency tkvd where table=:tabla and dependency=:dependencia")
        				.setParameter("tabla", tabla).setParameter("dependencia", dependencia).getResultList(); 

    			for (TableKeyValueDependency tkvd: tablaD){
    				lstfilterDependency.add(new ComboItem(tkvd.key, tkvd.dependency));
    			}
    			
    			resultados = new Gson().toJson(lstfilterDependency);
        	}
    	}catch(Exception e){
    		play.Logger.error("No se han podido recuperar las dependencias de la tabla: " + tabla + " error: " + e.getMessage());
    	}
    
    	return resultados;
    }
    
    /**
     * Método que recupera los valores de una tabla para los elementos dados y los devuelve en un objeto JSON para ser manipuladas por la vista.
     * @param tabla
     * @param elementos
     * @return
     * @throws IOException
     */
    public static String filterTKV(String tabla, String elementos) throws IOException{
    	Map<String, String> mapfilterTKV = new HashMap<String, String>();
    	Type type = new TypeToken<List<ComboItem>>(){}.getType();
    	List<ComboItem> lstElementos = new ArrayList<ComboItem>();
    	List<ComboItem> lstResult = new ArrayList<ComboItem>();
    	String resultados = null;
    	try{
    		if (tabla != null && !tabla.isEmpty() && elementos != null && !elementos.isEmpty()){
    			mapfilterTKV = TableKeyValue.findByTableAsMap(tabla);
    			lstElementos = new Gson().fromJson(elementos, type);
    			
    			if (mapfilterTKV != null)
	    			for (ComboItem item : lstElementos)
	    				if (mapfilterTKV.containsKey(item.getKey()))
	    					lstResult.add(new ComboItem(item.getKey(), mapfilterTKV.get(item.getKey())));
    			
    			resultados = new Gson().toJson(lstResult);		
    		}
    	}catch (Exception e){
    		play.Logger.error("No se han podido recuperar los valores de dependencia de la tabla: " + tabla + " error: " + e.getMessage());
    	}
    
    	return resultados;
    }
    
    /**
     * Método que realiza una combinación de filterDependency y filterTKV, devuelve un objeto JSON para ser manipulado por la vista.
     * @param tabla
     * @param dependencia
     * @return
     * @throws IOException
     */
    public static String filterDependencyPerformance(String tabla, String dependencia) throws IOException{
    	Map<String, String> mapfilterTKV = new HashMap<String, String>();
    	List<ComboItem> lstResult = new ArrayList<ComboItem>();
    	String resultados = null;
    	try{
    		if (tabla != null && !tabla.isEmpty() && dependencia != null && !dependencia.isEmpty()){
    			List<TableKeyValueDependency> tablaD = TableKeyValueDependency.em().createQuery("select tkvd from TableKeyValueDependency tkvd where table=:tabla and dependency=:dependencia")
        				.setParameter("tabla", tabla).setParameter("dependencia", dependencia).getResultList(); 
    			
    			mapfilterTKV = TableKeyValue.findByTableAsMap(tabla);
    			
    			for (TableKeyValueDependency tkvd: tablaD)
    				if (mapfilterTKV.containsKey(tkvd.key))
    					lstResult.add(new ComboItem(tkvd.key, mapfilterTKV.get(tkvd.key)));
    			
    			resultados = new Gson().toJson(lstResult);		
    		}
    	}catch (Exception e){
    		play.Logger.error("No se han podido recuperar los valores de dependencia de la tabla: " + tabla + " error: " + e.getMessage());
    	}
    
    	return resultados;
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
