package templates;

import com.sun.media.sound.RealTimeSequencer.PlayThread;

import generator.utils.*;
import es.fap.simpleled.led.Wiki;
import info.bliki.wiki.model.WikiModel;


public class GWiki {

	def Wiki wiki;
	
	public static String generate(Wiki wiki){
		GWiki g = new GWiki();
		g.wiki = wiki;
		g.view();
	}
	
	public String view(){	
		//TODO: Problemas al cargar imagenes, solucion: overwrite the WikiModel#parseInternalImageLink()
		WikiModel wikiModel = new WikiModel("@{/public/images/\${image}}","@{\${title}Controller.index(idSolicitud)}");
					
		StringBuffer wikidatas = new StringBuffer();
		for(String data : wiki.getWikiData()){
			wikidatas.append(wikiModel.render(data).replaceAll("\r", ""));   // para quitar el salto de linea \r\n
		}
		
		String estilo =  wiki.estilo != null? wiki.estilo: "";
		
		String view =
		"""
			<div class="wiki ${estilo}">
${wikidatas}
			</div>
		"""
		return view;
	}
	
}
