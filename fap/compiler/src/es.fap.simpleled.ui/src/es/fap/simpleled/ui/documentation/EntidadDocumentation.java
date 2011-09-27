package es.fap.simpleled.ui.documentation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.internal.text.html.BrowserInformationControlInput;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;
import org.eclipse.xtext.impl.KeywordImpl;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.hover.DispatchingEObjectTextHover;
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider;
import org.eclipse.xtext.ui.editor.hover.html.XtextBrowserInformationControlInput;

import es.fap.simpleled.led.Entity;

public class EntidadDocumentation extends FapDocumentation {

	public String getDocumentation(EObject o) {
		Entity entidad = (Entity) o;
//		String out = "<b>Entidad " + entidad.getName();
//		if (entidad.getExtends() != null){
////			out += " extends "
//		}
//		
		XtextBrowserInformationControlInput i;
//		BrowserInformationControlInput b;
//		b.
		String text = NodeModelUtils.getNode(o).getText();
//		text = text.trim().replaceAll("\n", "</br>").replaceAll(" ", "&nbsp;").replaceAll("\t", "&nbsp;&nbsp;&nbsp;");
		
		DispatchingEObjectTextHover f;
//		return stringToHTMLString(string)(text);
		return text.trim().replaceAll("\n", "</br>").replaceAll(" ", "&nbsp;").replaceAll("\t", "&nbsp;&nbsp;&nbsp;");
		
//		
//		entidad.getAnotaciones().
//		
//		String comment = parse(findComment(o));
//		if (!comment.equals("")){
//			comment = "<p>" + comment + "</p>";
//		}
//		
//		if (reference){
//			return comment + "<b>EObject:</b> " + o.getClass().getSimpleName() + " (referencia)";
//		}
//		return comment + "<b>EObject:</b> " + o.getClass().getSimpleName();
	}
	
	public String getFirstLine(EObject o) {
		return "";
	}
	
	public static String stringToHTMLString(String string) {
	    StringBuffer sb = new StringBuffer(string.length());
	    // true if last char was blank
	    boolean lastWasBlankChar = false;
	    int len = string.length();
	    char c;

	    for (int i = 0; i < len; i++)
	        {
	        c = string.charAt(i);
	        if (c == ' ') {
	            // blank gets extra work,
	            // this solves the problem you get if you replace all
	            // blanks with &nbsp;, if you do that you loss 
	            // word breaking
	            if (lastWasBlankChar) {
	                lastWasBlankChar = false;
	                sb.append("&nbsp;");
	                }
	            else {
	                lastWasBlankChar = true;
	                sb.append(' ');
	                }
	            }
	        else {
	            lastWasBlankChar = false;
	            //
	            // HTML Special Chars
	            if (c == '"')
	                sb.append("&quot;");
	            else if (c == '&')
	                sb.append("&amp;");
	            else if (c == '<')
	                sb.append("&lt;");
	            else if (c == '>')
	                sb.append("&gt;");
	            else if (c == '\n')
	                // Handle Newline
	                sb.append("&lt;br/&gt;");
	            else {
	                int ci = 0xffff & c;
	                if (ci < 160 )
	                    // nothing special only 7 Bit
	                    sb.append(c);
	                else {
	                    // Not 7 Bit use the unicode system
	                    sb.append("&#");
	                    sb.append(new Integer(ci).toString());
	                    sb.append(';');
	                    }
	                }
	            }
	        }
	    return sb.toString();
	}
	
	
}