package es.fap.simpleled.ui.documentation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.xtext.impl.KeywordImpl;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.IGlobalServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.hover.DispatchingEObjectTextHover;
import org.eclipse.xtext.ui.editor.hover.IEObjectHoverProvider;
import org.eclipse.xtext.ui.editor.hover.IEObjectHoverProvider.IInformationControlCreatorProvider;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Tuples;

import com.google.inject.Inject;

public class FapHoverProvider extends DispatchingEObjectTextHover {

	@Inject
	private EObjectAtOffsetHelper eObjectAtOffsetHelper;
	
	@Inject 
	private IGlobalServiceProvider serviceProvider;
	
	@Inject 
	private IEObjectHoverProvider fapHoverProvider;
	
	private IInformationControlCreatorProvider lastCreatorProvider;

	protected Pair<EObject, IRegion> getXtextElementAt(XtextResource resource, final int offset) {
		// check for cross reference
		EObject crossLinkedEObject = eObjectAtOffsetHelper.resolveCrossReferencedElementAt(resource, offset);
		if (crossLinkedEObject != null) {
			if (!crossLinkedEObject.eIsProxy()) {
				ILeafNode leafNode = NodeModelUtils.findLeafNodeAtOffset(resource.getParseResult().getRootNode(), offset);
				if(leafNode.isHidden() && leafNode.getOffset() == offset) {
					leafNode = NodeModelUtils.findLeafNodeAtOffset(resource.getParseResult().getRootNode(), offset - 1);
				}
				FapDocumentationProvider.reference = true;
				return Tuples.create(crossLinkedEObject, (IRegion) new Region(leafNode.getOffset(), leafNode.getLength()));
			}
		} else {
			EObject o = FapEObjectHelper.resolveElement(resource, offset);
			if (o != null) {
//				ITextRegion region = locationInFileProvider.getSignificantTextRegion(o);
//				final IRegion region2 = new Region(region.getOffset(), region.getLength());
//				if (TextUtilities.overlaps(region2, new Region(offset, 0)))
//					return Tuples.create(o, region2);
				FapDocumentationProvider.reference = false;
				return Tuples.create(o, (IRegion)new Region(offset, 0));
			}
		}
		return null;
	}
	
	public Object getHoverInfo(EObject first, ITextViewer textViewer, IRegion hoverRegion) {
		IEObjectHoverProvider hoverProvider = null;
		if (first instanceof KeywordImpl){
			hoverProvider = fapHoverProvider;
		}
		else{
			hoverProvider = serviceProvider.findService(first, IEObjectHoverProvider.class);
		}
		if (hoverProvider==null)
			return null;
		IInformationControlCreatorProvider creatorProvider = hoverProvider.getHoverInfo(first, textViewer, hoverRegion);
		if (creatorProvider==null)
			return null;
		this.lastCreatorProvider = creatorProvider;
		return lastCreatorProvider.getInfo();
	}
	
	public IInformationControlCreator getHoverControlCreator() {
		return this.lastCreatorProvider==null?null:lastCreatorProvider.getHoverControlCreator();
	}
	
}