package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import aed.TiposDocumentosClient;

import models.ObligatoriedadDocumentos;
import models.SolicitudGenerica;
import models.Tramite;

import play.Play;
import play.utils.Java;
import utils.ObligatoriedadDocumentosFap;

public class VerificacionFapController {

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
