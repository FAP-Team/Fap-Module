package templates;


import generator.utils.*;
import es.fap.simpleled.led.*;
import generator.utils.HashStack.HashStackName;

public class GEnlace {

	def Enlace enlace;
	
	public static String generate(Enlace enlace){
		GEnlace g = new GEnlace();
		g.enlace = enlace;
		g.view();
	}
	
	public String view(){
		
		TagParameters p = new TagParameters()
		
		if(enlace.name){
			p.putStr("id", enlace.name)
		}
		
		if(enlace.titulo){
			p.putStr("titulo", enlace.titulo)
		}
		
		if(enlace.destino){
			p.putStr("target", enlace.destino)
		}
		
		if(enlace.url){
			p.putStr("url", enlace.url)
		}
		
		if(enlace.anterior){
			p.put("anterior", true)
			p.putStr("container", HashStack.top(HashStackName.CONTAINER).name);
		}
		
		if(enlace.pagina){
			Controller pagUtil = Controller.fromPagina(enlace.pagina.pagina).initialize();
			p.put("action", pagUtil.getRouteIndex(enlace.pagina.accion));
		}
		
		if(enlace.popup){
			Controller popupUtil = Controller.fromPopup(enlace.popup.popup).initialize();
			p.putStr("popup", enlace.popup.popup.name);
			p.put("action", popupUtil.getRouteIndex(enlace.popup.accion));
		}
		
		if(enlace.campo != null){
			p.put("url", CampoUtils.create(enlace.campo).firstLower())
		}
		
		if(enlace.estilo){
			p.putStr("estilo", enlace.estilo);
		}
		
		
		String view = """
		#{fap.enlace ${p.lista()} /}"""
		
		return view;	
	}
	
}
