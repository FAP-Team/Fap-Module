package templates

import org.eclipse.emf.common.util.BasicEList
import org.eclipse.emf.common.util.EList;

import generator.utils.FileUtils;
import generator.utils.StringUtils;
import generator.utils.LedUtils;
import java.text.DecimalFormat
import java.text.NumberFormat
import es.fap.simpleled.led.impl.ElementoListaImpl;
import es.fap.simpleled.led.util.ModelUtils;
import es.fap.simpleled.led.*;
import wfcomponent.Start;

public class GLista extends GElement{
	
	Lista lista;
	int numFichero;
	
	public GLista(Lista lista, GElement container){
		super(lista, container);
		this.lista = lista;
	}
	
	public void generate(){
		generateFile();
		if (lista.enumerado)
			generateEnum();
	}
	
	public String generateFile(){
		String contenido = "";
		int contador=0;
		numFichero=1;
		boolean pendienteEscribir=false;
		for(ElementoLista el : lista.elementos){
			// Distinguir si el elemento es de tipo 'normal' o 'dependiente' para generarlo de una forma u otra
			if (el.value){
				// Generacion de tipo 'normal'
				contenido += generateElemento(el);
			}
			else {
				// Generación de tipo 'dependiente
				contenido += generateElementoDependiente(el);
				// Nombre generado de la tabla : table-key-key
			}
			contador++;
			if (!contenido.equals("")){
				pendienteEscribir=true;
				if (contador == 100){
					contador = 0;
					FileUtils.overwrite(FileUtils.getRoute('LIST'), lista.name + numFichero + ".yaml", contenido);
					contenido = "";
					numFichero++;
					pendienteEscribir=false;
				}
			}
		}
		if ((pendienteEscribir) && (!contenido.equals(""))){
			if (numFichero != 1)
				FileUtils.overwrite(FileUtils.getRoute('LIST'), lista.name + numFichero + ".yaml", contenido);
			else
				FileUtils.overwrite(FileUtils.getRoute('LIST'), lista.name + ".yaml", contenido);
		}
	}
	
        
    /**
     * Genera el enum a partir de la definición de la lista
     * 
     * En caso de que se esté generando en la aplicación y exista una lista
     * en el módulo con el mismo nombre, se mezclan las dos listas, teniendo
     * más prioridad la definida por el usuario.
     */
    public void generateEnum(){
        if(lista.elementos.size() == 0) return;
		
        String ppackage;
        String route;
        String elementos;
		if (Start.generatingModule){ // Si es lista FAP, lo creo en un directorio distinto y con marcadores comentados para propiciar el añadir despues si la sobreescribimos con una lista de aplicacion
			ppackage = "enumerado.fap.gen";
            route = FileUtils.getRoute('ENUM_FAP');
            elementos = getEnumElementsDefinition(lista.elementos);
		} else{ // Si la lista es de la Aplicacion
            ppackage = "enumerado.gen";
            route = FileUtils.getRoute('ENUM');
            List<ElementoLista> merged = elementosListaMergedWithModule();
            elementos = getEnumElementsDefinition(lista.elementos);
		}
        String className = getEnumClassName();
        String classContent = getEnumClassBody(ppackage, className, elementos);
        FileUtils.overwrite(route, className + ".java", classContent);
	}
	
    private String getEnumClassName(){
        return StringUtils.firstUpper(lista.name) + "Enum";
    }
    
    private String getEnumElementKey(ElementoLista el){
        String key;
        if(el.key == null){
            key = StringUtils.id(el.value);
        }else{
            key = el.key.getFirst();
            for(String resto : el.key.getResto()){
                key += "." + resto;
            }
            
        }
        return escapeKey(key);
    }
    
    private String escapeKey(String clave){
        String result = clave;
        if (Character.isDigit(clave.charAt(0))){
            result = "_" + result;
        }
        return result.replaceAll('\\.', '_');
    }
    
    private String getEnumElementValue(ElementoLista el){
        return el.value?: getEnumElementKey(el);
    }

    private String getEnumElementsDefinition(List<ElementoLista> elementos){
        List<String> enumElementsDef = new ArrayList<String>();
        for(ElementoLista el : lista.elementos){
            String key = getEnumElementKey(el);
            String value = getEnumElementValue(el);
            enumElementsDef.add("""${key}("${value}")""")
        }
        return enumElementsDef.join(",")
    }
    
