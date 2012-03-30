package templates;

import es.fap.simpleled.led.*;
import es.fap.simpleled.led.util.ModelUtils
import generator.utils.Entidad;
import generator.utils.LedUtils
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import templates.elements.*;

public class GGroupElement extends GElement{

	public EList<Elemento> elementos;
	
	public GGroupElement(EObject element, GElement container){
		super(element, container);
	}
	
	public void generate(){
		// Se realiza una copia del array para que no se produzca un error de concurrencia.
		List<Elemento> copy = new ArrayList<Elemento>(elementos);
		for (Elemento elemento : copy)
			getInstance(elemento).generate();
	}
	
	public String controller(){
		String controller = "";
		for(Elemento elemento : elementos)
			controller += getInstance(elemento).controller();
		return controller;
	}
	
	public String routes(){
		String routes = "";
		for(Elemento elemento : elementos)
			routes += getInstance(elemento).routes();
		return routes;
	}
	
	public String saveCode(){
		String saveCode = "";
		for(Elemento elemento : elementos)
			saveCode += getInstance(elemento).saveCode();
		return saveCode;
	}
	
	public Set<Entidad> saveEntities(){
		Set<Entidad> saveEntities = super.saveEntities();
		for(Elemento elemento : elementos)
			saveEntities.addAll(getInstance(elemento).saveEntities());
		return saveEntities;
	}

	public Set<Entidad> dbEntities(){
		Set<Entidad> dbEntities = super.dbEntities();
		for(Elemento elemento : elementos)
			dbEntities.addAll(getInstance(elemento).dbEntities());
		return dbEntities;
	}
	
	public List<String> extraParams(){
		List<String> extraParams = super.extraParams();
		for(Elemento elemento : elementos)
			extraParams.addAll(getInstance(elemento).extraParams());
		return extraParams;
	}
	
	public String validateCopy(Stack<Set<String>> validatedFields){
		String out = "";
		if (element.permiso != null){
			validatedFields.push(new HashSet<String>());
			out += """if (secure.checkGrafico("${element.permiso.name}", "editable", accion, (Map<String,Long>)tags.TagMapStack.top("idParams"), null)) {\n""";
		}

		for (Elemento elemento: elementos)
			out += getInstance(elemento).validateCopy(validatedFields);

		if (element.permiso != null){
			validatedFields.pop();
			out += "\n}\n";
		}
		return out;
	}
	
	public String bindReferences(){
		String references = "";
		for(Elemento elemento : elementos)
			references += getInstance(elemento).bindReferences();
		return references;
	}
	
	public List<GElement> getInstancesOf(Class clazz){
		List<GElement> instances = super.getInstancesOf(clazz);
		for(Elemento elemento : elementos)
			instances.addAll(getInstance(elemento).getInstancesOf(clazz));
		return instances;
	}
	
	public void addElement(EObject element){
		elementos.add(element);
		getInstance(element).generate();
	}
	
	public void addElementBefore(EObject element, EObject before){
		int index = elementos.indexOf(before);
		if (index != -1)
			elementos.add(index, element);
		else
			elementos.add(element);
		getInstance(element).generate();
	}
	
	public void addElementAfter(EObject element, EObject after){
		int index = elementos.indexOf(after);
		if (index != -1)
			elementos.add(index + 1, element);
		else
			elementos.add(element);
		getInstance(element).generate();
	}
	
	public void replaceElement(EObject replacement, EObject replaced){
		int index = elementos.indexOf(replaced);
		if (index != -1){
			elementos.add(index, replacement);
			elementos.remove(replaced);
		}
		getInstance(replacement).generate();
	}
	
	public void removeElement(EObject element){
		elementos.remove(element);
	}
	
}
