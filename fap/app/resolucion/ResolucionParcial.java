package resolucion;

import java.util.ArrayList;
import java.util.List;

import enumerado.fap.gen.EstadoLineaResolucionEnum;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadoResolucionNotificacionEnum;
import enumerado.fap.gen.EstadoTipoMultipleEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.ModalidadResolucionEnum;
import models.LineaResolucionFAP;
import models.ResolucionFAP;
import models.SolicitudGenerica;

public class ResolucionParcial extends ResolucionMultipleTotal {

	public ResolucionParcial(ResolucionFAP resolucion) {
		super(resolucion);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void initResolucion(Long idResolucion) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		resolucion.modalidad = ModalidadResolucionEnum.resolucionMultiple.name();
		resolucion.tipoMultiple = EstadoTipoMultipleEnum.parcial.name();
		resolucion.firmarJefeServicio = true;
		resolucion.firmarDirector = true;
		resolucion.permitirPortafirma = true;
		resolucion.permitirRegistrar = true;
		resolucion.permitirPublicar = true;
		resolucion.estado = EstadoResolucionEnum.borrador.name();
		resolucion.estadoNotificacion = EstadoResolucionNotificacionEnum.noNotificada.name();
		resolucion.save();
	}

	public void setLineasDeResolucion(Long idResolucion, List<Long> idsSeleccionados) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		for (Long id : idsSeleccionados) {
			SolicitudGenerica sol = SolicitudGenerica.findById(id);
			LineaResolucionFAP lResolucion = new LineaResolucionFAP();
			lResolucion.solicitud = sol;
			if (EstadosSolicitudEnum.verificado.name().equals(sol.estado)) {
				lResolucion.estado = EstadoLineaResolucionEnum.concedida.name();
			} else if (EstadosSolicitudEnum.excluido.name().equals(sol.estado)) {
				lResolucion.estado = EstadoLineaResolucionEnum.excluida.name();
			} else {
				lResolucion.estado = EstadoLineaResolucionEnum.excluida.name();
			}
			lResolucion.save();
			
			resolucion.lineasResolucion.add(lResolucion);
			resolucion.save();
		}
		
	}
}
