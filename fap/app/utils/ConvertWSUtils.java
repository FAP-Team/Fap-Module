package utils;

import java.util.ArrayList;
import java.util.List;

import es.gobcan.eadmon.procedimientos.ws.dominio.CodigoExclusion;
import es.gobcan.eadmon.procedimientos.ws.dominio.ListaCodigosExclusion;

import models.TipoCodigoExclusion;

public class ConvertWSUtils {

	public static List<TipoCodigoExclusion> codigosExclusionWS2List (ListaCodigosExclusion listaWS) {
		List<TipoCodigoExclusion> lista = new ArrayList<TipoCodigoExclusion>();
		if(listaWS != null){
			for(CodigoExclusion ce : listaWS.getCodigosExclusion()){
				TipoCodigoExclusion nuevo = new TipoCodigoExclusion();
				nuevo.codigo = ce.getCodigo();
				nuevo.descripcionCorta = ce.getDescripcionCorta();
				nuevo.descripcion = ce.getDescripcion();
				lista.add(nuevo);
			}
		}
		return lista;
	}
}
