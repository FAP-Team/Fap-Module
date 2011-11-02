package es.fap.simpleled.led.util;

public final class Proposal {

	public String text;
	public boolean valid;
	public Integer priority;

	public Proposal(String text, boolean valid) {
		this.text = text;
		this.valid = valid;
		this.priority = 0;
	}
	
	public Proposal(String text, boolean valid, int priority) {
		this.text = text;
		this.valid = valid;
		this.priority = priority;
	}
	
	public String getEditorText(){
		String dot = "";
		if (!valid){
			dot = ".";
		}
		return text.split("-")[0].trim() + dot;
	}

}