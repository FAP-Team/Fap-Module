package resolucion;

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
	public void setLineasDeResolucion(Long idResolucion) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		if (resolucion.lineasResolucion.size() == 0) {
			// Por cada una de las solicitudes a resolver, añadimos una línea de resolución
			for (SolicitudGenerica sol: getSolicitudesAResolver(idResolucion)) {
				LineaResolucionFAP lResolucion = new LineaResolucionFAP();
				lResolucion.solicitud = sol;
				if (sol.estado.equals(EstadosSolicitudEnum.verificado)) {
					lResolucion.estado = EstadoLineaResolucionEnum.concedida.name();
				} else if (sol.estado.equals(EstadosSolicitudEnum.excluido.name())) {
					lResolucion.estado = EstadoLineaResolucionEnum.excluida.name();
				} else {
					lResolucion.estado = EstadoLineaResolucionEnum.excluida.name();
				}
				lResolucion.save();
				
				resolucion.lineasResolucion.add(lResolucion);
				resolucion.save();
				break;
			}
		}
	}
	
}
