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
		if (lista.enumerado)
		   g.generateEnum();
	}
	
	public String generateFile(){
		String contenido = "";
		
		for(ElementoLista el : lista.elementos){
			contenido += generateElemento(el);
		}
		
		FileUtils.overwrite(FileUtils.getRoute('LIST'), lista.name + ".yaml", contenido);
	}
	
	
	// Para generar los ficheros .java con los enumerados
	public String generateEnum(){
		String contenido = "";
		String clase=lista.name.charAt(0).toUpperCase();
		for (int i=1; i<lista.name.length(); i++){
			clase+=lista.name.charAt(i);
		}
		clase+="Enum";
		Boolean existenElementos=false;
		String key, claveEnum;
		contenido+="package enumerado.gen;\n\npublic enum ${clase}{\n\n";
		int cont=1;
		for(ElementoLista el : lista.elementos){
			key = el.key ? el.key.getFirst() : StringUtils.id(el.value);
			for (String rest : el.key.getResto()) {
				key += "."+rest;
			}
			claveEnum = generateClaveEnum(key);
			if (cont != lista.elementos.size){
			   contenido+="\t${claveEnum}(\"${key}\"),\n";
			   cont++;
			} else{
			   contenido+="\t${claveEnum}(\"${key}\");\n";
			   cont=0;
			}
			existenElementos=true;
			
		}
		if (!existenElementos){
			return "";
		}
		contenido+="\n\tprivate String valor;\n\n\tprivate ${clase}(String valor){\n\t\tthis.valor = valor;\n\t}\n\n\t@Override\n\tpublic String toString() {\n\t\treturn this.valor;\n\t}\n}";
		
		FileUtils.overwrite(FileUtils.getRoute('ENUM'), clase + ".java", contenido);
	}
	
	private String generateClaveEnum(String clave){
		String dev="";
		if (Character.isDigit(clave.charAt(0))){
			dev+="_"+clave.charAt(0);
		} else{
			dev+=clave.charAt(0);
		}
		for (int i=1; i<clave.length(); i++){
			if (clave.charAt(i)=='.'){
			   dev+='_';
			}
			else {
			   dev+=clave.charAt(i);
			}
		}
		return dev;
	}
	
	private String generateElemento(ElementoLista el){
		String table = lista.name;
		String key = el.key ? el.key.getFirst() : StringUtils.id(el.value);
		for (String rest : el.key.getResto()) {
			key += "."+rest;
		}
		String value = el.value?:el.key;
		
		String out = """TableKeyValue(${table}-${key}):
  table: '${table}'
  key: '${key}'
  value: '${value}'

"""
	}
	
}