    private String getEnumClassBody(String ppackage, String className, String elements){
        String body = """
            package ${ppackage};
            
            public enum ${className}{
                ${elements};
            
                private String value;
            
                private ${className}(String value){
                    this.value = value;
                }
                
                public String value(){
                    return value;
                }
            
                @Override
                public String toString(){
                    return this.name() + "[" + this.value() + "]";
                }
            }
            """
        return body;
    }
    
    private List<ElementoLista> elementosListaMergedWithModule(){
        List listas = ModelUtils.getVisibleNodes(LedPackage.Literals.LISTA, lista.name, LedUtils.resource);
        List merged = lista.elementos;
        for(Lista lista : listas){
            for(ElementoLista elemento : lista.elementos){
                if (listContainsElementoLista(elemento, merged) == false){
					elemento.elementosDependientes = new BasicEList<ElementoListaDependiente>();
					ElementoLista el = LedFactory.eINSTANCE.createElementoLista();
					el.key = elemento.key;
					el.value = elemento.value;
					if (elemento.elementosDependientes != null) {
						el.elementosDependientes = new BasicEList();
						for (ElementoListaDependiente elDep: elemento.elementosDependientes) {
							ElementoListaDependiente newElDep = LedFactory.eINSTANCE.createElementoListaDependiente();
							newElDep.key = elDep.key;
							newElDep.value = elDep.value;
							el.elementosDependientes.add(newElDep);
						}
					} else {
						el.elementosDependientes = null;
					}
					
                    merged.add(el);
                }
            }
        }
        return merged
    }
        
    // Comprueba en una Lista de ElementoLista, que no este un determinado elemento ya introducido
    private boolean listContainsElementoLista(ElementoLista el, List<ElementoLista> lista){
        def key1 = el.key ? el.key.getFirst() : StringUtils.id(el.value);
        boolean ret = false;
        
        for(ElementoLista el2 : lista){
            def key2 = el2.key ? el2.key.getFirst() : StringUtils.id(el2.value);
            if (key1 == key2){
                return true;
            }
        }
        return false;
    }

	private String generateElemento(ElementoLista el){
		String table = lista.name;
		String key = getEnumElementKey(el); 
        String value = getEnumElementValue(el);
		
		String out = """TableKeyValue(${table}-${key}):
  table: '${table}'
  key: '${key}'
  value: '${value}'
  noVisible: '${lista.ocultar}'
"""
	}
	
	private String generateElementoDependiente(ElementoLista el){
		String table = lista.name;
		String key = el.key.getFirst();
		String keyDep = "";
		String valueDep = "";
		// Calculamos el nombre de la clave del elemento dependiente
		for (String rest : el.key.getResto()) {
			key += "."+rest;
		}
		String out = "";
		int contador=0;
		// Recorremos todos los elementos que tiene el elemento Dependiente
		for (ElementoListaDependiente elDep : el.elementosDependientes){
			// Calculamos el nombre de la clave de cada uno de los elementos
			keyDep = elDep.key ? elDep.key.getFirst() : StringUtils.id(elDep.value);
			for (String rest : elDep.key.getResto()) {
				keyDep += "."+rest;
			}
			// Calculamos que valor va a tener asociado cada clave
			valueDep = elDep.value?:elDep.key;
			// Empezamos a crear la salida
			// Para la tabla TKV
			out += """TableKeyValue(${table}-${key}-${keyDep}):
  table: '${table}'
  key: '${keyDep}'
  value: '${valueDep}'
  noVisible: '${lista.ocultar}'

"""
			// Para la tabla TKVDependency
			out += """TableKeyValueDependency(${table}-${key}-${keyDep}):
  table: '${table}'
  dependency: '${key}'
  key: '${keyDep}'
  noVisible: '${lista.ocultar}'

"""
			contador+=2;
			if (contador == 100){
				contador = 0;
				NumberFormat formatter = new DecimalFormat("0000");
				String numeroFormateado = formatter.format(numFichero);
				FileUtils.overwrite(FileUtils.getRoute('LIST'), lista.name + numeroFormateado + ".yaml", out);
				out = "";
				numFichero++;
			}
		}
		//String value = el.value?:el.key;
		return out;
	}
	
}
