package templates;

import javax.management.InstanceOfQueryExp;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource

import es.fap.simpleled.led.*;
import generator.utils.LedUtils;

public class Expand {
	
	public static void expand(EObject object){
		LedUtils.resource = object.eResource();
		if (LedUtils.inPath(object))
			GElement.getInstance(object, null).generate();
	}
}
