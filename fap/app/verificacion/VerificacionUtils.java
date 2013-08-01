package verificacion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.joda.time.DateTime;

import play.libs.F.Promise;
import properties.FapProperties;

import reports.Report;
import services.FirmaService;
import services.GestorDocumentalService;
import services.aed.ProcedimientosService;
import services.filesystem.TipoDocumentoEnTramite;

import config.InjectorConfig;
import controllers.fap.VerificacionFapController;

import messages.Messages;
import models.Documento;
import models.RegistroModificacion;
import models.SolicitudGenerica;
import models.TableKeyValue;
import models.TipoDocumento;
import models.Tramite;
import models.Verificacion;
import models.VerificacionDocumento;
import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;
import es.gobcan.eadmon.procedimientos.ws.dominio.ObligatoriedadEnum;
import es.gobcan.eadmon.verificacion.ws.dominio.EstadoDocumentoVerificacion;

public class VerificacionUtils {

	/**
	 * Devuelve la lista de documentos a verificar (presentes y no presentes) que no han sido verificados en forma de lista de VerificacionDocumentos.
	 * 
	 * 1ª Verificación del trámite actual:
	 *    - Lista con todos los documentos aportados y no aportados.
	 * 2ª o posteriores verificaciones de un trámite:
	 *    - Añadimos los nuevo documentos presentados con  estado ="No verificado"
	 *    - Copiamos de la verificación anterior de ese trámite todos los documentos  "No válido"
	 *       y "No presentado" con sus respectivos motivos y códigos de requerimiento que no se han
	 *       presentado ahora.
	 * 
	 * @param listDoc Lista de Documentos (verificados y no verificados)
	 * @return Lista de VerificaciónDocumentos (sólo los no verificados)
	 */
	public static List<VerificacionDocumento> getVerificacionDocumentosFromNewDocumentos (List<Documento> listDoc, String uriTramite, List<Verificacion> verificacionesBefore, Long idSolicitud) {
		
		Tramite tramite = (Tramite) Tramite.find("select t from Tramite t where t.uri=?", uriTramite).first();
		
		List<VerificacionDocumento> list = new ArrayList<VerificacionDocumento>();
		List<Documento> aux = new ArrayList<Documento>();
		List<Documento> auxIterar = new ArrayList<Documento>();
		aux.addAll(listDoc);
		System.out.println("Lista de documentos verif y sin verif: "+listDoc.size());
		auxIterar.addAll(listDoc);
		
		GestorDocumentalService gestorDocumental = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		
		/// Comprobamos si existenVerificaciones de este mismo trámite anteriormente
		Verificacion verificacionAnterior = null;
		if (verificacionesBefore != null && !verificacionesBefore.isEmpty()) {
			for (Verificacion auxVerificacion : verificacionesBefore) {
				if (auxVerificacion.uriTramite.equals(uriTramite)) {
					verificacionAnterior = auxVerificacion;
				}
			}
		}
		
		SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(idSolicitud);
		//Comprobamos si la solicitud ha tenido modificaciones
				Boolean hayModificaciones = false;
				if ((dbSolicitud.registroModificacion != null) && (!dbSolicitud.registroModificacion.isEmpty())){
					hayModificaciones = true;
				}

				//Tener en cuenta que hay que añadir los doc de SolicitudModificacion si he tenido modificaciones (Solo de la última)
				if (hayModificaciones){
					RegistroModificacion ultimoRegistroModificacion = obtenerUltimoRegistroModificacionRegistrado(dbSolicitud);
					if (ultimoRegistroModificacion != null){
						VerificacionDocumento vDoc = new VerificacionDocumento(ultimoRegistroModificacion.registro.oficial);
						vDoc.existe = true;		
						vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noVerificado.name();
						TipoDocumento tipoDocAux = TipoDocumento.find("select tipo from TipoDocumento tipo where tipo.uri=?", ultimoRegistroModificacion.registro.justificante.tipo).first();
						if (tipoDocAux != null) 
							vDoc.identificadorMultiple = tipoDocAux.cardinalidad;
						vDoc.save();
						list.add(vDoc);
						listDoc.add(ultimoRegistroModificacion.registro.oficial);
					}
				}
	
		/// Si verificacionAnterior == null, NO tiene verificaciones anteriores en ese trámite
		List<TipoDocumentoEnTramite> listaTipos = new ArrayList<TipoDocumentoEnTramite>();
		if (verificacionAnterior == null) {
			play.Logger.info("No existen verificaciones anteriores para la solicitud "+idSolicitud+" del trámite "+uriTramite);
			listaTipos = gestorDocumental.getTiposDocumentosAportadosCiudadano(tramite);
			
			// Documentos condicionados automaticos obligatorios de la aplicacion en cuestion
			List<String> docCondicionadosAutomaticosNoAportados=new ArrayList<String>();
			try {
				docCondicionadosAutomaticosNoAportados = VerificacionFapController.invoke(VerificacionFapController.class, "getDocumentosNoAportadosCondicionadosAutomaticos", tramite.nombre, idSolicitud);
			} catch (Throwable e) {
				play.Logger.error("Fallo al recuperar la lista con los tipos de documentos condicionados automaticos: "+e);
			}
			System.out.println("Lista de tipos: "+listaTipos.size());
			for (TipoDocumentoEnTramite tipoDoc : listaTipos) {
				boolean tipoEncontrado = false;
				// Mejorar la implementación
				for (Documento doc: auxIterar) {
					if ((doc.tipo != null) && (doc.tipo.trim().equals(tipoDoc.getUri()))) {
						VerificacionDocumento vDoc = new VerificacionDocumento(doc);
						vDoc.existe = true;
						if (tipoDoc.getObligatoriedad() == ObligatoriedadEnum.CONDICIONADO_AUTOMATICO) {
							// Comprobar si se tenía que añadir o no
							if ((docCondicionadosAutomaticosNoAportados != null) && (docCondicionadosAutomaticosNoAportados.size() != 0) 
									&& docCondicionadosAutomaticosNoAportados.contains(ObligatoriedadDocumentosFap.eliminarVersionUri(tipoDoc.getUri())))
								vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noVerificado.name();
							else
								vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noProcede.name();
						} else {
							vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noVerificado.name();
						}
						vDoc.identificadorMultiple = tipoDoc.getCardinalidad().name();
						//vDoc.etiquetaTipoDocumento
						if ((tipoEncontrado) && !vDoc.identificadorMultiple.equalsIgnoreCase("multiple")) {
							play.Logger.error("El tipo de documento <"+doc.tipo+"> ya había sido añadido en la misma verificacion y su cardinalidad es "+vDoc.identificadorMultiple);
						}
						vDoc.save();
						list.add(vDoc);
						aux.remove(doc);
						tipoEncontrado = true;
					}
				}
				
				// Si fue encontrado, pero su cardinalidad es múltiple, debo añadir un VerificacionDocumento más, con "NoProcede"
//				if (tipoEncontrado && tipoDoc.getCardinalidad().name().equalsIgnoreCase("multiple")) {
//					play.Logger.info("Encontrado un tipo de documento con cardinalidad Multiple (Add NoProcede)");
//					VerificacionDocumento vDoc = new VerificacionDocumento();
//					vDoc.existe = false;
//					vDoc.uriTipoDocumento = tipoDoc.getUri();
//					vDoc.identificadorMultiple = tipoDoc.getCardinalidad().name();
//					vDoc.descripcion = TableKeyValue.getValue("tiposDocumentos", tipoDoc.getUri());
//					vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noProcede.name();
//					vDoc.save();
//					list.add(vDoc);
//				}
				
				// Si el tipo de documento no fue encontrado en los que aporta
				if (!tipoEncontrado) {
					// Si es OBLIGATORIO
					if ((tipoDoc.getObligatoriedad().equals(ObligatoriedadEnum.OBLIGATORIO))
						||(tipoDoc.getObligatoriedad().equals(ObligatoriedadEnum.IMPRESCINDIBLE))) {
						VerificacionDocumento vDoc = new VerificacionDocumento();
						vDoc.existe = false;
						vDoc.uriTipoDocumento = tipoDoc.getUri();
						vDoc.identificadorMultiple = tipoDoc.getCardinalidad().name();
						vDoc.descripcion = TableKeyValue.getValue("tiposDocumentos", tipoDoc.getUri());
						if (existsDocumentoVerificacionAnterior(EstadosDocumentoVerificacionEnum.noProcede, verificacionesBefore, tipoDoc.getUri(), tramite.uri)
							|| existsDocumentoVerificacionAnterior(EstadosDocumentoVerificacionEnum.valido, verificacionesBefore, tipoDoc.getUri(), tramite.uri)) {
							vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noVerificado.name();
						} else {
							vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noPresentado.name();
						}
						vDoc.save();
						list.add(vDoc);
	
					}
					else if (tipoDoc.getObligatoriedad().equals(ObligatoriedadEnum.CONDICIONADO_MANUAL)){
						VerificacionDocumento vDoc = new VerificacionDocumento();
						vDoc.existe = false;
						vDoc.uriTipoDocumento = tipoDoc.getUri();
						vDoc.identificadorMultiple = tipoDoc.getCardinalidad().name();
						vDoc.descripcion = TableKeyValue.getValue("tiposDocumentos", tipoDoc.getUri());
						vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noPresentado.name();
						vDoc.save();
						list.add(vDoc);
					} 
					// Condicionado AUTOMATICO
					else if (tipoDoc.getObligatoriedad().equals(ObligatoriedadEnum.CONDICIONADO_AUTOMATICO)){
						VerificacionDocumento vDoc = new VerificacionDocumento();
						vDoc.existe = false;
						vDoc.uriTipoDocumento = tipoDoc.getUri();
						vDoc.identificadorMultiple = tipoDoc.getCardinalidad().name();
						vDoc.descripcion = TableKeyValue.getValue("tiposDocumentos", tipoDoc.getUri());
						// Si el tipo de Documento está en la lista de los tipos de documentos obligatorios condicionados automaticos que obtenemos de la propia aplicacion
						// Quitamos la uri del tipo de documento porque esta quitada en la lista de condicionados automaticos, por lo que se debe quitar para comparar
						if ((docCondicionadosAutomaticosNoAportados != null) && (docCondicionadosAutomaticosNoAportados.size() != 0) 
							&& docCondicionadosAutomaticosNoAportados.contains(ObligatoriedadDocumentosFap.eliminarVersionUri(tipoDoc.getUri()))){
							vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noPresentado.name();
						} else {
							vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noProcede.name();
						}
						vDoc.save();
						list.add(vDoc);
					}
				}
			}
			
			// Recorro todos los documentos no pertenecientes al trámite actual pero que se han aportado
			for (Documento docAux: aux){
				VerificacionDocumento vDoc = new VerificacionDocumento(docAux);
				vDoc.existe = true;
				vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noProcede.name();
				vDoc.save();
				list.add(vDoc);
			}
	
		} else {
			/// Tiene verificaciones anteriores de este trámite y está en verificacionAnterior
			play.Logger.info("Existe al menos una verificación anterior "+verificacionAnterior.id+" de la solicitud "+idSolicitud+" para trámite "+uriTramite);
			
			// Añadimos todos los documentos que se aportaron ahora
			for (Documento doc: listDoc) {
				VerificacionDocumento vDoc = new VerificacionDocumento(doc);
				vDoc.existe = true;
				TipoDocumento tipoDocAux = TipoDocumento.find("select tipo from TipoDocumento tipo where tipo.uri=?", doc.tipo).first();
				if (tipoDocAux != null) {
					vDoc.identificadorMultiple = tipoDocAux.cardinalidad;
			    } else {
					play.Logger.error("No existe el tipo de documento para el tipo: "+doc.tipo+". Se seteará por defecto a UNICO.");
					vDoc.identificadorMultiple = "UNICO";
				}
				vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noVerificado.name();
				vDoc.save();
				list.add(vDoc);
			}
			
			// De la verificación anterior copiamos los No Validos y los No Presentados que no
			// hayan sido añadidos ahora
			for (VerificacionDocumento docVerif : verificacionAnterior.documentos) {
				if (docVerif.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noPresentado.name())
						|| docVerif.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noValido.name())) {
					boolean findActual = false;
					for (Documento doc: listDoc) { 
						if (ObligatoriedadDocumentosFap.eliminarVersionUri(docVerif.uriTipoDocumento).equals(ObligatoriedadDocumentosFap.eliminarVersionUri(doc.tipo))) {
							findActual = true;
							break;
						}
					}
//					if ((!findActual) && (hayModificaciones) && (docVerif.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noValido.name())) 
//							&& (docVerif.uriTipoDocumento.equals(FapProperties.get("fap.aed.tiposdocumentos.solicitud.modificacion.modificacion")))){
//						//Si soy la presentacion de solicitud NO modificacion: marcar como verificado y no añadir
//						docVerif.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.valido.name();
//						docVerif.save();
//						findActual = true; //Así no vuelve a mostrarse
//					}
					
					if (!findActual) {
						VerificacionDocumento newVerDoc = new VerificacionDocumento(docVerif);
						newVerDoc.save();
						list.add(newVerDoc);
					}
				}
			}
			
		}
		return list;
	}

	/**
	 * Indica si existe un documento con dicho tipo en las verificaciones anteriores de ese mismo trámite.
	 * 
	 * @param listVerificacion Lista de verificaciones anteriores donde buscar.
	 * @param uriTipo Tipo del documento.
	 * @param uriTramite Uri del trámite sobre el que se busca.
	 * 
	 * @return True Si existe el documento en verificaciones anteriores
	 */
	public static boolean existsDocumentoVerificacionAnterior (List<Verificacion> listVerificacion, String uriTipo, String uriTramite) {
		play.Logger.info("Buscamos el tipo de documento <"+uriTipo+"> en verificaciones anteriores del trámite <"+uriTramite+">");
		for (Verificacion verif: listVerificacion) {
			if (verif.uriTramite.equals(uriTramite)) {
				for (VerificacionDocumento vDoc: verif.documentos) {
					if (vDoc.uriTipoDocumento.equals(uriTipo)) {
						if (vDoc.existe) {
							play.Logger.info("Existe -> "+vDoc.uriDocumento);
							return true;
						}
					}
				}
			}
		}
		play.Logger.info("NO existen documentos del tipo de documento "+uriTipo);
		return false;
	}
	
	/**
	 * Indica si el documento fue verificado como "resultado" con dicho tipo en las verificaciones anteriores de ese mismo trámite.
	 * 
	 * @param resultado Resultado de la verificacion anterior
	 * @param listVerificacion Lista de verificaciones anteriores donde buscar.
	 * @param uriTipo Tipo del documento.
	 * @param uriTramite Uri del trámite sobre el que se busca.
	 * 
	 * @return True Si existe el documento en verificaciones anteriores
	 */
	public static boolean existsDocumentoVerificacionAnterior (EstadosDocumentoVerificacionEnum resultado, List<Verificacion> listVerificacion, String uriTipo, String uriTramite) {
		play.Logger.info("Buscamos el tipo de documento <"+uriTipo+"> con resultado <"+resultado.name()+"> en verificaciones anteriores del trámite <"+uriTramite+">");
		for (Verificacion verif: listVerificacion) {
			if ((verif.uriTramite != null) && (verif.uriTramite.equals(uriTramite))) {
				for (VerificacionDocumento vDoc: verif.documentos) {
					if (vDoc.uriTipoDocumento.equals(uriTipo)) {
						if (vDoc.existe && (vDoc.estadoDocumentoVerificacion.equals(resultado.name()))) {
							play.Logger.info("Existe -> "+vDoc.uriDocumento);
							return true;
						}
					}
				}
			}
		}
		play.Logger.info("NO existen documentos del tipo de documento "+uriTipo);
		return false;
	}
	
	/**
	 * Indica si existe algun documento no verificado, en la verificacion actual
	 * 
	 * @param verificacionActual La verificación que está en curso
	 * 
	 * @return True Si existe un documento de la verificación actual que está No Verificado
	 */
	public static boolean existsDocumentoNoVerificado (Verificacion verificacionActual) {
	    for (VerificacionDocumento vDoc: verificacionActual.documentos) {
		   if (vDoc.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noVerificado.name())) {
			   return true;
		   }
		}
		return false;
	}
	
	/**
	 * Indica si existen documentos nuevos aportados por el solicitante y que no estan incluidos en la verificacion actual, ni en anteriores
	 * 
	 * @param verificacionActual La verificación que está en curso
	 * 
	 * @return documentosNuevos Lista con los documentos nuevos que ha aportado el solicitante y no han sido incluidos en ninguna verificacion
	 */
	public static List<Documento> existDocumentosNuevos (Verificacion verificacionActual, Long idSolicitud) {
		List<Documento> documentosNuevos=null;
		List <Documento> documentosNuevosSinVerificacionActual = null;
		try {
			documentosNuevos = (List<Documento>)VerificacionFapController.invoke(VerificacionFapController.class, "getNuevosDocumentosVerificar", verificacionActual.id, idSolicitud);
			documentosNuevosSinVerificacionActual = (List<Documento>)VerificacionFapController.invoke(VerificacionFapController.class, "getNuevosDocumentosVerificar", verificacionActual.id, idSolicitud);
		} catch (Throwable e) {
			e.printStackTrace();
			play.Logger.error("Error recuperando los documentos nuevos a verificar", e.getMessage());
		}
		for (Documento doc: documentosNuevos){
			for (VerificacionDocumento vDoc: verificacionActual.documentos){
				if ((vDoc.uriDocumento != null) && (vDoc.uriDocumento.equals(doc.uri))){
					documentosNuevosSinVerificacionActual.remove(doc);
					break;
				}
			}
		}
		
		//AQuí tener en cuenta como "Nuevo documento" el de la solicitud de modificacion si la hay
		SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(idSolicitud);
		
		//Comprobamos si la solicitud ha tenido modificaciones
		Boolean hayModificaciones = false;
		if ((dbSolicitud.registroModificacion != null) && (!dbSolicitud.registroModificacion.isEmpty())){
			hayModificaciones = true;
		}
		//Tener en cuenta que hay que añadir los doc de SolicitudModificacion si he tenido modificaciones (Solo de la última)
		if (hayModificaciones){
			RegistroModificacion ultimoRegistroModificacion = obtenerUltimoRegistroModificacionRegistrado(dbSolicitud);
			if (ultimoRegistroModificacion != null){
				documentosNuevosSinVerificacionActual.add(ultimoRegistroModificacion.registro.oficial);
			}
		}
		
		return documentosNuevosSinVerificacionActual;
	}
	
	/**
	 * Indica si existen documentos nuevos aportados por el solicitante y que no estan incluidos en la verificacion actual, ni en anteriores, ni en la verificacion de tipos actual
	 * 
	 * @param verificacionActual La verificación que está en curso
	 * @param verificaciones Las verificaciones anteriores ya finalizadas
	 * @param documentosActuales La lista de documentos actuales que ha aportado el solicitante
	 * 
	 * @return documentosNuevos Lista con los documentos nuevos que ha aportado el solicitante y no han sido incluidos en ninguna verificacion
	 */
	public static List<Documento> existDocumentosNuevosVerificacionTipos (Verificacion verificacionActual, List<Verificacion> verificaciones, List<Documento> documentosActuales, Long idSolicitud) {
		List <Documento> documentos = existDocumentosNuevos (verificacionActual, idSolicitud);
		List <Documento> documentosNuevos = existDocumentosNuevos (verificacionActual, idSolicitud);
		for (Documento vtdoc: verificacionActual.verificacionTiposDocumentos){
			for (Documento doc: documentos){
				if ((vtdoc.uri != null) && (doc.uri != null) && (vtdoc.uri.equals(doc.uri))){
					documentosNuevos.remove(doc);
					break;
				}
			}
		}
		return documentosNuevos;
	}
	
	/**
	 * Indica si todos los documentos de la verificacion estan correctamente (no procede o valido)
	 * 
	 * @param verificacionActual La verificación que está en curso
	 * 
	 * @return True Si todos los documentos estan correctamente (en estado no procede o valido)
	 */
	public static boolean documentosValidos (Verificacion verificacionActual) {
		for (VerificacionDocumento vDoc: verificacionActual.documentos){
			if (!(vDoc.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.valido.name())) && !((vDoc.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noProcede.name())))){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Indica si existe algun documento en la verificacion en estado No presentado o no valido
	 * 
	 * @param verificacionActual La verificación que está en curso
	 * 
	 * @return True Si algun documento esta en estado No presentado o no valido
	 */
	public static boolean documentosIncorrectos (Verificacion verificacionActual) {
		for (VerificacionDocumento vDoc: verificacionActual.documentos){
			if ((vDoc.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noValido.name())) || ((vDoc.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noPresentado.name())))){
				return true;
			}
		}
		return false;
	}
	
	// Función que a través del trámite sobre el que se está trabajando en la clase
	// Recupera los documentos aportados por el CIUDADANO y que sean CONDICIONADO_AUTOMATICO
	// Almacenandolos en la lista local de la clase para tal efecto
	public static List<String> ObtenerDocumentosAutomaticos(Tramite tramite){
		List<String> lista = new ArrayList<String>();
		for (TipoDocumento td : tramite.documentos) {
			if (td.aportadoPor.toUpperCase().equals("CIUDADANO")){
				if(td.obligatoriedad.toUpperCase().equals("CONDICIONADO_AUTOMATICO")){
					lista.add(eliminarVersionUri(td.uri));
				}
			}
		}
		return lista;
	}
	// Para eliminar de la URI, la Versión, que no hará falta en el proceso de obtener la documentación obligatoria al trámite
	public static String eliminarVersionUri(String uri) {
		String PATTERN_VERSION_URI = "(.*)/v[0-9][0-9]$";
		Pattern p = Pattern.compile(PATTERN_VERSION_URI);
		Matcher m = p.matcher(uri);
		if (m.find())
			return m.group(1);
		return uri;
 	}
	
	/**
	 * Establece el campo verificado de los documentos a true
	 * 
	 * @param vDocs VerificacionDocumentos verificados
	 * @param docs Documentos donde deberá buscar para setear los anteriores
	 */
	public static void setVerificadoDocumentos (List<VerificacionDocumento> vDocs, List<Documento> docs) {
		for (VerificacionDocumento vDoc : vDocs) {
			for (Documento docu: docs) {
				if ((docu.uri != null) && (vDoc.uriDocumento != null) && (docu.uri.equals(vDoc.uriDocumento))){
					docu.verificado=true;
					break;
				}
			}
		}
	}
	
	public static void setVerificadoDocumento (List<VerificacionDocumento> vDocs, Documento docu) {
		for (VerificacionDocumento vDoc : vDocs) {
			if ((docu.uri != null) && (vDoc.uriDocumento != null) && (docu.uri.equals(vDoc.uriDocumento))){
				docu.verificado=true;
				break;
			}
		}
	}
	
	public static RegistroModificacion obtenerUltimoRegistroModificacionRegistrado(SolicitudGenerica solicitud){
		//Obtener la última modificacion 
		Boolean encontrado = false;
		RegistroModificacion ultimoRegistroModificacion = new RegistroModificacion();
		//Fecha del ppio de los tiempos
		ultimoRegistroModificacion.fechaRegistro = new DateTime(0, 1, 1, 1, 1);
		for (RegistroModificacion registroModificacion : solicitud.registroModificacion) {
			//Solo se trabaja con los registros REGISTRADOS
			if ((ultimoRegistroModificacion.fechaRegistro != null) && (registroModificacion.fechaRegistro != null)
					&& (registroModificacion.fechaRegistro.isAfter(ultimoRegistroModificacion.fechaRegistro))){
				ultimoRegistroModificacion = registroModificacion;
				encontrado = true;
			}
		}
		if (encontrado)
			return ultimoRegistroModificacion;
		return null;
	}
	
}
