package es.fap.simpleled.ui.documentation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;
import org.eclipse.xtext.impl.KeywordImpl;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider;

public class FapDocumentationProvider extends DefaultEObjectHoverProvider implements IEObjectDocumentationProvider {

	public static INode node;
	public static boolean reference;
	
	public String getDocumentation(EObject o) {
		if (o instanceof KeywordImpl){
			EObject semantic = FapEObjectHelper.getKeywordContainer(node);
			return "<b>EObject:</b> " + semantic.getClass().getSimpleName() + "   <b>Keyword:</b> " + ((KeywordImpl)o).getValue();
		}
		
		String comment = parse(findComment(o));
		if (!comment.equals("")){
			comment = "<p>" + comment + "</p>";
		}
		
		if (reference){
			return comment + "<b>EObject:</b> " + o.getClass().getSimpleName() + " (referencia)";
		}
		return comment + "<b>EObject:</b> " + o.getClass().getSimpleName();
	}
	
	protected String getHoverInfoAsHtml(EObject o) {
//		if (!hasHover(o))
//			return null;
		StringBuffer buffer = new StringBuffer();
		buffer.append (getFirstLine(o));
		String documentation = getDocumentation(o);
		if (documentation != null && documentation.length() > 0) {
			buffer.append ("<p>");
			buffer.append (documentation);
			buffer.append("</p>");
		}
		return buffer.toString();
	}
	
	String ruleName = "ML_COMMENT";
	String startTag = "/\\*\\*?"; // regular expression
	String endTag = "\\*/"; // regular expression
	String linePrefix = "\\** ?"; // regular expression
	String linePostfix = "\\**"; // regular expression
	String whitespace = "( |\\t)*"; // regular expression
	
	protected String findComment(EObject o) {
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

	public String parse(String comment) {
		if (comment != null && !comment.equals("")) {
			comment = comment.replaceAll("\\A" + startTag, "");
			comment = comment.replaceAll(endTag + "\\z", "");
			comment = comment.replaceAll("(?m)^"+ whitespace + linePrefix, "");
			comment = comment.replaceAll("(?m)" + whitespace + linePostfix + whitespace + "$", "");
			return comment.trim();
		} else
			return "";
	}
	

}