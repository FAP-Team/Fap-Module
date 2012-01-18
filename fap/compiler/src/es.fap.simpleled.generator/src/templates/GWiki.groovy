package templates;

import java.io.StringWriter;

import com.sun.media.sound.RealTimeSequencer.PlayThread;

import generator.utils.*;
import es.fap.simpleled.led.Wiki;
import info.bliki.wiki.model.WikiModel;
import jj.play.org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import jj.play.org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import jj.play.org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;


public class GWiki {

	def Wiki wiki;
	
	public static String generate(Wiki wiki){
		GWiki g = new GWiki();
		g.wiki = wiki;
		g.view();
	}
	
	private String getParsedText(String textile){
		MarkupParser parser = new MarkupParser(new TextileLanguage());
		StringWriter writer = new StringWriter();
		HtmlDocumentBuilder builder = new HtmlDocumentBuilder(writer);
		builder.setEmitAsDocument(false);
		parser.setBuilder(builder);
		StringBuffer wikidatas = new StringBuffer();
		parser.parse(textile);
		wikidatas.append(writer.toString());
	}
	
	public String view(){	
		StringBuffer wikidata = new StringBuffer();
		for(String data : wiki.getWikiData()){
			wikidata.append(getParsedText(data) + "\n");
		}
		
//		//TODO: Problemas al cargar imagenes, solucion: overwrite the WikiModel#parseInternalImageLink()
//		WikiModel wikiModel = new WikiModel("@{/public/images/\${image}}","@{\${title}Controller.index(idSolicitud)}");

		String estilo =  wiki.estilo? wiki.estilo: "";
		
		String view =
		"""
			<div class="wiki ${estilo}">
${wikidata}
			</div>
		"""
		return view;
	}
	
}
