package utils;

import java.io.StringWriter;
import java.util.Random;

import jj.play.org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import jj.play.org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import jj.play.org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;

public class StringUtils {

	/**
	 * Comprueba si un string están en una lista
	 * @param str Cadena a comprobar
	 * @param list Lista donde se va a comprobar
	 * @return
	 */
	public static boolean in(String str, String...list){
		for(String s : list){
			if(str == null){
				if(s == null) return true;
			}else if(str.equals(s)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Comprueba si un string no están en una lista
	 * @param str Cadena a comprobar
	 * @param list Lista donde se va a comprobar
	 * @return
	 */
	public static boolean notin(String str, String...list){
		return !in(str, list);
	}
	
	/**
	 * Devuelve una cadena aleatoria
	 * @param size
	 * @return
	 */
	public static String aleatorio(int size) {
		final int DEF_SIZE = 512;		
		StringBuffer cadena = new StringBuffer(DEF_SIZE);
		Random random = new Random();

		for (int i = 0; i < size; i++) {
			int randInt = random.nextInt(26) + 65;
			char randChar = (char) randInt;

			cadena.append(randChar);
		}

		return cadena.toString();
	}
	
	/**
	 * Hace un join de la lista de String con un separador
	 * @param separador
	 * @param argumentos
	 * @return
	 */
	public static String join(String separador, String...argumentos){
		StringBuilder sb = new StringBuilder();
		for (String s : argumentos) {
			if(s != null && !s.isEmpty()){
				sb.append(s);
				sb.append(separador);
			}
		}
		return sb.toString();
	}
	
	// Permite convertir un String con formato Textile en contenido válido en ese formato para HTML
	public static StringBuffer getParsedText(String textile){
		MarkupParser parser = new MarkupParser(new TextileLanguage());
		StringWriter writer = new StringWriter();
		HtmlDocumentBuilder builder = new HtmlDocumentBuilder(writer);
		builder.setEmitAsDocument(false);
		parser.setBuilder(builder);
		StringBuffer wikidatas = new StringBuffer();
		parser.parse(textile);
		wikidatas.append(writer.toString());
		return wikidatas;
	}
}
