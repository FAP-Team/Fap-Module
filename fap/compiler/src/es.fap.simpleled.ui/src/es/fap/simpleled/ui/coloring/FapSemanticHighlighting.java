package es.fap.simpleled.ui.coloring;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.resource.ILocationInFileProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightedPositionAcceptor;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator;
import org.eclipse.xtext.ui.editor.utils.TextStyle;
import org.eclipse.xtext.util.ITextRegion;

import com.google.inject.Inject;

import es.fap.simpleled.ui.documentation.FapDocumentationProvider;

public class FapSemanticHighlighting extends DefaultHighlightingConfiguration implements ISemanticHighlightingCalculator {

	@Inject
	private ILocationInFileProvider locationInFileProvider;
	
	public static final String REFERENCE_ID = "reference";
	public static final String NAME_ID = "name";
	public static final String ELEMENT_ID = "element";
	
	public static final RGB nameColor = new RGB(200, 0, 0);
	public static final RGB referenceColor = new RGB(32, 0, 183);
	public static final RGB keywordColor = new RGB(127, 0, 85);
	public static final RGB stringColor = new RGB(64, 128, 128);
	public static final RGB elementColor = new RGB(210, 0, 25);
	
	public void configure(IHighlightingConfigurationAcceptor acceptor) {
		super.configure(acceptor);
		acceptor.acceptDefaultHighlighting(REFERENCE_ID, "Reference", referenceStyle());
		acceptor.acceptDefaultHighlighting(NAME_ID, "Name", nameStyle());
		acceptor.acceptDefaultHighlighting(ELEMENT_ID, "Element", elementStyle());
	}
	
	public TextStyle stringTextStyle() {
		TextStyle textStyle = new TextStyle();
		textStyle.setColor(new RGB(64, 128, 128));
		return textStyle;
	}
	
	public TextStyle elementStyle() {
		TextStyle textStyle = new TextStyle();
		textStyle.setColor(elementColor);
		textStyle.setStyle(SWT.BOLD);
		return textStyle;
	}
	
	public TextStyle nameStyle() {
		TextStyle textStyle = new TextStyle();
		textStyle.setColor(nameColor);
		return textStyle;
	}
	
	public TextStyle commentTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setColor(new RGB(128, 128, 128));
		return textStyle;
	}
	
	public TextStyle referenceStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setColor(referenceColor);
		return textStyle;
	}
	
	public void provideHighlightingFor(XtextResource resource, IHighlightedPositionAcceptor acceptor) {
		if (resource == null || resource.getParseResult() == null){
			return;
		}
		INode root = resource.getParseResult().getRootNode();
		for (INode node : root.getAsTreeIterable()) {
			EObject grammar = node.getGrammarElement();
			if (grammar instanceof CrossReference) {
				acceptor.addPosition(node.getOffset(), node.getLength(), REFERENCE_ID);
				continue;
			}
			if (grammar instanceof Keyword){
				Keyword keyword = (Keyword) grammar;
				String val = keyword.getValue();
				if (val.equals("accion")
					|| val.equals("agente")
					|| val.equals("editar")
					|| val.equals("crear")
					|| val.equals("leer")
					|| val.equals("borrar")
					|| val.equals("editable")
					|| val.equals("visible")
					|| val.equals("oculto")
					|| val.equals("all")
					|| val.equals("_grafico")
					|| val.equals("_accion")
					|| val.equals("denegar")
				){
					acceptor.addPosition(node.getOffset(), node.getLength(), REFERENCE_ID);
					continue;
				}
				Assignment assign = FapDocumentationProvider.getDocFeature(node);
				if (assign != null && "type".equals(assign.getFeature())){
					acceptor.addPosition(node.getOffset(), node.getLength(), REFERENCE_ID);
					continue;
				}
				if ("elemento".equals(FapDocumentationProvider.getFeature(node))){
					acceptor.addPosition(node.getOffset(), node.getLength(), ELEMENT_ID);
					continue;
				}
				
			}
			if ("key".equals(FapDocumentationProvider.getFeature(node))){
				acceptor.addPosition(node.getOffset(), node.getLength(), REFERENCE_ID);
				continue;
			}
			INode node2 = node;
			while (node2 != null && !node2.hasDirectSemanticElement())
				node2 = node2.getParent();
			if (node2 != null) {
				EObject semantic = node2.getSemanticElement();
				try {
					if (semantic.getClass().getMethod("getName").invoke(semantic) != null){
						ITextRegion region = locationInFileProvider.getSignificantTextRegion(semantic);
						IRegion region2 = new Region(region.getOffset(), region.getLength());
						if (TextUtilities.overlaps(region2, new Region(node.getOffset(), 0))){
							acceptor.addPosition(node.getOffset(), node.getLength(), NAME_ID);
							continue;
						}
					}
				} catch (Exception e) {}
			}
		}
	}
	
}
