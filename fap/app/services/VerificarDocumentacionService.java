package services;

import java.util.ArrayList;
import java.util.List;
import messages.Messages;
import utils.ObligatoriedadDocumentosFap;
import models.TableKeyValue;
import models.Documento;
import models.Tramite;

/* Clase encargada de verificar que los documentos obligatorios, han sido aportados por el usuario.
 * Si esto no se cumple, mostrará los mensajes de error pertinentes acerca de aquellos tipos de documentos
 * que no ha aportado, para que pueda hacerlo, antes de proseguir con la fase en la que estaba
 */

public class VerificarDocumentacionService {

	// Clase que contendrá, en principio, todos los documentos obligatorios de dicho trámite
	//                    , y finalmente, todos los documentos obligatorios que el usuario NO ha aportado
	private static ObligatoriedadDocumentosFap docObligatoriedad;
	
	// Lista con todos los documentos que el usuario ha aportado
	private static List<Documento> lstDocumentosSubidos=new ArrayList<Documento>();

	// Constructor de la clase, que tiene dos parametros:
	// 		* tramite: De tipo Tramite, y especifica el trámite que se está utilizando, para saber a partir de él los documentos obligatorios
	// 		* lstDocumentosSubidos: Lista que contiene los documentos que el usuario a aportado    
	public VerificarDocumentacionService(Tramite tramite, List<Documento> lstDocumentosSubidos) {
		// Se calculan los documentos obligatorios a adjuntar, a raíz del trámite que nos llega por parámetros
		this.docObligatoriedad=new ObligatoriedadDocumentosFap(tramite);
		// Guardamos en una variable local a la clase, la lista de documentos que el usuario ha aportado, para despues comparar
		// con la lista de documentos obligatorios al trámite (docObligatoriedad), y así ver si falta o no algun documento obligatorio a aportar 
		this.lstDocumentosSubidos=lstDocumentosSubidos;
	}

	// Constructor de la clase, que tiene dos parametros:
	// 		* tramite: De tipo String, y servirá para especificas el trámite que se está utilizando, para saber a partir de él los documentos obligatorios
	//			** Es de tipo String, ya que después se conocerá el trámite en sí, consultando en la base de datos por el nombre del trámite que será este string
	// 		* lstDocumentosSubidos: Lista que contiene los documentos que el usuario a aportado 
	public VerificarDocumentacionService(String strTramite, List<Documento> lstDocumentosSubidos) {
		// Se calculan los documentos obligatorios a adjuntar, a raíz del trámite que nos llega por parámetros
		this.docObligatoriedad=new ObligatoriedadDocumentosFap(strTramite);
		// Guardamos en una variable local a la clase, la lista de documentos que el usuario ha aportado, para despues comparar
		// con la lista de documentos obligatorios al trámite (docObligatoriedad), y así ver si falta o no algun documento obligatorio a aportar 
		this.lstDocumentosSubidos=lstDocumentosSubidos;
	}

	/**
	 * Función que se encarga de corroborar que se han aportado todos los documentos necesarios, en caso negativo
	 * prepara los errores correspondientes para mostrarle al usuario
	 */
		
