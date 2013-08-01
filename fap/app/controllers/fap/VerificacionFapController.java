package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Documento;
import models.ObligatoriedadDocumentos;
import models.SolicitudGenerica;
import models.Tramite;
import models.Verificacion;
import models.VerificacionDocumento;

import play.Play;
import play.utils.Java;
import properties.FapProperties;
import verificacion.ObligatoriedadDocumentosFap;
import verificacion.VerificacionUtils;

public class VerificacionFapController extends InvokeClassController{
	
	
	/**
	 * Método que devuelve la lista de documentos a verificar en una determinada verificacion
	 * @param idVerificacion Verificacion actual sobre la que vamos a ejercer todas las operaciones
	 * @return Lista con los Documentos que se quieran verificar
	 */
	public static List<Documento> getNuevosDocumentosVerificar(Long idVerificacion, Long idSolicitud){
		List<Documento> nuevosDocumentos = new ArrayList<Documento>();
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		Verificacion verificacion = Verificacion.findById(idVerificacion);
		// Todos los documentos de solicitud.documentacion.documentos que tenga el verificado a false + solicitud.registro.oficial si tramite es solicitud y verificado = false
		for (Documento doc: solicitud.documentacion.documentos){
			if ((doc.verificado == null) || (!doc.verificado))
				nuevosDocumentos.add(doc);
		}
		if  ((solicitud.registroModificacion.isEmpty()) && (solicitud.registro.oficial.uri != null) && ((verificacion.uriTramite != null) && (verificacion.uriTramite.equals(FapProperties.get("fap.aed.procedimientos.tramite.uri")))) && ((solicitud.registro.oficial.verificado == null) || ((solicitud.registroModificacion.isEmpty()) && (!solicitud.registro.oficial.verificado)))){
			nuevosDocumentos.add(solicitud.registro.oficial);
		}
		return nuevosDocumentos;
	}
	
	/**
	 * Método que devuelve si existen nuevos documentos a verificar, en la verificación actual que no han sido aún incluidos
	 * @return True si existen nuevos Documentos a verificar
	 */
	public static boolean isNuevosDocumentos(Long idVerificacion, Long idSolicitud){
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		Set documentosVerificaciones = new HashSet();
		for (Verificacion verificaciones: solicitud.verificaciones){
			for (VerificacionDocumento vDoc: verificaciones.documentos){
				if (vDoc.uriDocumento != null)
					documentosVerificaciones.add(vDoc.uriDocumento);
			}
		}
		for (VerificacionDocumento vDoc: solicitud.verificacion.documentos){
			if (vDoc.uriDocumento != null)
				documentosVerificaciones.add(vDoc.uriDocumento);
		}
		for (Documento doc: solicitud.documentacion.documentos){
			if ((doc.uri != null) && (!containsSinVersion(documentosVerificaciones, doc.uri))){
				return true;
			}
		}
		return false;
	}
	
	private static boolean containsSinVersion (Set<String> documentos, String uriDoc) {
		if ((uriDoc == null) || (uriDoc.trim().isEmpty()))
			return false;
		for (String aux : documentos) {
			if (VerificacionUtils.eliminarVersionUri(uriDoc).equals(VerificacionUtils.eliminarVersionUri(aux)))
				return true;
		}
		return false;
	}
	
	
	/**
	 * Método que setea a verificados todos los documentos de una lista pertenecientes a un trámite
	 * @param idVerificacion Verificacion actual sobre la que vamos a ejercer todas las operaciones
	 * @param documentos Lista de documentos que se setearan como verificados
	 */
	public static void setDocumentosVerificados(Long idVerificacion, List<Documento> documentos){
		for (Documento doc: documentos){
			doc.verificado=true;
		}
	}

	/**
	 * Método que permite saber que documentos condicionados automáticos son los correspondientes a una determinada aplicacion
	 * @return Lista con los tipos de documentos condicionados automaticos obligatorios de dicha aplicacion
	 * @throws Throwable
	 */
	public static List<String> getDocumentosNoAportadosCondicionadosAutomaticos(String tramite, Long idSolicitud) throws Throwable  {
		play.Logger.info("No hay ninguna llamada para calcular los documentos condicionados automaticos");
    	play.Logger.info("Se incluirán todos los documentos condicionados automáticos por defecto");
		// Devolver todos los CONDICIONADOS AUTOMATICOS
    	List<String> docObli = new ArrayList<String>();
    	try{
    		Tramite tr = Tramite.find("select tramite from Tramite tramite where nombre=?", tramite).first();
    		docObli = VerificacionUtils.ObtenerDocumentosAutomaticos(tr);
    	} catch (Exception e){
    		play.Logger.warn("Fallo al recuperar la lista con los tipos de documentos condicionados automaticos: "+e);
    		return new ArrayList<String>();
    	}
    	if ((docObli != null)){
    		return docObli;
    	}
    	else
    		return new ArrayList<String>();
	}

}
