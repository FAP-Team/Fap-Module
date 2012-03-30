package templates;


import generator.utils.*;
import es.fap.simpleled.led.*;

public class GEnlace extends GElement{

	Enlace enlace;
	
	public GEnlace(Enlace enlace, GElement container){
		super(enlace, container);
		this.enlace = enlace;
	}
	
	public String view(){
		TagParameters p = new TagParameters()
		
		if(enlace.name)
			p.putStr("id", enlace.name)
		if(enlace.titulo)
			p.putStr("titulo", enlace.titulo)
		if(enlace.destino)
			p.putStr("target", enlace.destino)
		if(enlace.url)
			p.putStr("url", enlace.url)
		if(enlace.anterior){
			p.put("anterior", true)
			p.putStr("container", getPaginaOrPopupContainer().name);
		}
		if(enlace.pagina)
			p.put("action", Controller.create(GElement.getInstance(enlace.pagina.pagina, null)).getRouteIndex(enlace.pagina.accion));
		if(enlace.popup){
			p.putStr("popup", enlace.popup.popup.name);
			p.put("action", Controller.create(GElement.getInstance(enlace.popup.popup, null)).getRouteIndex(enlace.popup.accion));
		}
		if(enlace.campo != null)
			p.put("url", CampoUtils.create(enlace.campo).firstLower())
		if(enlace.estilo)
			p.putStr("estilo", enlace.estilo);
		
		return """
			#{fap.enlace ${p.lista()} /}
		""";
	}
	
}
