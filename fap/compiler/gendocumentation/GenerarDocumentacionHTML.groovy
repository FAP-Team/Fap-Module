import java.awt.event.ItemEvent;
import java.util.List;

import javax.swing.text.html.HTML;

import jj.play.org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import jj.play.org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;
import jj.play.org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;

class GenerarDocumentacionHTML {

	private static String plantillaJST = ""
	private static File fs, plantilla, fin
	private static String matcher = "^\\s*//"
	private static String titulo='', ruta='';
	private static List docs_text = [], code_text=[]
	private static List sections = []
	
	public static void main(String[] args) {
		plantillaJST = args[2]+"/plantillas/plantillaHTML.jst"
		ruta=args[3];
		generarDocumentacionHTML(args[0], args[1])
	}
	
	// Metodo publico que se puede acceder para generar la documentacion en formato HTML
	// de nuestros ficheros FAP
	public static void generarDocumentacionHTML(String fuente, String destino){
		titulo="Fap Documentation"
		cargarFichero(fuente)
		generarContenido()
		generarHTML(destino)
	}
	
	// Generamos 2 List con el contenido que tendra nuestra pagina HTML de documentacion
	//  - docs_text: Con el contenido de los Comentarios, exactamente como se deben mostrar por lineas
	//  - code_text: Con el contenido del código, exactamente como se debe mostrar por lineas
	// Creamos el Map <Clave, <"docs_text" | "code_text" , Texto>> que le enviaremos a la plantilla .jst
	private static void generarContenido (){
		parsearFichero()
		Integer cont=0
		// Preparamos la Lista de Maps, que le pasaremos a la plantilla con los Comentarios y el Codigo
		docs_text.each{
			def sectionsCont = [:]
			sectionsCont["docs_text"]=it
			sectionsCont["code_text"]= code_text[cont++]
			sections.add(sectionsCont)
		}
	}
	
	// Parseamos el fichero .fap, y lo dividimos en 2 List:
	// - docs_text: Contiene cada una de las lineas de comentarios del fichero .fap, en la posición correcta para mostrarse (quitandole el // de comentario)
	// - code_text: Contiene cada una de las lineas de codigo del fichero .fap, en la posición correcta para mostrarse
	private static void parsearFichero(){
	   // 1= Comentario; 2= Codigo; 3= Linea en Blanco
	   int anterior = 3; // Anterior es utilizado para saber que es lo que tenia la linea anterior y poder crear adecuadamente las secciones
	   int contComentario=-1, contCodigo=-1
	   Boolean comentario=false
	   String auxComentario=""
	   fs.eachLine {
		   if ((it =~ /^\s*\/\*/) || comentario){ // Comentario tipo /* --- */
			   if (comentario){ // Si ya hemos empezado a leer el comentario (tiene varias lineas)
				   if (it =~ /\*\//){ // Si es la última linea del comentario
						   docs_text[contComentario]+=" "+it.replaceAll(/\*\/.*/, '')
						   comentario = false
				   } else{
						   docs_text[contComentario]+=" "+it
				   }
			   } else{ // Si estamos empezando a leer el comentario
				   if (it =~ /\*\//){ // Comentario /* ... */ en una sola linea
					   auxComentario = it.replaceAll(/\*\/.*/, '') // Quitamos el final
					switch (anterior){
					   case 1:
							   docs_text[contComentario]+=" "+auxComentario.replaceAll(/\s*\/\*/, '') // Quitamos el princpio
							   break
					   case 2..3:
							   docs_text[++contComentario]=auxComentario.replaceAll(/\s*\/\*/, '')
							   break
					   default:
							   break
					}
					   comentario = false
				   } else{ // Hay varias lineas de comentario
						switch (anterior){
					   case 1:
							   docs_text[contComentario]+=" "+it.replaceAll(/\*\/.*/, '')  // Quitamos el princpio
							   break
					   case 2..3:
							   docs_text[++contComentario]=it.replaceAll(/\*\/.*/, '')
							   break
					   default:
							   break
					 }
						comentario = true
				   }
				   anterior=1
			   }
		   } else if (it =~ matcher){ // Es un comentario, lo insertamos limpiando los caracteres de comentarios
			   switch (anterior){
				   case 1:
						   docs_text[contComentario]+=" "+it.replaceAll(/\s*\/\/\s*/, '')
						   break
				   case 2..3:
						   docs_text[++contComentario]=it.replaceAll(/\s*\/\/\s*/, '')
						   break
				   default:
						   break
			   }
			   anterior = 1
		   } else if (it =~ /^\s*$/){ // Si es una linea en blanco
				  switch (anterior){
				   case 1:
					   code_text[++contCodigo]=""
					   anterior=3
					   break
					case 2:
					   code_text[contCodigo]+="<br/>"
					   anterior=2
					   break
					default:
					   anterior=3
					   break
			   }
		   } else{// Si es una linea de codigo
				  switch (anterior){
					case 1:
					   code_text[++contCodigo]=it
					   break
					case 2:
					   code_text[contCodigo]+="<br/>"+it
					   break
					case 3:
					   code_text[++contCodigo]=it
					   docs_text[++contComentario]=""
					   break
					default:
					   break
				}
			   anterior=2
		   }
	   }
	   // Lo convertimos de Textile a HTML
	   for (i in 0..(docs_text.size-1)){
			   if (docs_text[i] != ''){
				   docs_text[i] = textile2HTML(docs_text[i])
			   }
		   }
	}

	// Función que a partir de un testo en formato .textile lo convierte a formato HTML con las caracteristicas
	// propias que tenía en textile
	private static String textile2HTML (String textile){
		String html = new jj.play.org.eclipse.mylyn.wikitext.core.parser.MarkupParser(new jj.play.org.eclipse.mylyn.wikitext.textile.core.TextileLanguage()).parseToHtml(textile)
		html = html.substring(html.indexOf("<body>") + 6, html.lastIndexOf("</body>"))
		return html
	}
	
	// Carga de fichero
	private static void cargarFichero (String fuente){
		fs=new File(fuente)
		plantilla= new File(plantillaJST)
	}
	
	// Genera la página HTML, a aprtir de las listas: "code_text" y "docs_text", que contienen el contenido de la pagina
	private static void generarHTML (String destino){
	   fin = new File(destino)
	   def binding = [
		  title : titulo,
		  sections: sections, 
		  ruta: ruta,
	   ]
	   def engine = new groovy.text.SimpleTemplateEngine()
	   String text = '';
	   plantilla.eachLine{
		   text+=it
	   }
	   def template = engine.createTemplate(text).make(binding)
	   fin.write(template.toString())
	}
}