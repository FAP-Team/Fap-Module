package resolucion;

import services.GestorDocumentalService;
import java.util.List;
import enumerado.fap.gen.EstadoLineaResolucionEnum;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadoTipoMultipleEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.ModalidadResolucionEnum;
import models.LineaResolucionFAP;
import models.ResolucionFAP;
import models.SolicitudGenerica;

public class ResolucionSimple extends ResolucionBase {

	public ResolucionSimple(ResolucionFAP resolucion) {
		super(resolucion);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initResolucion(Long idResolucion) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		resolucion.modalidad = ModalidadResolucionEnum.resolucionSimple.name();
		resolucion.tipoMultiple = EstadoTipoMultipleEnum.total.name();
		resolucion.firmarJefeServicio = true;
		resolucion.firmarDirector = true;
		resolucion.permitirPortafirma = true;
		resolucion.permitirRegistrar = true;
		resolucion.permitirPublicar = true;
		resolucion.estado = EstadoResolucionEnum.borrador.name();
		resolucion.save();
	}
	
	@Override
	public void setLineasDeResolucion(Long idResolucion, List<Long> idsSeleccionados) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		Long idSeleccionado = idsSeleccionados.get(0);
		LineaResolucionFAP lResolucion = new LineaResolucionFAP();
		lResolucion.solicitud = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud where ((solicitud.estado not in('borrador')) and (solicitud.id =?))", idSeleccionado).first();
		if (lResolucion.solicitud.estado.equals(EstadosSolicitudEnum.verificado.name())) {
			lResolucion.estado = EstadoLineaResolucionEnum.concedida.name();
		} else {
			lResolucion.estado = EstadoLineaResolucionEnum.excluida.name();
		}
		lResolucion.save();
		resolucion.lineasResolucion.add(lResolucion);
		resolucion.save();
	}
	
//	@Override
//	public void publicarResolucion(Long idResolucion) {
		// 1. TODO: Debe publicar en el servicio web (no implementado el servicio en la agencia)
		// 2. Debemos establecer la fechaFinDeAceptación a todas las líneas de resolución
		//    en concreto a las concedidas
		// 3. Cambiar el estado del expediente
		// 4. Copiar el documento de resolución a todos los expedientes
//		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
//		GestorDocumentalService gestorDocumental = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
//		List<ExpedienteAed> listaExpedientes = new ArrayList<ExpedienteAed>();
//		int i = 1;
//	if (resolucion.lineasResolucion.size() >= 1){
//		//play.Logger.error("La Resolución "+resolucion.id+" que se está intentando publicar tiene: "+resolucion.lineasResolucion.size()+" líneas de resolución");
//		Messages.error("No se ha podido publicar la resolución. La solicitud ");
//	}
//	else{
//		LineaResolucionFAP linea = resolucion.lineasResolucion.get(0);
//	}	
//		play.Logger.info("Resolución: "+resolucion.id+" tiene "+resolucion.lineasResolucion.size()+" líneas de resolución");
		
//
//		for (LineaResolucionFAP linea: resolucion.lineasResolucion) {
//			play.Logger.info("Línea: "+linea.id+" estado: "+linea.estado);
//
//			Solicitud sol = Solicitud.findById(linea.solicitud.id);
//
//			// 3
//			if (TipoResolucionEnum.provisional.name().equals(resolucion.tipo)) {
//				cambiaEstadoProvisional(linea);
//			} else {
//				cambiaEstadoDefinitiva(linea);
//			}
//
//			if (linea.estado.equalsIgnoreCase("concedida") || linea.estado.equalsIgnoreCase("concedidaTurismo")) {
//				// 2
//				sol.fechaFinDeAceptacion = resolucion.fechaFinAceptacion;
//
//			}
//
//			listaExpedientes.add(linea.solicitud.expedienteAed);
//
//			// 4
//			if ((i%10 == 0) || (i == resolucion.lineasResolucion.size())) {
//				try {
//					gestorDocumental.copiarDocumentoEnExpediente(resolucion.registro.oficial.uri, listaExpedientes);
//					listaExpedientes.clear();
//					play.Logger.info("Copiados los expedientes "+i);
//				} catch (GestorDocumentalServiceException e) {
//					play.Logger.error("No se han podido copiar el documento de resolución a los expedientes: "+i+" -> "+e);
//					Messages.error("No se han podido copiar el documento de resolución a los expedientes");
//				}
//			}
//			i++;
//
//			sol.save();
//		}
//
//		// Si quedan expedientes por copiar:
//		// 4
//		try {
//			if (listaExpedientes.size() != 0) {
//				gestorDocumental.copiarDocumentoEnExpediente(resolucion.registro.oficial.uri, listaExpedientes);
//				listaExpedientes.clear();
//				play.Logger.info("Copiados los expedientes restantes");
//			} else {
//				play.Logger.info("No quedan expedientes a los que copiar la resolución");
//			}
//		} catch (GestorDocumentalServiceException e) {
//			play.Logger.error("No se han podido copiar el documento de resolución a los expedientes: "+" -> "+e);
//			Messages.error("No se han podido copiar el documento de resolución a los expedientes");
//		}
		
		
		
		// TODO Solo se hará esto:
//		faltaHacerEnTesis(resolucion);
//	}
	
}
