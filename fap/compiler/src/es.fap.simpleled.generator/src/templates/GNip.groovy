package templates;

import es.fap.simpleled.led.*
import generator.utils.*

public class GNip extends GSaveCampoElement{
	
	Nip nip;
	
	public GNip(Nip nip, GElement container){
		super(nip, container);
		this.nip = nip;
		campo = CampoUtils.create(nip.campo);
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		if(nip.name != null)
			params.putStr "id", nip.name
			
		if(nip.titulo != null)
			params.putStr("titulo", nip.titulo)
		
		if(nip.requerido)
			params.put "requerido", true
				
		params.putStr "campo", campo.firstLower();
		
		return """
			#{fap.nip ${params.lista()} /}
		""";
	}
}
