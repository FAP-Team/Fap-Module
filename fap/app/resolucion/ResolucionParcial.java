package resolucion;

import enumerado.fap.gen.EstadoLineaResolucionEnum;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadoTipoMultipleEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.ModalidadResolucionEnum;
import models.LineaResolucion;
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
		resolucion.save();
	}

}