	public static void preparaPresentacionTramite(List<String> lstObligatoriosCondicionadosAutomatico) {
		// Se recalculan los documentos necesarios de obligatoriedad CONDICIONADOS_AUTOMATICOS, para filtrar los que ya han sido aportados
		excluirObligatoriosCondicionadosAutomaticosAportados(lstObligatoriosCondicionadosAutomatico);
		// Se comprueba los tipos de obligatoriedad documentos que el usuario ya ha aportado, para quedarse con los que NO ha aportado
		for (Documento doc : lstDocumentosSubidos) {
			if (doc.tipoCiudadano != null) {
				String tipo = eliminarVersionUri(doc.tipoCiudadano);
				// Si recorriendo todos los documentos que el usuario ha aportado
				// Es posible eliminar de la lista de obligatorios, el documento en cuestión,
				// es que es síntoma de que el documento estaba aportado.
				// Sino, por el contrario, el tipo de obligatoriedad documento no se borrará de nuestra lista de 
				// documentos obligatorios, ya que el usuario no lo ha aportado. De esta forma
				// la variable 'docObligatoriedad', después de este proceso, sólo contendrá
				// aquellos tipos de obligatoriedad de documentos que el usuario NO ha aportado y lo debería haber hecho.
				if(docObligatoriedad.imprescindibles.remove(tipo))
					continue;
				else if(docObligatoriedad.obligatorias.remove(tipo)) 
					continue;
				else if(docObligatoriedad.automaticas.remove(tipo)) 
					continue;
			}
		}

		// Una vez que se tienen todos los tipos de obligatoriedad de documentos obligatorios que el usuario NO ha aportado
		// Se generaran los mensajes de error correspondientes, para que el usuario se percate y subsane
		// este situación, para poder continuar con la fase en la que estaba inmerso.
		
		// Se comprueba si existen documentos de obligatoriedad IMPRESCINDIBLE NO APORTADOS
		if (!docObligatoriedad.imprescindibles.isEmpty()) {
			for (String uri : docObligatoriedad.imprescindibles) {
				String descripcion=((TableKeyValue) TableKeyValue.find("byKLike", "%" + uri + "%").first()).value;
				// Si NO ha aportado un determinado tipo de obligatoriedad de documento que sí debería haberlo hecho, se genera el error correspondiente
				Messages.error("Error: Pagina Documentación falta el documento \""+ descripcion + "\"");
			}
		}
		// Se comprueba si existen documentos de obligatoriedad OBLIGATORIOS NO APORTADOS
		if (!docObligatoriedad.obligatorias.isEmpty()) {
			for (String uri : docObligatoriedad.obligatorias) {
				String descripcion=((TableKeyValue) TableKeyValue.find("byKLike", "%" + uri + "%").first()).value;
				// Si NO ha aportado un determinado tipo de obligatoriedad de documento que sí debería haberlo hecho, se genera el error correspondiente
				Messages.error("Error: Pagina Documentación falta el documento \""+ descripcion + "\"");
			}
		}
		// Se comprueba si existen documentos de obligatoriedad AUTOMATICOS NO APORTADOS
		if (!docObligatoriedad.automaticas.isEmpty()) {
			for (String uri : docObligatoriedad.automaticas) {
				String descripcion=((TableKeyValue) TableKeyValue.find("byKLike", "%" + uri + "%").first()).value;
				// Si NO ha aportado un determinada obligatoriedad de documento que sí debería haberlo hecho, se genera el error correspondiente
				Messages.error("Error: Pagina Documentación falta el documento \""+ descripcion + "\"");
			}
		}
	}

	/* Función que se encarga de meter en la clase que gestiona todos los documentos obligatorios a aportar,
	 * los de obligatoriedad CONDICIONADOS_AUTOMÁTICOS, que el usuario no ha checkeado. Por ello, elimina previamente todos los que
	 * pudiera tener la clase al inicializarse (que lo hace a raíz del trámite), para meter aquellos que se
	 * han calculado previamente, en el controlador que llama a esta Clase. Esto es porque los CONDICIONADOS_AUTOMATICOS
	 * pueden variar entre aplicaciones distintas (dependiendo de las reglas de negocio), por ello a priori no se
	 * sabe qué documentos son los de esta obligatoriedad, y mucho menos cuales son los que el usuario no ha aportado exactamente.
	 * El que llame a este Servicio de VerificarDocumentacionService será el encargado de pasarle como argumento 
	 * a la función 'preparaPresentacionTramite', esa lista de documentos de obligatoriedad CONDICIONADOS_AUTOMATICOS, que el usuario
	 * debe aportar por diversos motivos y no ha hecho, y que tendrá que calcular.
	*/

	static void excluirObligatoriosCondicionadosAutomaticosAportados(List<String> lstExcludeNoVersion){
		docObligatoriedad.automaticas.clear();
		if(!((lstExcludeNoVersion==null) || (lstExcludeNoVersion.size()<1))){
			for (String tipo : lstExcludeNoVersion) {
				docObligatoriedad.automaticas.add(tipo);
			}
		}
	}

	// Para eliminar de la URI, la Versión, que no hará falta en el proceso de verificación de documentación
	static String eliminarVersionUri(String uri){
		return uri.substring(0,uri.length()-4);
	}
}

