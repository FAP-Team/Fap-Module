package templates;

import java.io.StringWriter;


import com.sun.media.sound.RealTimeSequencer.PlayThread;

import generator.utils.*;
import es.fap.simpleled.led.Codigo
import info.bliki.wiki.model.WikiModel;
import jj.play.org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import jj.play.org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import jj.play.org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;

public class GCodigo extends GElement{
	Codigo codigo;
	
	public GCodigo(Codigo codigo, GElement container){
		super(codigo, container);
		this.codigo = codigo;
	}
	
	public String view(){
		return """

Código que genera esta página:
<pre class="prettyprint linenums" style=​"overflow:​ auto;​ ">
​<code>
<ol class=​"linenums"> ${codigo.getContenido()} </ol>
</code>
</pre>
		""";
	}
}