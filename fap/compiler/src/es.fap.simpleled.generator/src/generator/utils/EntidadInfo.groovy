package generator.utils

import es.fap.simpleled.led.Entity
import es.fap.simpleled.led.util.*;

class EntidadInfo {

	public CampoUtils campo;
	public Entidad almacen;
	public Entidad entidad;
	
	public EntidadInfo(CampoUtils campo){
		this.campo = campo;
		this.almacen = Entidad.create(campo.campo.entidad);
		this.entidad = Entidad.create(LedCampoUtils.getUltimaEntidad(campo.campo));
		if (almacen.equals(entidad))
			almacen = Entidad.create((Entity)null);
	}
	
	public EntidadInfo(Entidad entidad){
		this.entidad = entidad;
		campo = CampoUtils.create(entidad.entidad); 
		almacen = Entidad.create((Entity) null);
	}
	
	public EntidadInfo(){}
	
}
