package templates;


import generator.utils.*;
import es.fap.simpleled.led.*;

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
		
		if(enlace.pagina){
			p.put("action", ControllerUtils.refPaginaAction(enlace.pagina))
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
