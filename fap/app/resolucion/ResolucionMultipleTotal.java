package resolucion;

import enumerado.fap.gen.EstadoLineaResolucionEnum;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadoTipoMultipleEnum;
import enumerado.fap.gen.EstadosEvaluacionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.ModalidadResolucionEnum;
import messages.Messages;
import models.Evaluacion;
import models.LineaResolucionFAP;
import models.ResolucionFAP;
import models.SolicitudGenerica;

public class ResolucionMultipleTotal extends ResolucionBase {

	public ResolucionMultipleTotal(ResolucionFAP resolucion) {
		super(resolucion);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void initResolucion(Long idResolucion) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		resolucion.modalidad = ModalidadResolucionEnum.resolucionMultiple.name();
		resolucion.tipoMultiple = EstadoTipoMultipleEnum.total.name();
		resolucion.firmarJefeServicio = true;
		resolucion.firmarDirector = true;
		resolucion.permitirPortafirma = true;
		resolucion.permitirRegistrar = true;
		resolucion.permitirPublicar = true;
		resolucion.estado = EstadoResolucionEnum.borrador.name();
		resolucion.save();
	}

	/**
	 * Establece las líneas de resolución a una resolución, en caso de que no existan.
	 * @param idResolucion
	 */
	@Override
	public void setLineasDeResolucion(Long idResolucion) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		if (resolucion.lineasResolucion.size() == 0) {
			// Por cada una de las solicitudes a resolver, añadimos una línea de resolución
			for (Object solObject: getSolicitudesAResolver(idResolucion)) {
				SolicitudGenerica sol = (SolicitudGenerica)solObject;
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
	
	/**
	 * Comprueba que todas las solicitudes estén en el siguiente estado:
	 * - Si la resolución tiene "baremación" -> todas solicitudes en "baremada"
	 * - Si no                               -> todas solicitudes en "verificada"
	 * @param idResolucion
	 */
	@Override
	public void prepararLineasResolucion (Long idResolucion) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		boolean hayErrores = false;
		if (resolucion.conBaremacion) {
			for (Object solObject: getSolicitudesAResolver(idResolucion)) {
				SolicitudGenerica sol = (SolicitudGenerica) solObject;
				if (!EstadosSolicitudEnum.verificado.name().equals(sol.estado) && (!EstadosSolicitudEnum.excluido.name().equals(sol.estado))) {
					play.Logger.error("Preparando la resolución. La solicitud "+sol.id+" está en estado "+sol.estado);
					hayErrores = true;
				} else if(EstadosSolicitudEnum.verificado.name().equals(sol.estado)) {
					Evaluacion eval = Evaluacion.find("select evaluacion from Evaluacion evaluacion where evaluacion.solicitud.id=?", sol.id).first();
					if (!eval.estado.equals(EstadosEvaluacionEnum.evaluada.name())) {
						play.Logger.error("Preparando la resolución. La solicitud "+sol.id+" no ha sido evaluada "+sol.estado+" ["+eval.id+"]");
						hayErrores = true;
					}
				}
			}
		} else {
			for (Object solObject: getSolicitudesAResolver(idResolucion)) {
				SolicitudGenerica sol = (SolicitudGenerica) solObject;
				if (!EstadosSolicitudEnum.verificado.name().equals(sol.estado) && (!EstadosSolicitudEnum.excluido.name().equals(sol.estado))) {
					play.Logger.error("Preparando la resolución. La solicitud "+sol.id+" está en estado "+sol.estado);
					hayErrores = true;
				}
			}
		}
		if (hayErrores) {
			Messages.error("No se puede prepara la resolución. Existen solicitudes en estados no válidos");
		}
	}
}
