package resolucion;

import enumerado.fap.gen.EstadoLineaResolucionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import models.LineaResolucion;
import models.ResolucionFAP;
import models.SolicitudGenerica;

public class ResolucionMultipleTotal extends ResolucionBase {

	public ResolucionMultipleTotal(ResolucionFAP resolucion) {
		super(resolucion);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Establece las líneas de resolución a una resolución, en caso de que no existan.
	 * @param idResolucion
	 */
	public static void setLineasDeResolucion (Long idResolucion) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		if (resolucion.lineasResolucion.size() == 0) {
			// Por cada una de las solicitudes a resolver, añadimos una línea de resolución
			for (SolicitudGenerica sol: getSolicitudesAResolver()) {
				LineaResolucion lResolucion = new LineaResolucion();
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
			}
		}
	}
}
