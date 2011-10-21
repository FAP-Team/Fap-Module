package es.fap.simpleled.led.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import es.fap.simpleled.led.util.DocElemento;
import es.fap.simpleled.led.util.DocParametro;

public class LedDocumentationUtils {

	public static String url = "http://localhost:9003/@documentation/modules/fap/";
	
	public static String findComment(EObject o) {
		String ruleName = "ML_COMMENT";
		String startTag = "/\\*\\*?"; // regular expression
		String returnValue = "";
		ICompositeNode node = NodeModelUtils.getNode(o);
		if (node != null) {
			// get the last multi line comment before a non hidden leaf node
			for (INode abstractNode : node.getAsTreeIterable()) {
				if (abstractNode instanceof ILeafNode && !((ILeafNode) abstractNode).isHidden())
					break;
				if (abstractNode instanceof ILeafNode && abstractNode.getGrammarElement() instanceof TerminalRule
						&& ruleName.equalsIgnoreCase(((TerminalRule) abstractNode.getGrammarElement()).getName())) {
					String comment = ((ILeafNode) abstractNode).getText();
					if (comment.matches("(?s)" + startTag + ".*")) {
						returnValue = comment;
					}
				}
			}
		}
		return returnValue;
	}

	public static String parseComment(String comment) {
		String endTag = "\\*/"; // regular expression
		String whitespace = "( |\\t)*"; // regular expression
		String linePrefix = "\\** ?"; // regular expression
		String linePostfix = "\\**"; // regular expression
		String startTag = "/\\*\\*?"; // regular expression
		if (comment != null && !comment.equals("")) {
			comment = comment.replaceAll("\\A" + startTag, "");
			comment = comment.replaceAll(endTag + "\\z", "");
			comment = comment.replaceAll("(?m)^"+ whitespace + linePrefix, "");
			comment = comment.replaceAll("(?m)" + whitespace + linePostfix + whitespace + "$", "");
			return comment.trim();
		} else
			return "";
	}
	
	public static String findAndParseComment(EObject o) {
		return parseComment(findComment(o));
	}
	
	public static String spaces(int n){
		String spaces = "";
		for (int i = 0; i < n; i++){
			spaces += "&nbsp;";
		}
		return spaces;
	}
	
	public static String getDocumentation(DocElemento elemento) {
		if (elemento == null){
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("<b>" + getHref(elemento.nombre, elemento.keyword) + "</b> </span><span style=\"color:#666\">&nbsp;&nbsp;Haz click para ir a la pagina de documentacion</span>");
		buffer.append("<p>" + elemento.descripcion + "</p>");
		buffer.append("<p><b>Parametros:</b></p>");
		for (DocParametro parametro: elemento.parametros){
			String param = getStyledNombre(parametro.nombre.split(" ")[0]);
			String opcional = "";
			if (parametro.tipo.equals("opcional")){
				opcional = spaces(2) + "(opcional)";
			}
			buffer.append("<span style=\"color:#800\">" + spaces(2) + "-" + spaces(2) + param + "</span><span style=\"color:#666\">" + opcional + "</span></br>");
		}
		return buffer.toString();
	}
	
	public static String getStyledNombre(String parametro){
		String styled1 = "";
		String styled2 = parametro;
		while (!styled1.equals(styled2)){
			styled1 = styled2;
			styled2 = styled2.replaceFirst("_", "<i>").replaceFirst("_", "</i>");
		}
		return styled1;
	}
	
	public static String getDocumentation(DocParametro parametro) {
		if (parametro == null){
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("<b>" + parametro.keyword + "</b>");
		buffer.append("<p>" + parametro.descripcion + "</p>");
		buffer.append("<p><b>Sintaxis:</b></p>");
		String param = getStyledNombre(parametro.nombre);
		String opcional = "";
		if (parametro.tipo.equals("opcional")){
			opcional = spaces(2) + "(opcional)";
		}
		buffer.append("<span style=\"color:#800\">" + spaces(2) + param + "</span><span style=\"color:#666\">" + opcional + "</span>");
		return buffer.toString();
	}

	public static String getHref(String pagina, String text){
		return "<a href=\"" + url + "dsl-" + pagina + "\">" + text + "</a>";
	}
	
	public static String getHrefNoDsl(String pagina, String text){
		return "<a href=\"" + url + pagina + "\">" + text + "</a>";
	}
	
	public static String utf(String utf){
		try {
			return new String(utf.getBytes(Charset.defaultCharset()), "UTF8");
		} catch (UnsupportedEncodingException e) {}
		return "";
	}
	
}