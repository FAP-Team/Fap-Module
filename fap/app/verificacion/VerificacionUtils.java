package verificacion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	public static List<VerificacionDocumento> getVerificacionDocumentosFromNewDocumentos (List<Documento> listDoc, String uriTramite, List<Verificacion> verificacionesBefore) {
		
		Tramite tramite = (Tramite) Tramite.find("select t from Tramite t where t.uri=?", uriTramite).first();
		
		List<VerificacionDocumento> list = new ArrayList<VerificacionDocumento>();
		List<Documento> aux = new ArrayList<Documento>();
		aux.addAll(listDoc);
		
		List<TipoDocumentoEnTramite> listaTipos = TiposDocumentosClient.getTiposDocumentosAportadosCiudadano(tramite);
		for (TipoDocumentoEnTramite tipoDoc : listaTipos) {
			boolean tipoEncontrado = false;
			// Mejorar la implementación
			for (Documento doc: aux) {
				if (doc.tipo.trim().equals(tipoDoc.getUri())) {
					VerificacionDocumento vDoc = new VerificacionDocumento(doc);
					vDoc.existe = true;
					if (tipoDoc.getObligatoriedad() == ObligatoriedadEnum.CONDICIONADO_AUTOMATICO) {
						// TODO: Comprobar si se tenía que añadir o no
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
				// TODO: Demás obligatoriedad
				// Condicionado MANUAL, igual que el anterior pero siempre es NO VERIFICADO, ya que es el gestor/revisor quien se encarga de discernir si debe ser aportado o no
				if (tipoDoc.getObligatoriedad().equals(ObligatoriedadEnum.CONDICIONADO_MANUAL)){
					VerificacionDocumento vDoc = new VerificacionDocumento();
					vDoc.existe = false;
					vDoc.uriTipoDocumento = tipoDoc.getUri();
					vDoc.descripcion = TableKeyValue.getValue("tiposDocumentos", tipoDoc.getUri());
					vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noPresentado.name();
					vDoc.save();
					list.add(vDoc);
				} 
				// Condicionado AUTOMATICO, llamar a algun metodo (que debe ser manual de la aplicacion, sobreescrito) que nos diga si se debe incluir el documento o no
				/*if (tipoDoc.getObligatoriedad().equals(ObligatoriedadEnum.CONDICIONADO_AUTOMATICO)){
					VerificacionDocumento vDoc = new VerificacionDocumento();
					vDoc.existe = false;
					vDoc.uriTipoDocumento = tipoDoc.getUri();
					vDoc.descripcion = TableKeyValue.getValue("tiposDocumentos", tipoDoc.getUri());
					//if (//TODO: ¿El documento hay que incluirlo?)
					//	vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noVerificado.name();
					vDoc.save();
					list.add(vDoc);

				}*/
				
				// Si es múltiple
				if (tipoDoc.getCardinalidad().name().equalsIgnoreCase("multiple")) {
					
				} else {
					// Al no ser múltiple lo debo buscar en los anteriores, y si no está .... añadirlo
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
			if (verif.uriTramite.equals(uriTramite)) {
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
	 * Indica si existen documentos nuevos aportados por el solicitante y que no estan incluidos en la verificacion actual
	 * 
	 * @param verificacionActual La verificación que está en curso
	 * @param documentos La lista de documentos actuales que ha aportado el solicitante y no han sido verificados
	 * 
	 * @return True Si existe un documento nuevo aportado por el solicitante que no esté en la verificación actual
	 */
	public static boolean existDocumentoNuevo (Verificacion verificacionActual, List<VerificacionDocumento> documentos) {
		Set docActualesVerificacion = new HashSet();
		for (VerificacionDocumento vDoc: verificacionActual.documentos){
			if (vDoc.uriDocumento != null){
				docActualesVerificacion.add(vDoc.uriDocumento);
			}
		}
		for (VerificacionDocumento actualesDoc: documentos){
			if ((actualesDoc.uriDocumento != null) && (!docActualesVerificacion.contains(actualesDoc.uriDocumento))){
				return true;
			}
		}
		return false;
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
