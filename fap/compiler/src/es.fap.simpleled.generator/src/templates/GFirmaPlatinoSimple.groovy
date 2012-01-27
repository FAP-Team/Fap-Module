package templates;

import es.fap.simpleled.led.FirmaPlatinoSimple
import es.fap.simpleled.led.Texto
import generator.utils.*
import generator.utils.HashStack.HashStackName

public class GFirmaPlatinoSimple {

	def FirmaPlatinoSimple firmaPlatino;
	
	public static String generate(FirmaPlatinoSimple firmaPlatino){
		GFirmaPlatinoSimple g = new GFirmaPlatinoSimple();
		g.firmaPlatino = firmaPlatino;
		g.view();
	}
	
	public String view(){
		
		CampoUtils campo = CampoUtils.create(firmaPlatino.getCampo(), "uri");
		EntidadUtils.addToIndexEntity(campo);
		
		HashStack.push(HashStackName.SAVE_EXTRA, "platino.Firma firma");
		
		TagParameters params = new TagParameters();
		if (firmaPlatino.titulo)
			params.putStr("titulo", firmaPlatino.getTitulo());
		
		params.putStr("id", firmaPlatino.name);
		params.putStr("firma", "firma.firma");
		params.put("uri", campo.firstLower());
		
		HashStack.push(HashStackName.FIRMA_BOTON, firmaPlatino.name);
		
		String view = 
		"""
#{fap.firma ${params.lista()} /}
		"""
		return view;
	}
	
}
