package verificacion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import play.libs.F.Promise;

import reports.Report;

import controllers.fap.VerificacionFapController;

import messages.Messages;
import models.Documento;
import models.SolicitudGenerica;
import models.TableKeyValue;
import models.Tramite;
import models.Verificacion;
import models.VerificacionDocumento;
import aed.TiposDocumentosClient;
import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;
import es.gobcan.eadmon.procedimientos.ws.dominio.ObligatoriedadEnum;
import es.gobcan.eadmon.procedimientos.ws.dominio.TipoDocumentoEnTramite;
import es.gobcan.eadmon.verificacion.ws.dominio.EstadoDocumentoVerificacion;

public class VerificacionUtils {

	/**
	 * Devuelve la lista de documentos a verificar (presentes y no presentes) que no han sido verificados en forma de lista de VerificacionDocumentos.
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
		auxIterar.addAll(listDoc);
		
		List<TipoDocumentoEnTramite> listaTipos = TiposDocumentosClient.getTiposDocumentosAportadosCiudadano(tramite);
		// Documentos condicionados automaticos obligatorios de la aplicacion en cuestion
		List<String> docCondicionadosAutomaticos=new ArrayList<String>();
		try {
			docCondicionadosAutomaticos = VerificacionFapController.invoke("getDocumentosCondicionadosAutomaticos", tramite.nombre, idSolicitud);
		} catch (Throwable e) {
			play.Logger.warn("Fallo al recuperar la lista con los tipos de documentos condicionados automaticos: "+e);
		}
		for (TipoDocumentoEnTramite tipoDoc : listaTipos) {
			boolean tipoEncontrado = false;
			// Mejorar la implementación
			for (Documento doc: auxIterar) {
				if ((doc.tipo != null) && (doc.tipo.trim().equals(tipoDoc.getUri()))) {
					VerificacionDocumento vDoc = new VerificacionDocumento(doc);
					vDoc.existe = true;
					if (tipoDoc.getObligatoriedad() == ObligatoriedadEnum.CONDICIONADO_AUTOMATICO) {
						// Comprobar si se tenía que añadir o no
						if (docCondicionadosAutomaticos.contains(ObligatoriedadDocumentosFap.eliminarVersionUri(tipoDoc.getUri())))
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
			
			// Si el tipo de documento no fue encontrado en los que aporta
			if (!tipoEncontrado) {

				// Si es OBLIGATORIO
				if ((tipoDoc.getObligatoriedad().equals(ObligatoriedadEnum.OBLIGATORIO))
					||(tipoDoc.getObligatoriedad().equals(ObligatoriedadEnum.IMPRESCINDIBLE))) {
					VerificacionDocumento vDoc = new VerificacionDocumento();
					vDoc.existe = false;
					vDoc.uriTipoDocumento = tipoDoc.getUri();
					vDoc.identificadorMultiple = tipoDoc.getCardinalidad().name();
					System.out.println(tipoDoc.getUri());
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
				// Condicionado MANUAL, igual que el anterior pero siempre es NO VERIFICADO, ya que es el gestor/revisor quien se encarga de discernir si debe ser aportado o no
				if (tipoDoc.getObligatoriedad().equals(ObligatoriedadEnum.CONDICIONADO_MANUAL)){
					VerificacionDocumento vDoc = new VerificacionDocumento();
					vDoc.existe = false;
					vDoc.uriTipoDocumento = tipoDoc.getUri();
					vDoc.identificadorMultiple = tipoDoc.getCardinalidad().name();
					vDoc.descripcion = TableKeyValue.getValue("tiposDocumentos", tipoDoc.getUri());
					vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noVerificado.name();
					vDoc.save();
					list.add(vDoc);
				} 
				// Condicionado AUTOMATICO
				if (tipoDoc.getObligatoriedad().equals(ObligatoriedadEnum.CONDICIONADO_AUTOMATICO)){
					// Si el tipo de Documento está en la lista de los tipos de documentos obligatorios condicionados automaticos que obtenemos de la propia aplicacion
					// Quitamos la uri del tipo de documento porque esta quitada en la lista de condicionados automaticos, por lo que se debe quitar para comparar
					if (docCondicionadosAutomaticos.contains(ObligatoriedadDocumentosFap.eliminarVersionUri(tipoDoc.getUri()))){
						VerificacionDocumento vDoc = new VerificacionDocumento();
						vDoc.existe = false;
						vDoc.uriTipoDocumento = tipoDoc.getUri();
						vDoc.identificadorMultiple = tipoDoc.getCardinalidad().name();
						vDoc.descripcion = TableKeyValue.getValue("tiposDocumentos", tipoDoc.getUri());
						vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noVerificado.name();
						vDoc.save();
						list.add(vDoc);
					}
				}
			}
		}
		
		// Recorro todos los documentos no pertenecientes al trámite actual pero que se han aportado
		for (Documento docAux: aux){
			VerificacionDocumento vDoc = new VerificacionDocumento();
			vDoc.existe = false;
			vDoc.uriTipoDocumento = docAux.tipo;
			//vDoc.identificadorMultiple = 
			vDoc.uriDocumento = docAux.uri;
			vDoc.descripcion = docAux.descripcion;
			vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noProcede.name();
			vDoc.save();
			list.add(vDoc);
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
			documentosNuevos = (List<Documento>)VerificacionFapController.invoke("getNuevosDocumentosVerificar", verificacionActual.id, idSolicitud);
			documentosNuevosSinVerificacionActual = (List<Documento>)VerificacionFapController.invoke("getNuevosDocumentosVerificar", verificacionActual.id, idSolicitud);
		} catch (Throwable e) {
			play.Logger.error("Error recuperando los documentos nuevos a verificar", e);
		}
		for (Documento doc: documentosNuevos){
			for (VerificacionDocumento vDoc: verificacionActual.documentos){
				if ((vDoc.uriDocumento != null) && (vDoc.uriDocumento.equals(doc.uri))){
					documentosNuevosSinVerificacionActual.remove(doc);
					break;
				}
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
				if ((vtdoc.uri != null) && (vtdoc.uri.equals(doc.uri))){
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
	
}
