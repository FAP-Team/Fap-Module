package templates;

import es.fap.simpleled.led.*;
import generator.utils.HashStack;
import generator.utils.HashStack.HashStackName;

public class GSubirArchivo {

	SubirArchivo subirArchivo
	
	public static String generate(SubirArchivo subirArchivo){
		def g = new GSubirArchivo();
		g.subirArchivo = subirArchivo;
		return g.view();
	}


	public String view(){
		HashStack.push(HashStackName.SAVE_EXTRA, "java.io.File ${subirArchivo.name}")
		HashStack.push(HashStackName.SUBIR_ARCHIVO, subirArchivo.name)
		return "#{fap.upload id:\"${subirArchivo.name}\" /}"
	}


}
