package resolucion;

import org.joda.time.DateTime;

import enumerado.fap.gen.EstadoLineaResolucionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import models.LineaResolucion;
import models.Resolucion;
import models.SolicitudGenerica;

public class ResolucionBase {

	public Resolucion resolucion;
	
	public ResolucionBase (Resolucion resolucion) {
		this.resolucion = resolucion;
	}
	
	/**
	 * Devuelve las solicitudes "posibles" a resolver (lista desde donde se seleccionará)
	 * @return
	 */
	public static java.util.List<SolicitudGenerica> getSolicitudesAResolver () {
		//return SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud where solicitud.estado=?", "iniciada").fetch();
		return SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud where solicitud.estado in('verificado','excluido','desistido')").fetch();
	}
	
	/**
	 * Establece las líneas de resolución a una resolución, en caso de que no existan.
	 * @param idResolucion
	 */
	public static void setLineasDeResolucion (Long idResolucion) {
		Resolucion resolucion = Resolucion.findById(idResolucion);
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
