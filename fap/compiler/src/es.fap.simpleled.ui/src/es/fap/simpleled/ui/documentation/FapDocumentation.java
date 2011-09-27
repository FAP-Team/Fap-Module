package es.fap.simpleled.ui.documentation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

public abstract class FapDocumentation {

	
	public abstract String getDocumentation(EObject o);
	
	public abstract String getFirstLine(EObject o);
	
	
	
	private static String ruleName = "ML_COMMENT";
	private static String startTag = "/\\*\\*?"; // regular expression
	private static String endTag = "\\*/"; // regular expression
	private static String linePrefix = "\\** ?"; // regular expression
	private static String linePostfix = "\\**"; // regular expression
	private static String whitespace = "( |\\t)*"; // regular expression
	
	public static String findComment(EObject o) {
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

//	public static String getTextWithoutComments(EObject o) {
//		String returnValue = "";
//		ICompositeNode node = NodeModelUtils.getNode(o);
//		if (node != null) {
//			// get the last multi line comment before a non hidden leaf node
//			for (INode abstractNode : node.getAsTreeIterable()) {
//				if (abstractNode instanceof ILeafNode && !((ILeafNode) abstractNode).isHidden()){
//					returnValue += abstractNode.getText();
//					continue;
//				}
//				if (!( abstractNode instanceof ILeafNode && abstractNode.getGrammarElement() instanceof TerminalRule
//						&& ruleName.equalsIgnoreCase(((TerminalRule) abstractNode.getGrammarElement()).getName()))) {
////					String comment = ((ILeafNode) abstractNode).getText();
////					if (comment.matches("(?s)" + startTag + ".*")) {
//						returnValue += abstractNode.getText();
////					}
//				}
//			}
//		}
//		return returnValue;
//	}
	
	public static String parse(String comment) {
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