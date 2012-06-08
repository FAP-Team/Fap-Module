package verificacion;

import java.util.ArrayList;
import java.util.List;

import models.FapModel;
import models.TipoDocumento;
import models.Tramite;

/* Clase que permite conocer los documentos obligatorios de un determinado trámite
 * Exactamente devuelve los documentos que son 'aportadoPor' el CIUDADANO y cuya obligatoriedad
 * es de tipo: OBLIGATORIO, CONDICIONADO_AUTOMATICO, IMPRESCINDIBLE y CONDICIONADO_MANUAL
 */

public class ObligatoriedadDocumentosFap extends FapModel{
	
	// Para saber el trámite sobre el que se está trabajando
	static public Tramite tramite;

	// Lista que contendrá los documentos IMPRESCINDIBLE al trámite
	public static List<String> imprescindibles;
	// Lista que contendrá los documentos OBLIGATORIO al trámite
	public static List<String> obligatorias;
	// Lista que contendrá los documentos CONDICIONADO_AUTOMATICO al trámite
	public static List<String> automaticas;
	// Lista que contendrá los documentos CONDICIONADO_MANUALES al trámite
	public static List<String> manuales;

	// Constructor de la clase que tiene como parámetros el tramite, para calcular los documentos
	// que son obligatorios en él.
	public ObligatoriedadDocumentosFap(Tramite tramite) {
		// Guardamos localmente, el trámite sobre el que trabajará la instancia de la clase
		this.tramite=tramite;
		// Se calculan los documentos obligatorios de dicho trámite
		this.initObligatoriedad();
	}

	// Constructor de la clase que tiene como parámetros el nombre del tramite, para calcular los documentos
	// que son obligatorios en él.
	public ObligatoriedadDocumentosFap(String strTramite) {
		// Guardamos localmente, el trámite sobre el que trabajará la instancia de la clase.
		// Este tramite lo calcularemos a partir de su nombre, que es el que nos pasan por parametro, mediante una
		// consulta a la base de datos
		this.tramite=Tramite.find("select tramite from Tramite tramite where nombre='" + strTramite + "'").first();
		// Se calculan los documentos obligatorios de dicho trámite
		this.initObligatoriedad();
	}

	// Función que se encarga de llamar al resto de funciones de la clase, que se encargan de obtener los documentos
	// obligatorios del trámite en cuestión
	public void initObligatoriedad(){
		// Nos cercioramos de que las listas que contendran los documentos obligatorios estén limpias, o sea vacías
		this.clear();
		// Obtenemos los documentos IMPRESCINDIBLE, almacenandose en la lista local de la clase, pertinente
		this.ObtenerDocumentosImprescindibles();
		// Obtenemos los documentos OBLIGATORIO, almacenandose en la lista local de la clase, pertinente
		this.ObtenerDocumentosObligatorios();
		// Obtenemos los documentos CONDICIONADO_AUTOMATICO, almacenandose en la lista local de la clase, pertinente
		this.ObtenerDocumentosAutomaticos();
		// Obtenemos los documentos CONDICIONADO_MANUAL, almacenandose en la lista local de la clase, pertinente
		this.ObtenerDocumentosManual();
	}

	// Función que a través del trámite sobre el que se está trabajando en la clase
	// Recupera los documentos aportados por el CIUDADANO y que sean OBLIGATORIOS
	// Almacenandolos en la lista local de la clase para tal efecto
	static void ObtenerDocumentosObligatorios(){
		for (TipoDocumento td : tramite.documentos) {
			if (td.aportadoPor.toUpperCase().equals("CIUDADANO")){
				if(td.obligatoriedad.toUpperCase().equals("OBLIGATORIO")){
					obligatorias.add(eliminarVersionUri(td.uri));
				}
			}
		}
	}
	
	// Función que a través del trámite sobre el que se está trabajando en la clase
	// Recupera los documentos aportados por el CIUDADANO y que sean CONDICIONADO_AUTOMATICO
	// Almacenandolos en la lista local de la clase para tal efecto
	static void ObtenerDocumentosAutomaticos(){
		for (TipoDocumento td : tramite.documentos) {
			if (td.aportadoPor.toUpperCase().equals("CIUDADANO")){
				if(td.obligatoriedad.toUpperCase().equals("CONDICIONADO_AUTOMATICO")){
					automaticas.add(eliminarVersionUri(td.uri));
				}
			}
		}
	}
	
	// Función que a través del trámite sobre el que se está trabajando en la clase
	// Recupera los documentos aportados por el CIUDADANO y que sean IMPRESCINDIBLE
	// Almacenandolos en la lista local de la clase para tal efecto
	static void ObtenerDocumentosImprescindibles(){
		for (TipoDocumento td : tramite.documentos) {
			if (td.aportadoPor.toUpperCase().equals("CIUDADANO")){
				if(td.obligatoriedad.toUpperCase().equals("IMPRESCINDIBLE")){
					imprescindibles.add(eliminarVersionUri(td.uri));
				}
			}
		}
	}
	
	// Función que a través del trámite sobre el que se está trabajando en la clase
	// Recupera los documentos aportados por el CIUDADANO y que sean CONDICIONADO_MANUAL
	// Almacenandolos en la lista local de la clase para tal efecto
	static void ObtenerDocumentosManual(){
		for (TipoDocumento td : tramite.documentos) {
			if (td.aportadoPor.toUpperCase().equals("CIUDADANO")){
				if(td.obligatoriedad.toUpperCase().equals("CONDICIONADO_MANUAL")){
					manuales.add( eliminarVersionUri(td.uri));
				}
			}
		}
	}

	// Función encargada de dejar todoas las listas locales de la clase, vacías
	// Comprabando que si no existe la instancia de una determinada lista, pues la
	// crea de la forma oportuna
	public void clear(){
		if (imprescindibles == null)
			imprescindibles = new ArrayList<String>();
		else
			imprescindibles.clear();
		
		if (obligatorias == null)
			obligatorias = new ArrayList<String>();
		else
			obligatorias.clear();
		
		if (automaticas == null)
			automaticas = new ArrayList<String>();
		else
			automaticas.clear();
		
		if (manuales == null) 
			manuales = new ArrayList<String>();
		else
			manuales.clear();
	}

	// Para eliminar de la URI, la Versión, que no hará falta en el proceso de obtener la documentación obligatoria al trámite
	static String eliminarVersionUri(String uri){
		return uri.substring(0,uri.length()-4);
	}
	
}
