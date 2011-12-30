package es.fap.simpleled.led.util;

import org.eclipse.emf.ecore.EObject;

public final class Proposal {

	public String text;
	public boolean valid;
	public EObject atributo;
	
	public Proposal(String text, boolean valid, EObject atributo) {
		this.text = text;
		this.valid = valid;
		this.atributo = atributo;
	}
	
	public Proposal(String text, boolean valid) {
		this.text = text;
		this.valid = valid;
	}
	
	public String getEditorText(){
		String dot = "";
		if (!valid){
			dot = ".";
		}
		return text.split("-")[0].trim() + dot;
	}

}