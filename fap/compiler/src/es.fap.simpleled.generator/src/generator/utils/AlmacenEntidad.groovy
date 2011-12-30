package generator.utils

import es.fap.simpleled.parseTreeConstruction.LedParsetreeConstructor.ThisRootNode;

class AlmacenEntidad {

	public CampoUtils campo;
	public EntidadUtils almacenAnterior;
	public EntidadUtils almacen;
	public EntidadUtils entidad;
	
	public AlmacenEntidad(CampoUtils campo, EntidadUtils almacenAnterior, EntidadUtils almacen, EntidadUtils entidad){
		this.campo = campo;
		this.almacenAnterior = almacenAnterior;
		this.almacen = almacen;
		this.entidad = entidad;
	}

	public AlmacenEntidad(){}
	
}
