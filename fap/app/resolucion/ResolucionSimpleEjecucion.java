package resolucion;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityTransaction;

import org.joda.time.DateTime;

import play.db.jpa.JPA;
import properties.FapProperties;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;
import enumerado.fap.gen.EstadoLineaResolucionEnum;
import enumerado.fap.gen.EstadoNotificacionEnum;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;

import reports.Report;
import services.FirmaService;
import services.GestorDocumentalService;
import services.NotificacionService;
import services.RegistroService;
import utils.NotificacionUtils;
import messages.Messages;
import models.Documento;
import models.DocumentoNotificacion;
import models.ExpedienteAed;
import models.LineaResolucionFAP;
import models.Notificacion;
import models.Registro;
import models.ResolucionFAP;
import models.SolicitudGenerica;

public class ResolucionSimpleEjecucion extends ResolucionSimple {

	public ResolucionSimpleEjecucion(ResolucionFAP resolucion) {
		super(resolucion);
	}

	@Override
	public void setLineasDeResolucion(Long idResolucion, List<Long> idsSeleccionados) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		Long idSeleccionado = idsSeleccionados.get(0);
		LineaResolucionFAP lResolucion = new LineaResolucionFAP();
		lResolucion.solicitud = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud where ((solicitud.estado not in('borrador')) and (solicitud.id =?))", idSeleccionado).first();
		lResolucion.estado = EstadoLineaResolucionEnum.afectada.name();
		lResolucion.save();
		resolucion.lineasResolucion.add(lResolucion);
		resolucion.save();
	}
}
