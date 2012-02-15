package templates;

import es.fap.simpleled.led.Campo
import es.fap.simpleled.led.Check
import es.fap.simpleled.led.Columna
import es.fap.simpleled.led.FirmaPlatinoSimple
import es.fap.simpleled.led.Tabla
import es.fap.simpleled.led.Texto
import es.fap.simpleled.led.impl.CheckImpl;
import es.fap.simpleled.led.impl.ColumnaImpl;
import es.fap.simpleled.led.impl.TablaImpl
import generator.utils.*
import generator.utils.HashStack.HashStackName;

public class GFirmaPlatinoSimple {

	def FirmaPlatinoSimple firmaPlatino;
	
	public static String generate(FirmaPlatinoSimple firmaPlatino){
		GFirmaPlatinoSimple g = new GFirmaPlatinoSimple();
		g.firmaPlatino = firmaPlatino;
		String view = g.view();
		return view;
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
		params.put("uri", '"${'+campo.firstLower()+'}"');
		
		String view = "";
		
		// Debemos crear la tabla de firmantes en ESPERA, en caso de que sea el solicitante persona jurídica
		//CampoUtils firmantes = CampoUtils.create(firmaPlatino.getFirmantes());	
		view += crearTablaFirmantes(firmaPlatino.name, firmaPlatino.getFirmantes());
		
		HashStack.push(HashStackName.FIRMA_BOTON, firmaPlatino);
		
		view += """
#{fap.firma ${params.lista()} /}
		"""
		return view;
	}
	
	/**
	 * Crea la tabla de firmante 
	 * @param name
	 * @param campo
	 * @return
	 */
	public String crearTablaFirmantes (String name, Campo campo) {
		Tabla tabla = new TablaImpl();
		
		//tabla.setPermiso(permiso);
		tabla.setName(name+"Firmantes");
		tabla.setCampo(campo);
		tabla.setTitulo("Firmantes");
				
		Columna idValor = new ColumnaImpl();
		idValor.setCampo(CampoUtils.create("Firmante.idvalor").campo);
		idValor.setTitulo("Nº");
		idValor.setExpandir(true);
		
		Columna nombre = new ColumnaImpl();
		nombre.setCampo(CampoUtils.create("Firmante.nombre").campo);
		nombre.setTitulo("Nombre");
		nombre.setExpandir(true);
		
		Columna cardinalidad = new ColumnaImpl();
		cardinalidad.setCampo(CampoUtils.create("Firmante.cardinalidad").campo);
		cardinalidad.setTitulo("Cardinalidad");
		cardinalidad.setExpandir(true);
		
		Columna fechaFirma = new ColumnaImpl();
		fechaFirma.setCampo(CampoUtils.create("Firmante.fechaFirma").campo);
		fechaFirma.setTitulo("Firmado");
		fechaFirma.setExpandir(true);

		tabla.getColumnas().add(idValor);
		tabla.getColumnas().add(nombre);
		tabla.getColumnas().add(cardinalidad);
		tabla.getColumnas().add(fechaFirma);
		
		return Expand.expand(tabla);
	}
	
	public String controller() {
		return "// Controlador de la firma";
	}
	
}
