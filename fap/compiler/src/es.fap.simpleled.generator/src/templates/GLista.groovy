package templates

import generator.utils.FileUtils;
import generator.utils.StringUtils;
import generator.utils.LedUtils;
import es.fap.simpleled.led.util.ModelUtils;
import es.fap.simpleled.led.*;
import wfcomponent.Start;

public class GLista {
	
	def Lista lista;
	
	public static String generate(Lista lista){
		GLista g = new GLista();
		g.lista = lista;
		g.generateFile();
		if (lista.enumerado){
		   if (Start.generatingModule)
		      g.generateEnum(true);
		   else
		      g.generateEnum(false);
		}
	}
	
	public String generateFile(){
		String contenido = "";
		
		for(ElementoLista el : lista.elementos){
			contenido += generateElemento(el);
		}
		
		FileUtils.overwrite(FileUtils.getRoute('LIST'), lista.name + ".yaml", contenido);
	}
	
	// Comprueba en una Lista de ElementoLista, que no este un determinado elemento ya introducido
	private Boolean findElementoLista (ElementoLista el, List Lista){
		def key1 = el.key ? el.key.getFirst() : StringUtils.id(el.value);
		Boolean ret=false;
		Lista.each{
			def key2 = it.key ? it.key.getFirst() : StringUtils.id(it.value);
			if (key1 == key2){
				ret = true;
			}
		}
		return ret;
	}
	
	
	// Para generar los ficheros .java con los enumerados
	// tipo = true: si esta generando las listas de FAP, false: generando la listas de la aplicacion
	public String generateEnum(boolean tipo){
		String contenido = "";
		String clase=lista.name.charAt(0).toUpperCase();
		for (int i=1; i<lista.name.length(); i++){
			clase+=lista.name.charAt(i);
		}
		clase+="Enum";
		Boolean existenElementos=false;
		String key, claveEnum;
		if (tipo){ // Si es lista FAP, lo creo en un directorio distinto y con marcadores comentados para propiciar el añadir despues si la sobreescribimos con una lista de aplicacion
			contenido+="package enumerado.fap.gen;\n\npublic enum ${clase}{\n\n";
			int cont=1;
			for(ElementoLista el : lista.elementos){
				key = el.key ? el.key.getFirst() : StringUtils.id(el.value);
				for (String rest : el.key.getResto()) {
					key += "."+rest;
				}
				claveEnum = generateClaveEnum(key);
				if (cont != lista.elementos.size){
					contenido+="\t${claveEnum}(\"${el.value}\"),\n";
					cont++;
				} else{
					contenido+="\t${claveEnum}(\"${el.value}\");\n";
					cont=0;
				}
				existenElementos=true;
			
			}
			if (!existenElementos){
				return "";
			}
			contenido+="\n\tprivate String valor;\n\n\tprivate ${clase}(String valor){\n\t\tthis.valor = valor;\n\t}\n\n\t@Override\n\tpublic String toString() {\n\t\treturn this.valor;\n\t}\n}";
		
			FileUtils.overwrite(FileUtils.getRoute('ENUM_FAP'), clase + ".java", contenido);
		} else{ // Si la lista es de la Aplicacion
				List listas = ModelUtils.getVisibleNode(LedPackage.Literals.LISTA, lista.name, LedUtils.resource);
				List listaAll = [];
				List insertar = lista.elementos;
				if (listas.size > 1){
					listas.each{
						for(ElementoLista el : it.elementos){
							listaAll.add(el);
						}
					}
					for (ElementoLista el: listaAll){
						if (findElementoLista(el, insertar) == false){
							insertar.add(el);
						}
					}
				}
				contenido+="package enumerado.gen;\n\npublic enum ${clase}{\n\n";
				int cont=1;
				for(ElementoLista el : insertar){
					key = el.key ? el.key.getFirst() : StringUtils.id(el.value);
					for (String rest : el.key.getResto()) {
						key += "."+rest;
					}
					claveEnum = generateClaveEnum(key);
					if (cont != insertar.size){
						contenido+="\t${claveEnum}(\"${el.value}\"),\n";
						cont++;
					} else{
						contenido+="\t${claveEnum}(\"${el.value}\");\n";
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
