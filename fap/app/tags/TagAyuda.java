package tags;

public class TagAyuda {
	public String texto;
	public String textoPopup;
	public String refPopup;
	
	public TagAyuda(String texto, String textoPopup, String refPopup) {
		this.texto = texto;
		this.textoPopup = textoPopup;
		this.refPopup = refPopup;
	}

	public static TagAyuda texto(String texto){
		return new TagAyuda(texto, null, null);
	}

	public static TagAyuda textoPopup(String textoPopup){
		return new TagAyuda(null, textoPopup, null);
	}
	
	public static TagAyuda refPopup(String refPopup){
		return new TagAyuda(null, null, refPopup);
	}
	
	public boolean isEmpty(){
		return (texto == null || texto.isEmpty()) &&
		       (textoPopup == null || textoPopup.isEmpty()) &&
		       (refPopup == null || refPopup.isEmpty());
	}
}
