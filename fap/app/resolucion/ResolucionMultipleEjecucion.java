package resolucion;

import java.util.ArrayList;
import java.util.List;

import enumerado.fap.gen.EstadoLineaResolucionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import models.LineaResolucionFAP;
import models.ResolucionFAP;
import models.SolicitudGenerica;

public class ResolucionMultipleEjecucion extends ResolucionParcial{

        public ResolucionMultipleEjecucion(ResolucionFAP resolucion) {
                super(resolucion);
        }
        
        public void setLineasDeResolucion(Long idResolucion, List<Long> idsSeleccionados) {
                ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
                for (Long id : idsSeleccionados) {
                        SolicitudGenerica sol = SolicitudGenerica.findById(id);
                        LineaResolucionFAP lResolucion = new LineaResolucionFAP();
                        lResolucion.solicitud = sol;
                        //Todas las solicitudes seleccionadas pasan al estado afectada
                        lResolucion.estado = EstadoLineaResolucionEnum.afectada.name();
                        lResolucion.save();
                        resolucion.lineasResolucion.add(lResolucion);
                        resolucion.save();
                }
        }

}