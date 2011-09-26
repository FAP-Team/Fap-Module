package wfcomponent;

import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;

import generator.utils.*;

public class Start implements IWorkflowComponent {
	
	static path;
	def String target;
	String createSolicitud;
	
	
	@Override
	public void preInvoke() {}

	@Override
	public void invoke(IWorkflowContext ctx) {
		FileUtils.target = target;
		LedUtils.generatingModule = !createSolicitud.equals("true");
	}

	@Override
	public void postInvoke() {}
}
