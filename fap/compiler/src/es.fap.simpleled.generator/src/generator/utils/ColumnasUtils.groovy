package generator.utils

import java.text.CollationElementIterator;

import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.led.*
import es.fap.simpleled.led.impl.*;
import generator.utils.HashStack.HashStackName;

public class ColumnasUtils {

	CampoUtils campo;
	
	public static List<Columna> columnas(Campo campo){
		ColumnasUtils columnas = new ColumnasUtils();
		columnas.campo = CampoUtils.create(CampoUtils.create(campo).getUltimaEntidad());
		return columnas.recorrerEntidad();
	}
	
	public List<Columna> recorrerEntidad(){
		List<Columna> out = new ArrayList<Columna>();
		for (Attribute attr: LedEntidadUtils.getAllDirectAttributes(campo.getUltimaEntidad())){
			out.addAll(generateAttr(attr));
		}
		out.last()?.expandir = true;
		return out;
	}
				
	private List<Columna> generateAttr(Attribute attr){
		List<Columna> out = new ArrayList<Columna>();
		if (LedEntidadUtils.esSimple(attr)){
			out.add(columna(attr));
		}
		else{
			out.addAll(generateAttrCompound(attr));
		}
		return out;
	}
	
	private List<Columna> generateAttrCompound(Attribute attr) {
		List<Columna> out = new ArrayList<Columna>();
		CompoundType compound = attr.type.compound;
		if (compound.entidad != null){
			out.addAll(generateEntidadReferencia(attr));
		}
		else{
			out.add(columna(attr));
		}		
		return out;
	}
	
	private List<Columna> generateEntidadReferencia(Attribute attr) {
		List<Columna> out = new ArrayList<Columna>()
		String referencia = attr.type.compound.tipoReferencia?.type;
		if (referencia == null || referencia.equals("OneToOne") || referencia.equals("ManyToOne")){ 
			out.addAll(columnas(campo.addAttribute(attr)));
		}
		return out;
	}
	
	private Columna columna(Attribute attr){
		Columna col = new ColumnaImpl();
		col.campo = campo.addAttribute(attr);
		col.titulo = attr.name;
		col.ancho = col.titulo.length() * 15;
		return col;
	}
	
}