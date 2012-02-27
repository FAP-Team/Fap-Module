package wfcomponent;

import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;

import generator.utils.*;

public class Start implements IWorkflowComponent {
	
	static path;
	def String target;
	String createSolicitud;
	
	// True si se está generando el módulo. False si la aplicación.
	public static boolean generatingModule;
	
	@Override
	public void preInvoke() {}

	@Override
	public void invoke(IWorkflowContext ctx) {
        System.setProperty("line.separator", "\n");
        FileUtils.target = target;
		generatingModule = !createSolicitud.equals("true");
	}

	@Override
	public void postInvoke() {}
}
