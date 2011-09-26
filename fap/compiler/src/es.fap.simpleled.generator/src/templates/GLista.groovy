package templates

import generator.utils.FileUtils;
import generator.utils.StringUtils;
import es.fap.simpleled.led.*;

public class GLista {
	
	def Lista lista;
	
	public static String generate(Lista lista){
		GLista g = new GLista();
		g.lista = lista;
		g.generateFile();
	}
	
	public String generateFile(){
		String contenido = "";
		
		for(ElementoLista el : lista.elementos){
			contenido += generateElemento(el);
		}
		
		FileUtils.overwrite(FileUtils.getRoute('LIST'), lista.name + ".yaml", contenido);
	}
	
	private String generateElemento(ElementoLista el){
		String table = lista.name;
		String key = el.key?:StringUtils.id(el.value)
		String value = el.value?:el.key;
		
		String out = """TableKeyValue(${table}-${key}):
  table: '${table}'
  key: '${key}'
  value: '${value}'

"""
	}
	
}
