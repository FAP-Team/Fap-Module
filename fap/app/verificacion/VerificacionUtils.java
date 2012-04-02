package verificacion;

import java.util.ArrayList;
import java.util.List;

import models.Documento;
import models.SolicitudGenerica;
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
					if ((tipoEncontrado) || !vDoc.identificadorMultiple.equalsIgnoreCase("multiple")) {
						play.Logger.error("El tipo de documento <"+doc.tipo+"> ya había sido añadido en la misma verificacion y su cardinalidad es "+vDoc.identificadorMultiple);
					}
					
					vDoc.save();
					list.add(vDoc);
					tipoEncontrado = true;
				}
			}
			
			// Si el tipo de documento no fue encontrado en los que aporta
			if (tipoEncontrado = false) {
				
				// Si es OBLIGATORIO
				if ((tipoDoc.getObligatoriedad().equals(ObligatoriedadEnum.OBLIGATORIO))
					||(tipoDoc.getObligatoriedad().equals(ObligatoriedadEnum.IMPRESCINDIBLE))) {
					VerificacionDocumento vDoc = new VerificacionDocumento();
					vDoc.existe = false;
					vDoc.uriTipoDocumento = tipoDoc.getUri();
					vDoc.descripcion = tipoDoc.getIdentificador();
					if (existsDocumentoVerificacionAnterior(EstadosDocumentoVerificacionEnum.noProcede, verificacionesBefore, tipoDoc.getUri(), tramite.uri)
						|| existsDocumentoVerificacionAnterior(EstadosDocumentoVerificacionEnum.valido, verificacionesBefore, tipoDoc.getUri(), tramite.uri)) {
						vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noVerificado.name();
					} else {
						vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noProcede.name();
					}
					vDoc.save();
					list.add(vDoc);

				} 
				
				// TODO: Demás obligatoriedad
				
				
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
}
