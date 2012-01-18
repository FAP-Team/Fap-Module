package tags;

public class TagAyuda {
	public String texto;
	public String popover;
	
	public TagAyuda(String texto, String textoPopup) {
		this.texto = texto;
		this.popover = textoPopup;
	}

	public static TagAyuda texto(String texto){
		return new TagAyuda(texto, null);
	}

	public static TagAyuda popover(String textoPopup){
		return new TagAyuda(null, textoPopup);
	}
	
	public boolean isEmpty(){
		return (texto == null || texto.isEmpty()) &&
		       (popover == null || popover.isEmpty());
	}
}
