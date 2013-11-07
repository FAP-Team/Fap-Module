package controllers;

import javax.persistence.EntityTransaction;

import messages.Messages;
import models.ResolucionFAP;
import play.db.jpa.JPA;
import play.mvc.Util;
import resolucion.ResolucionBase;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.PaginaPublicarResolucionControllerGen;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadoResolucionPublicacionEnum;

public class PaginaPublicarResolucionController extends PaginaPublicarResolucionControllerGen {

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void cambioEstadoFecha(Long idResolucionFAP, ResolucionFAP resolucionFAP, String btnCambioEstadoFecha) {
		checkAuthenticity();
		if (!permisoCambioEstadoFecha("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		ResolucionFAP dbResolucionFAP = PaginaPublicarResolucionController.getResolucionFAP(idResolucionFAP);
		PaginaPublicarResolucionController.cambioEstadoFechaBindReferences(resolucionFAP);

		if (!Messages.hasErrors()) {
			PaginaPublicarResolucionController.cambioEstadoFechaValidateCopy("editar", dbResolucionFAP, resolucionFAP);
		}

		if (!Messages.hasErrors()) {
			PaginaPublicarResolucionController.cambioEstadoFechaValidateRules(dbResolucionFAP, resolucionFAP);
		}
		
		ResolucionBase resolBase = null;
		if (!Messages.hasErrors()) {
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		}
		if (!Messages.hasErrors()){
			resolBase.publicarCambiarEstadoYDatos(idResolucionFAP);
		}
		if (!Messages.hasErrors()) {
			//Existen dos pasos de cuando se copiaba el oficial de resolucion en expediente
			//dbResolucionFAP.estadoPublicacion=EstadoResolucionPublicacionEnum.estadoFecha.name();
			dbResolucionFAP.estadoPublicacion = EstadoResolucionPublicacionEnum.publicada.name();
			
			if (!dbResolucionFAP.conBaremacion) {
				EntityTransaction tx = JPA.em().getTransaction();
				tx.commit();
				tx.begin();
				if (EstadoResolucionEnum.notificada.name().equals(dbResolucionFAP.estado))
					resolBase.avanzarFase_Registrada_PublicadaYNotificada(dbResolucionFAP);
				else
					resolBase.avanzarFase_Registrada_Publicada(dbResolucionFAP);
				tx.commit();
				tx.begin();
		} 
			dbResolucionFAP.save();
			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaPublicarResolucionController.cambioEstadoFechaRender(idResolucionFAP);
	}
	
	
//	@Util
//	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
//	public static void copiaExpediente(Long idResolucionFAP, String btnCopiaExpediente) {
//		checkAuthenticity();
//		if (!permisoCopiaExpediente("editar")) {
//			Messages.error("No tiene permisos suficientes para realizar la acción");
//		}
//
//		if (!Messages.hasErrors()) {
//			PaginaPublicarResolucionController.copiaExpedienteValidateRules();
//		}
//		ResolucionBase resolBase = null;
//		if (!Messages.hasErrors()) {
//			try {
//				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
//			} catch (Throwable e) {
//				new Exception ("No se ha podido obtener el objeto resolución", e);
//			}
//		}
//		if (!Messages.hasErrors()) {
//			resolBase.publicarCopiarEnExpedientes(idResolucionFAP);
//			resolBase.resolucion.estadoPublicacion = EstadoResolucionPublicacionEnum.publicada.name();
//			resolBase.resolucion.save();
//		}
//		if (!Messages.hasErrors()) {
//
//			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada con éxito");
//		} else
//			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
//		PaginaPublicarResolucionController.copiaExpedienteRender(idResolucionFAP);
//	}
	
//	@Util
//	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
//	public static void generarBaremacionResolucion(Long idResolucionFAP, String btnGenerarResolucion) {
//		EntityTransaction tx = JPA.em().getTransaction();
//		tx.commit();
//		checkAuthenticity();
//		if (!permisoGenerarBaremacionResolucion("editar")) {
//			Messages.error("No tiene permisos suficientes para realizar la acción");
//		}
//
//		if (!Messages.hasErrors()) {
//			PaginaPublicarResolucionController.generarBaremacionResolucionValidateRules();
//		}
//		
//		ResolucionBase resolBase = null;
//		if (!Messages.hasErrors()) {
//			try {
//				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
//			} catch (Throwable e) {
//				new Exception ("No se ha podido obtener el objeto resolución", e);
//			}
//		}
//		if (!Messages.hasErrors()) {
//			resolBase.publicarGenerarDocumentoBaremacionEnResolucion(idResolucionFAP);
//		}
//
//		
//		if (!Messages.hasErrors()) {
//
//			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada con éxito");
//		} else
//			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
//		PaginaPublicarResolucionController.generarBaremacionResolucionRender(idResolucionFAP);
//	}
//	
//	@Util
//	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
//	public static void firmarBaremacion(Long idResolucionFAP, String btnFirmarBaremacion) {
//		checkAuthenticity();
//		if (!permisoFirmarBaremacion("editar")) {
//			Messages.error("No tiene permisos suficientes para realizar la acción");
//		}
//
//		if (!Messages.hasErrors()) {
//			PaginaPublicarResolucionController.firmarBaremacionValidateRules();
//		}
//		
//		ResolucionBase resolBase = null;
//		try {
//			resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
//		} catch (Throwable e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		if(resolBase.resolucion.conBaremacion){
//			resolBase.firmarDocumentosBaremacionEnResolucion(resolBase);
//		}
//		
//		if (!Messages.hasErrors()) {
//
//			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada con éxito");
//		} else
//			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
//		PaginaPublicarResolucionController.firmarBaremacionRender(idResolucionFAP);
//	}

	//TODO adaptar los métodos
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void generarDocumentoBaremacionResolucion(Long idResolucionFAP, String btnGenerarDocumento) {
		checkAuthenticity();
		if (!permisoGenerarDocumentoBaremacionResolucion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			PaginaPublicarResolucionController.generarDocumentoBaremacionResolucionValidateRules();
		}
		
		ResolucionBase resolBase = null;
		if (!Messages.hasErrors()) {
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		}
		
		if ((!Messages.hasErrors()) && (resolBase.isGenerarDocumentoBaremacionIndividual())) {
			resolBase.generarDocumentosBaremacionIndividualResolucion(idResolucionFAP);
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaPublicarResolucionController.generarDocumentoBaremacionResolucionRender(idResolucionFAP);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void generarClasificarDocumentos(Long idResolucionFAP, String btnClasificarDocumentos) {
		checkAuthenticity();
		if (!permisoGenerarClasificarDocumentos("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			PaginaPublicarResolucionController.generarClasificarDocumentosValidateRules();
		}
		ResolucionBase resolBase = null;
		if (!Messages.hasErrors()) {
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		}
		
		if (!Messages.hasErrors()) {
			resolBase.clasificarDocumentosBaremacionIndividual(idResolucionFAP);
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaPublicarResolucionController.generarClasificarDocumentosRender(idResolucionFAP);
	}
	
	//Informes Oficiales
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void generarInformeConComentarios(Long idResolucionFAP, String btnGenerarInformeConComentarios) {
		checkAuthenticity();
		if (!permisoGenerarInformeConComentarios("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			PaginaPublicarResolucionController.generarInformeConComentariosValidateRules();
		}

		ResolucionBase resolBase = null;
		if (!Messages.hasErrors()) {
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		}
		
		if ((!Messages.hasErrors()) && (resolBase.isGenerarDocumentoBaremacionIndividual())) {
			resolBase.generarDocumentoOficialBaremacionConComentarios(idResolucionFAP);
		}

		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaPublicarResolucionController.generarInformeConComentariosRender(idResolucionFAP);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void clasificarInformeConComentarios(Long idResolucionFAP, String btnClasificarInformeConComentarios) {
		checkAuthenticity();
		if (!permisoClasificarInformeConComentarios("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			PaginaPublicarResolucionController.clasificarInformeConComentariosValidateRules();
		}
		
		ResolucionBase resolBase = null;
		if (!Messages.hasErrors()) {
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		}
		
		if ((!Messages.hasErrors()) && (resolBase.isGenerarDocumentoBaremacionIndividual())) {
			resolBase.clasificarDocumentoOficialBaremacionConComentarios(idResolucionFAP);
		}
		
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaPublicarResolucionController.clasificarInformeConComentariosRender(idResolucionFAP);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void generarInformeSinComentarios(Long idResolucionFAP, String btnGenerarInformeSinComentarios) {
		checkAuthenticity();
		if (!permisoGenerarInformeSinComentarios("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			PaginaPublicarResolucionController.generarInformeSinComentariosValidateRules();
		}
		
		ResolucionBase resolBase = null;
		if (!Messages.hasErrors()) {
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		}
		
		if ((!Messages.hasErrors()) && (resolBase.isGenerarDocumentoBaremacionIndividual())) {
			resolBase.generarDocumentoOficialBaremacionSinComentarios(idResolucionFAP);
		}
		
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaPublicarResolucionController.generarInformeSinComentariosRender(idResolucionFAP);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void clasificarInformeSinComentarios(Long idResolucionFAP, String btnClasificarInformeSinComentarios) {
		checkAuthenticity();
		if (!permisoClasificarInformeSinComentarios("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			PaginaPublicarResolucionController.clasificarInformeSinComentariosValidateRules();
		}
		
		ResolucionBase resolBase = null;
		if (!Messages.hasErrors()) {
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		}
		
		if ((!Messages.hasErrors()) && (resolBase.isGenerarDocumentoBaremacionIndividual())) {
			resolBase.clasificarDocumentoOficialBaremacionSinComentarios(idResolucionFAP);
		}
		
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaPublicarResolucionController.clasificarInformeSinComentariosRender(idResolucionFAP);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void firmarBaremacion(Long idResolucionFAP, String btnFirmarBaremacion) {
		checkAuthenticity();
		if (!permisoFirmarBaremacion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		if (!Messages.hasErrors()) {
			PaginaPublicarResolucionController.firmarBaremacionValidateRules();
		}
		
		ResolucionBase resolBase = null;
		if (!Messages.hasErrors()) {
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		}
		
		if (!Messages.hasErrors()) {
			resolBase.firmarDocumentosBaremacionEnResolucion(resolBase);
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaPublicarResolucionController.firmarBaremacionRender(idResolucionFAP);
	}
	//
	
}
