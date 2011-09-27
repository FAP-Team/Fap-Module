package es.fap.simpleled.ui.documentation;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension4;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;
import org.eclipse.xtext.impl.KeywordImpl;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider;

import es.fap.simpleled.led.Entity;

public class FapDocumentationProvider extends DefaultEObjectHoverProvider implements IEObjectDocumentationProvider {

	public static INode node;
	public static boolean reference;
	
	public String getDocumentation(EObject o) {
		if (o instanceof KeywordImpl){
			EObject semantic = FapEObjectHelper.getKeywordContainer(node);
			return "<b>EObject:</b> " + semantic.getClass().getSimpleName() + "   <b>Keyword:</b> " + ((KeywordImpl)o).getValue();
		}
		if (o instanceof Entity){
			return new EntidadDocumentation().getDocumentation(o);
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
	
	protected String getFirstLine(EObject o) {
		if (o instanceof Entity){
			return new EntidadDocumentation().getFirstLine(o);
		}
		String label = getLabel(o);
		if (label == null){
			label = "";
		}
		return o.eClass().getName() + " <b>" + label + "</b>";
	}
	
	protected String getHoverInfoAsHtml(EObject o) {
//		if (!hasHover(o))
//			return null;
		StringBuffer buffer = new StringBuffer();
		String firstLine = getFirstLine(o);
		String documentation = getDocumentation(o);

		if (!firstLine.equals("")){
			buffer.append (firstLine);
			if (documentation != null && documentation.length() > 0) {
				buffer.append ("<p>");
				buffer.append (documentation);
				buffer.append("</p>");
			}
		}
		else{
			if (documentation != null && documentation.length() > 0) {
				buffer.append (documentation);
			}
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
	
	private IInformationControlCreator hoverControlCreator;
	
	public IInformationControlCreator getHoverControlCreator() {
		if (hoverControlCreator == null)
			hoverControlCreator = new HoverControlCreator(getInformationPresenterControlCreator());
		return hoverControlCreator;
	}
	
	public final class HoverControlCreator extends AbstractReusableInformationControlCreator {

		private final IInformationControlCreator fInformationPresenterControlCreator;

		public HoverControlCreator(IInformationControlCreator informationPresenterControlCreator) {
			fInformationPresenterControlCreator = informationPresenterControlCreator;
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#doCreateInformationControl(org.eclipse.swt.widgets.Shell)
		 */
		@Override
		public IInformationControl doCreateInformationControl(Shell parent) {
			String tooltipAffordanceString = EditorsUI.getTooltipAffordanceString();
			if (BrowserInformationControl.isAvailable(parent)) {
				String font = "org.eclipse.jdt.ui.javadocfont"; // FIXME: PreferenceConstants.APPEARANCE_JAVADOC_FONT;
				MyBrowserInformationControl iControl = new MyBrowserInformationControl(parent, font,
						tooltipAffordanceString) {
					/*
					 * @see org.eclipse.jface.text.IInformationControlExtension5#getInformationPresenterControlCreator()
					 */
					@Override
					public IInformationControlCreator getInformationPresenterControlCreator() {
						return fInformationPresenterControlCreator;
					}
				};
				addLinkListener(iControl);
				return iControl;
			} else {
				return new DefaultInformationControl(parent, tooltipAffordanceString);
			}
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#canReuse(org.eclipse.jface.text.IInformationControl)
		 */
		@Override
		public boolean canReuse(IInformationControl control) {
			if (!super.canReuse(control))
				return false;

			if (control instanceof IInformationControlExtension4) {
				String tooltipAffordanceString = EditorsUI.getTooltipAffordanceString();
				((IInformationControlExtension4) control).setStatusText(tooltipAffordanceString);
			}

			return true;
		}
	}
	
	public class MyBrowserInformationControl extends BrowserInformationControl{

		public MyBrowserInformationControl(Shell parent, String symbolicFontName, String statusFieldText) {
			super(parent, symbolicFontName, statusFieldText);
		}
		
		public void setSize(int width, int height) {
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			super.setSize(d.width / 4, d.height / 4);
		}
		
	}
	

}