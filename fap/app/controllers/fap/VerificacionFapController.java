package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import aed.TiposDocumentosClient;

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

public class VerificacionFapController {
	
	
	/**
	 * Método que devuelve la lista de documentos a verificar en una determinada verificacion
	 * @param idVerificacion Verificacion actual sobre la que vamos a ejercer todas las operaciones
	 * @return Lista con los Documentos que se quieran verificar
	 */
	public static List<Documento> getNuevosDocumentosVerificar(Long idVerificacion){
		List<Documento> nuevosDocumentos = new ArrayList<Documento>();
		SolicitudGenerica solicitud = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud join solicitud.verificacion verificacion where verificacion.id=?", idVerificacion).first();
		// Todos los documentos de solicitud.documentacion.documentos que tenga el verificado a false + solicitud.registro.oficial si tramite es solicitud y verificado = false
		for (Documento doc: solicitud.documentacion.documentos){
			if ((doc.verificado == null) || (!doc.verificado))
				nuevosDocumentos.add(doc);
		}
		if (((solicitud.verificacion.uriTramite != null) && (solicitud.verificacion.uriTramite.equals(FapProperties.get("fap.aed.procedimientos.tramite.uri")))) && ((solicitud.registro.oficial.verificado == null) || (!solicitud.registro.oficial.verificado))){
			nuevosDocumentos.add(solicitud.registro.oficial);
		}
		return nuevosDocumentos;
	}
	
	/**
	 * Método que devuelve si existen nuevos documentos a verificar, en la verificación actual que no han sido aún incluidos
	 * @return True si existen nuevos Documentos a verificar
	 */
	public static boolean isNuevosDocumentos(Long idVerificacion){
		SolicitudGenerica solicitud = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud join solicitud.verificacion verificacion where verificacion.id=?", idVerificacion).first();
		Set documentosVerificaciones = new HashSet();
		for (Verificacion verificacion: solicitud.verificaciones){
			for (VerificacionDocumento vDoc: verificacion.documentos){
				documentosVerificaciones.add(vDoc.uriDocumento);
			}
		}
		for (VerificacionDocumento vDoc: solicitud.verificacion.documentos){
			documentosVerificaciones.add(vDoc.uriDocumento);
		}
		for (Documento doc: solicitud.documentacion.documentos){
			if (!documentosVerificaciones.contains(doc.uri)){
				return true;
			}
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
	public static List<String> getTipoDocumentosCondicionadosAutomaticos(String tramite) throws Throwable  {
		return VerificacionFapController.invoke("getDocumentosCondicionadosAutomaticos", tramite);
	}
	
	private static List<String> invoke(String m, Object... args) throws Throwable {
		Class metodoALlamar = null;
        List<Class> classes = Play.classloader.getAssignableClasses(VerificacionFapController.class);
        if(classes.size() != 0) {
        	metodoALlamar = classes.get(0);
        } else {
        	play.Logger.info("No hay ninguna llamada para calcular los documentos condicionados automaticos");
        	play.Logger.info("Se incluirán todos los documentos condicionados automáticos por defecto");
        	// Devolver todos los CONDICIONADOS AUTOMATICOS
        	ObligatoriedadDocumentosFap docObli = null;
        	try{
        		long idTramite = Tramite.find("select id from Tramite where nombre=?", args[0].toString()).first();
        		docObli = (ObligatoriedadDocumentosFap)ObligatoriedadDocumentosFap.find("select docObli from ObligatoriedadDocumentosFap docObli join docObli.tramite tramite where tramite.id=?", idTramite).first();
        	} catch (Exception e){
        		play.Logger.warn("Fallo al recuperar la lista con los tipos de documentos condicionados automaticos: "+e);
        		return new ArrayList<String>();
        	}
        	if ((docObli != null) && (docObli.automaticas != null)){
        		return docObli.automaticas;
        	}
        	else
        		return new ArrayList<String>();
        }
        try {
        	return (List<String>)Java.invokeStaticOrParent(metodoALlamar, m, args);
        } catch(InvocationTargetException e) {
        	throw e.getTargetException();
        }
	}
}
