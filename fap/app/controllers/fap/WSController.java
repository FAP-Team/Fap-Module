package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import controllers.GraficasWSController;

import messages.Messages;
import models.ListaResultadosPeticion;
import models.Peticion;
import models.ResultadoPeticion;
import models.ResultadosPeticion;
import models.ServicioWebInfo;
import models.ServiciosWeb;
import models.TableKeyValue;

import play.Play;
import play.libs.WS;
import play.libs.WS.WSRequest;
import play.mvc.Http;
import play.mvc.With;
import play.utils.Java;
import properties.FapProperties;

public class WSController extends GenericController {

	/**
	 * Función que es sobreescrita por todos los servicios web
	 * hijos.
	 * @return La información de cada servicio web.
	 */
	public static ServicioWebInfo getInfoWS() {
		return null;
	}
	
	/**
	 * Función que será llamada vía Ajax y que devuelve un array de pares,
	 * el cual será la entrada para las gráficas.
	 * @param listaResultadosId
	 * @param nameVariable
	 * @param rango
	 * @param activo
	 */
	public static void getData(Long listaResultadosId, String nameVariable, int rango, boolean activo) {
		ListaResultadosPeticion listaResultados = ListaResultadosPeticion.findById(listaResultadosId);
		Map<String, Object> mapa = new HashMap<String, Object>();
		boolean type1 = false;
		boolean type2 = false;
		boolean type3 = false;
		for (int i = 0; i < listaResultados.resultadosPeticion.size(); i++) {
			ResultadosPeticion resultados = listaResultados.resultadosPeticion.get(i);
			
			for (int j = 0; j < resultados.resultadoPeticion.size(); j++) {
				ResultadoPeticion resultado = resultados.resultadoPeticion.get(j);
				String nombreVariable = resultado.nombre;
				String valor = null;
				
				if (nameVariable.equals(nombreVariable)) {
					if (resultado.getType() != null) {
						if (resultado.getType().equals("String"))
							valor = resultado.valorString;
						else if (resultado.getType().equals("Boolean")) {
							if (resultado.valorBoolean.toString().toLowerCase().equals("true"))
								valor = "Sí";
							else
								valor = "No";
						}
						else if (resultado.getType().equals("DateTime")) {
							valor = resultado.valorDateTime;
							String fecha = valor.split("T")[0];
							String dia = fecha.split("-")[2];
							String mes = fecha.split("-")[1];
							String agno = fecha.split("-")[0];
							if (rango == 0)
								valor = "Día "+dia;
							else if (rango == 1) {
								valor = dia+"-"+getMes(mes);
								type1 = true;
							}
							else if (rango == 2) {
								DateTime date = new DateTime(fecha);
								valor = "Semana " + date.getWeekOfWeekyear();
							}
							else if (rango == 3) {
								valor = getMes(mes);
								type2 = true;
							}
							else if (rango == 4) {
								valor = getMes(mes)+"-"+agno;
								type3 = true;
							}
							else if (rango == 5)
								valor = agno;
						}
						
						if (mapa.containsKey(valor)) {
							int numAnterior = (Integer) mapa.get(valor);
							mapa.remove(valor);
							mapa.put(valor, numAnterior + 1);
						} else {
							mapa.put(valor, 1);
						}
					}
				}
				
			}
		}
		Map<String, Object> mapaOrdenado = sortByComparator(mapa, type1, type2, type3);
		type1 = false;
		type2 = false;
		type3 = false;
		// Teniendo en el mapa la información que se va a representar,
		// se crea un array de pares con esa información.
		String jsData = "[";
		Iterator it = mapaOrdenado.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry e = (Map.Entry)it.next();
			jsData += "[ '" + e.getKey() + "', "+ e.getValue()+"]";
			if (it.hasNext())
				jsData += ", ";
		}
		jsData += "]";
		renderText(jsData);
	}
	
	/**
	 * Función que me permite ordenar el mapa con la información que 
	 * se va a mostrar en la gráfica.
	 * @param mapa
	 * @return mapaOrdenado
	 */
	private static Map sortByComparator(Map mapa, final boolean type1, final boolean type2, final boolean type3) {		 
        List lista = new LinkedList(mapa.entrySet());
        Collections.sort(lista, new Comparator() {
             public int compare(Object o1, Object o2) {
            	if (type1) {
            		String clave1 = getNumMes(((String) ((Map.Entry) (o1)).getKey()).split("-")[1]) + "-" +  ((String) ((Map.Entry) (o1)).getKey()).split("-")[0];
            		String clave2 = getNumMes(((String) ((Map.Entry) (o2)).getKey()).split("-")[1]) + "-" + ((String) ((Map.Entry) (o2)).getKey()).split("-")[0];
            		return ((Comparable) clave1).compareTo(clave2);
            	} else if (type2) {
            		String clave1 = ((String) ((Map.Entry) (o1)).getKey());
            		String clave2 = ((String) ((Map.Entry) (o2)).getKey());
            		return ((Comparable) getNumMes(clave1)).compareTo(getNumMes(clave2));
            	} else if (type3) {
            		String clave1 = ((String) ((Map.Entry) (o1)).getKey()).split("-")[0];
            		String clave2 = ((String) ((Map.Entry) (o2)).getKey()).split("-")[0];
            		return ((Comparable) getNumMes(clave1)).compareTo(getNumMes(clave2));
            	}
            	return ((Comparable) ((Map.Entry) (o1)).getKey()).compareTo(((Map.Entry) (o2)).getKey());
             }
		});

		Map mapaOrdenado = new LinkedHashMap();
		for (Iterator it = lista.iterator(); it.hasNext();) {
		     Map.Entry entry = (Map.Entry)it.next();
		     mapaOrdenado.put(entry.getKey(), entry.getValue());
		}
		return mapaOrdenado;
	}

	/**
	 * Función que permite obtener la información de cada servicio web hijo.
	 * @param nombreFuncion
	 * @param args
	 * @return Lista con el ServicioWebInfo de cada servicio web hijo.
	 * @throws Throwable
	 */
	public static <T> List<ServicioWebInfo> invoke(String nombreFuncion, Object... args) throws Throwable {
		Class claseDelMetodoALlamar = null;
        List<Class> classes = Play.classloader.getAssignableClasses(WSController.class);
        ServicioWebInfo swi = new ServicioWebInfo();
        List<ServicioWebInfo> listaInfoWS = new ArrayList<ServicioWebInfo>();
        
        if(classes.size() != 0) {
        	for (int i = 0; i < classes.size(); i++) {
	        	claseDelMetodoALlamar = classes.get(i);
	        	if (!claseDelMetodoALlamar.getName().endsWith("Gen")) {
		        	try {
		            	swi = (ServicioWebInfo)Java.invokeStaticOrParent(claseDelMetodoALlamar, nombreFuncion, args);
		            	listaInfoWS.add(swi);
		            	
		            } catch(InvocationTargetException e) {
		            	throw e.getTargetException();
		            }
	        	}
        	}
        	return listaInfoWS;
        } else {
        	swi = (ServicioWebInfo)Java.invokeStatic(WSController.class, nombreFuncion, args);
        	listaInfoWS.add(swi);
        	return listaInfoWS;
        }
	}
	
	private static String getMes(String mes) {
		switch (Integer.parseInt(mes)) {
		case 1:
			return "Ene";
		case 2:
			return "Feb";
		case 3:
			return "Mar";
		case 4:
			return "Abr";
		case 5:
			return "May";
		case 6:
			return "Jun";
		case 7:
			return "Jul";
		case 8:
			return "Ago";
		case 9:
			return "Sept";
		case 10:
			return "Oct";
		case 11:
			return "Nov";
		case 12:
			return "Dic";
		default:
			break;
		}
		return null;
	}
	
	private static int getNumMes(String mes) {
		if (mes.equals("Ene"))
			return 1;
		else if (mes.equals("Feb"))
			return 2;
		else if (mes.equals("Mar"))
			return 3;
		else if (mes.equals("Abr"))
			return 4;
		else if (mes.equals("May"))
			return 5;
		else if (mes.equals("Jun"))
			return 6;
		else if (mes.equals("Jul"))
			return 7;
		else if (mes.equals("Ago"))
			return 8;
		else if (mes.equals("Sept"))
			return 9;
		else if (mes.equals("Oct"))
			return 10;
		else if (mes.equals("Nov"))
			return 11;
		else if (mes.equals("Dic"))
			return 12;
		return 0;
	}
}
