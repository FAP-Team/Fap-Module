package es.fap.simpleled.ui.documentation;


import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.impl.KeywordImpl;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;


public class FapEObjectHelper extends EObjectAtOffsetHelper {

	public static EObject resolveElement(XtextResource resource, int offset) {
		return new FapEObjectHelper().internalResolveElementAt(resource, offset, true);
	}
	
	protected EObject internalResolveElementAt(XtextResource resource, int offset, boolean isContainment) {
		IParseResult parseResult = resource.getParseResult();
		if (parseResult != null && parseResult.getRootNode() != null) {
			ILeafNode leaf = NodeModelUtils.findLeafNodeAtOffset(parseResult.getRootNode(), offset);
			if(leaf.isHidden() && leaf.getOffset() == offset) {
				leaf = NodeModelUtils.findLeafNodeAtOffset(parseResult.getRootNode(), offset - 1);
			}
			INode node = leaf;
			
			if (node != null){
				EObject grammarElement = node.getGrammarElement();
				if (grammarElement instanceof KeywordImpl){
					FapDocumentationProvider.node = node;
					return grammarElement;
				}
			}
			while (node != null) {
				if (node.getGrammarElement() instanceof CrossReference) {
					return resolveCrossReferencedElement(node);
				} else if (isContainment && node.hasDirectSemanticElement()) {
					return node.getSemanticElement();
				} else {
					node = node.getParent();
				}
			}
		}
		return null;
	}
	
	public static EObject getKeywordContainer(INode node){
		return NodeModelUtils.findActualSemanticObjectFor(node);
	}
	
}