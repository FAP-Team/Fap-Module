package resolucion;

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
		SolicitudGenerica sol = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud where ((solicitud.estado not in('borrador')) and (solicitud.id =?))", idSeleccionado).first();
		LineaResolucionFAP lResolucion = new LineaResolucionFAP();
		lResolucion.solicitud = sol;
		if (sol.estado.equals(EstadosSolicitudEnum.verificado.name())) {
			lResolucion.estado = EstadoLineaResolucionEnum.concedida.name();
		} else {
			lResolucion.estado = EstadoLineaResolucionEnum.excluida.name();
		}
		lResolucion.save();
		resolucion.lineasResolucion.add(lResolucion);
		resolucion.save();
	}
	
}
