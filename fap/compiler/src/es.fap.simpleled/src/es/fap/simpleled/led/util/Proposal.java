package es.fap.simpleled.led.util;

public final class Proposal {

	public String text;
	public boolean valid;

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