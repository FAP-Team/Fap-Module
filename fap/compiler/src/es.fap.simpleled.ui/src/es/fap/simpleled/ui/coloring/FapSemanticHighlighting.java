package es.fap.simpleled.ui.coloring;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.swt.graphics.RGB;
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
	
	public void configure(IHighlightingConfigurationAcceptor acceptor) {
		super.configure(acceptor);
		acceptor.acceptDefaultHighlighting(REFERENCE_ID, "Reference", referenceStyle());
		acceptor.acceptDefaultHighlighting(NAME_ID, "Name", nameStyle());
	}
	
	public TextStyle stringTextStyle() {
		TextStyle textStyle = new TextStyle();
		textStyle.setColor(new RGB(64, 128, 128));
		return textStyle;
	}
	
	public TextStyle nameStyle() {
		TextStyle textStyle = new TextStyle();
		textStyle.setColor(new RGB(200, 0, 0));
		return textStyle;
	}
	
	public TextStyle commentTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setColor(new RGB(128, 128, 128));
		return textStyle;
	}
	
	public TextStyle referenceStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setColor(new RGB(32, 0, 183));
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
				if (keyword.getValue().equals("action") || keyword.getValue().equals("agente")){
					acceptor.addPosition(node.getOffset(), node.getLength(), REFERENCE_ID);
					continue;
				}
				String feature = FapDocumentationProvider.getFeature(node);
				if ("type".equals(feature)){
					acceptor.addPosition(node.getOffset(), node.getLength(), REFERENCE_ID);
					continue;
				}
			}
			INode node2 = node;
			while (node2 != null && !node2.hasDirectSemanticElement()) {
				node2 = node2.getParent();
			}
			if (node2 != null) {
				EObject semantic = node2.getSemanticElement();
				try {
					if (semantic.getClass().getMethod("getName", null).invoke(semantic, null) == null){
						continue;
					}
				} catch (Exception e) {
					continue;
				}
				ITextRegion region = locationInFileProvider.getSignificantTextRegion(semantic);
				IRegion region2 = new Region(region.getOffset(), region.getLength());
				if (TextUtilities.overlaps(region2, new Region(node.getOffset(), 0))){
					acceptor.addPosition(node.getOffset(), node.getLength(), NAME_ID);
				}
			}
		}
	}
	
	

}
