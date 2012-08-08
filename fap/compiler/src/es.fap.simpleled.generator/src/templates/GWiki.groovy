package templates;

import java.io.StringWriter;


import com.sun.media.sound.RealTimeSequencer.PlayThread;

import generator.utils.*;
import es.fap.simpleled.led.Wiki;
import info.bliki.wiki.model.WikiModel;
import jj.play.org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import jj.play.org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import jj.play.org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;


public class GWiki extends GElement{

	Wiki wiki;
	
	public GWiki(Wiki wiki, GElement container){
		super(wiki, container);
		this.wiki = wiki;
	}
	
	public String view(){	
		StringBuffer wikidata = new StringBuffer();
		String convertirComillasDolar="", dolar="", acumulado="", buscarLlaveMasCercana="";
		String porcentaje="", acumuladoGroovy="";
		String dataGroovy="";
		for(String data : wiki.getWikiData()){
			if (data.contains("\${") || data.contains("%{")){ // Para que no se convierta a UTF8 lo englobado en ${...} y en %{...%}
				dolar="";
				acumulado="";
				dataGroovy=data;
				convertirComillasDolar=getParsedText(data);
				while (data.indexOf("\${") != -1){
					buscarLlaveMasCercana=data.substring(data.indexOf("\${"));
					
					dolar=buscarLlaveMasCercana.substring(buscarLlaveMasCercana.indexOf("\${"), buscarLlaveMasCercana.indexOf("}")+1);
					
					acumulado+=convertirComillasDolar.substring(0, convertirComillasDolar.indexOf("\${"));
					
					acumulado+=dolar;
					
					convertirComillasDolar=convertirComillasDolar.substring(convertirComillasDolar.indexOf("\${"));
					convertirComillasDolar=convertirComillasDolar.substring(convertirComillasDolar.indexOf("}")+1);
					
					data=data.substring(data.indexOf("\${"));
					data=data.substring(data.indexOf("}")+1);
				}
				acumulado+=convertirComillasDolar;
				
				acumuladoGroovy="";
				porcentaje="";
				while (dataGroovy.indexOf("%{") != -1){
		
					porcentaje=dataGroovy.substring(dataGroovy.indexOf("%{"), dataGroovy.indexOf("%}")+2);

					acumuladoGroovy+=acumulado.substring(0, acumulado.indexOf("%{"));

					acumuladoGroovy+=porcentaje;
					
					acumulado=acumulado.substring(acumulado.indexOf("%}")+2);

					dataGroovy=dataGroovy.substring(dataGroovy.indexOf("%}")+2);
				}
				if (!acumuladoGroovy.isEmpty()){
					acumuladoGroovy+=acumulado;
					acumulado=acumuladoGroovy;
				}
				wikidata.append(acumulado + "\n");
			} else {
				wikidata.append(getParsedText(data) + "\n");
			}
		}
		
//		//TODO: Problemas al cargar imagenes, solucion: overwrite the WikiModel#parseInternalImageLink()
//		WikiModel wikiModel = new WikiModel("@{/public/images/\${image}}","@{\${title}Controller.index(idSolicitud)}");

		String estilo =  wiki.estilo? wiki.estilo: "";
		
		return """
			<div class="wiki ${estilo}">
${wikidata}
			</div>
		""";
		return view;
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
	
}
