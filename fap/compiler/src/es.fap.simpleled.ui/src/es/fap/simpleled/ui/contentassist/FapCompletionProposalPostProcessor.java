package es.fap.simpleled.ui.contentassist;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalPostProcessor;

public class FapCompletionProposalPostProcessor implements ICompletionProposalPostProcessor{

	/*
	 * Se ha comentado ese extracto de la función para modificar el comportamiento por defecto del autocompletado de Xtext
	 * en lo referente a no insertar automaticamente la propuesta de autocompletado cuando ésta es única.
	 */
	public ICompletionProposal[] postProcess(ICompletionProposal[] proposals) {
//		if (proposals.length == 1) {
//			if (proposals[0] instanceof ConfigurableCompletionProposal) {
//				ConfigurableCompletionProposal proposal = (ConfigurableCompletionProposal) proposals[0];
//				if (proposal.isAutoInsertable() && proposal.getReplaceContextLength() > proposal.getReplacementLength()) {
//					proposal.setAutoInsertable(false);
//				}
//			}
//		}
		return proposals;
	}

}
