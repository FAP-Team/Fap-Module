package es.fap.simpleled.led.util;

import java.util.ArrayList;
import java.util.List;

public class DocElemento {
	public String nombre;
	public String keyword; // Cuando se parsean los .textile este atributo no se setea. 
	public String descripcion;
	public List<DocParametro> parametros;

	public DocElemento () {
		parametros = new ArrayList<DocParametro>();
	}
}
