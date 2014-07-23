package templates;

import es.fap.simpleled.led.Campo
import es.fap.simpleled.led.Check
import es.fap.simpleled.led.Columna
import es.fap.simpleled.led.FirmaSimple
import es.fap.simpleled.led.FirmaSetCampo
import es.fap.simpleled.led.LedFactory
import es.fap.simpleled.led.Tabla
import es.fap.simpleled.led.Texto
import es.fap.simpleled.led.impl.CheckImpl;
import es.fap.simpleled.led.impl.ColumnaImpl;
import es.fap.simpleled.led.impl.TablaImpl
import generator.utils.*

public class GFirmaSimple extends GElement{

	FirmaSimple firmaSimple;
	
	public GFirmaSimple(FirmaSimple firmaSimple, GElement container){
		super(firmaSimple, container);
		this.firmaSimple = firmaSimple;
	}
	
	public String view(){
		CampoUtils documentoUri = CampoUtils.create(firmaSimple.documento.campo, "uri");
		CampoUtils documentoFirma = CampoUtils.create(firmaSimple.documento.campo);
		TagParameters params = new TagParameters();
		if (firmaSimple.titulo)
			params.putStr("titulo", firmaSimple.titulo);
		params.putStr("id", firmaSimple.name);
		params.putStr("firma", "firma");
		params.put("uri", '"${' + documentoUri.firstLower() + '}"');
        params.put("firmaActual", '"${' + documentoFirma.firstLower() + '.getFirma()' + '}"')
		return """
			#{fap.firma ${params.lista()} /}
		""";
	}
	
	public void generate(){
		Tabla tabla = LedFactory.eINSTANCE.createTabla();
		tabla.setName(firmaSimple.name + "Firmantes");
		tabla.getMetaClass().setAttribute(tabla, "campo", firmaSimple.firmantes.campo);
		tabla.setTitulo("Interesados");
				
		Columna idValor = LedFactory.eINSTANCE.createColumna();
		idValor.setCampo(CampoUtils.create("Firmante.idvalor").campo);
		idValor.setTitulo("Nº");
		idValor.setExpandir(true);
		
		Columna nombre = LedFactory.eINSTANCE.createColumna();
		nombre.setCampo(CampoUtils.create("Firmante.nombre").campo);
		nombre.setTitulo("Nombre");
		nombre.setExpandir(true);
		
		Columna cardinalidad = LedFactory.eINSTANCE.createColumna();
		cardinalidad.setCampo(CampoUtils.create("Firmante.cardinalidad").campo);
		cardinalidad.setTitulo("Cardinalidad");
		cardinalidad.setExpandir(true);
		
		Columna fechaFirma = LedFactory.eINSTANCE.createColumna();
		fechaFirma.setCampo(CampoUtils.create("Firmante.fechaFirma").campo);
		fechaFirma.setTitulo("Firmado");
		fechaFirma.setExpandir(true);

		tabla.getColumnas().add(idValor);
		tabla.getColumnas().add(nombre);
		tabla.getColumnas().add(cardinalidad);
		tabla.getColumnas().add(fechaFirma);

		getGroupContainer().addElementBefore(tabla, firmaSimple);
	}
	
	public List<String> extraParams(){
		List<String> extraParams = super.extraParams();
		extraParams.add("String firma");
		return extraParams;
	}

	public Set<Entidad> dbEntities(){
		Set<Entidad> entidades = new HashSet<Entidad>();
		entidades.add(Entidad.create(firmaSimple.documento.campo.entidad));
		entidades.add(Entidad.create(firmaSimple.firmantes.campo.entidad));
		if (!firmaSimple.calcularFirmantes.campo.method)
			entidades.add(Entidad.create(firmaSimple.calcularFirmantes.campo.entidad));
		if (firmaSimple.setToTrue)
			entidades.add(Entidad.create(firmaSimple.setToTrue.campo.entidad));
		for (FirmaSetCampo setCampo: firmaSimple.setCampos){
			CampoUtils campoSetTo = CampoUtils.create(setCampo.campo);
			entidades.add(Entidad.create(setCampo.campo.entidad));
		}
		return entidades;
	}
	
	public String controller() {
		Controller controller = Controller.create(getControllerContainer());
		String botonesMethod = "";
		CampoUtils documento = CampoUtils.create(firmaSimple.documento.campo);
		Entidad entidadDoc = documento.entidad;
		CampoUtils firmantes = CampoUtils.create(firmaSimple.firmantes.campo);
		CampoUtils registroFirma = null;
		if (firmaSimple.registroFirma)
			registroFirma = CampoUtils.create(firmaSimple.registroFirma.campo);
		String strRegistroFirma = "";
		if (registroFirma != null)
			strRegistroFirma = """${registroFirma.firstLower()}.fasesRegistro.firmada = true;
							 ${registroFirma.firstLower()}.save();
						  """
		else
			strRegistroFirma = "${entidadDoc.variable}.registro.fasesRegistro.firmada = true;"
		String strCampoFirmantes = "${firmantes.firstLower()}";
		String strCampoToTrue = "";
		if (firmaSimple.setToTrue) {
			CampoUtils campoToTrue = CampoUtils.create(firmaSimple.setToTrue.campo);
			strCampoToTrue = """
				${campoToTrue.firstLower()} = true;
				${campoToTrue.sinUltimoAtributo()}.save();
			""";
		}
		String strCampoSetTo = "";
		for (FirmaSetCampo setCampo: firmaSimple.setCampos){
			CampoUtils campoSetTo = CampoUtils.create(setCampo.campo);
			strCampoSetTo += """
				${campoSetTo.firstLower()} = "${setCampo.value}";
				${campoSetTo.sinUltimoAtributo()}.save();
			""";
		}
		String getters = "";
		for (Entidad e: dbEntities())
				getters += """${e.clase} ${e.variable} = ${controller.complexGetterCall(e)};\n""";
		String calcularFirmantes;
		if (firmaSimple.calcularFirmantes.campo.method)
			calcularFirmantes = firmaSimple.calcularFirmantes.campo.method;
		else
			calcularFirmantes = """${CampoUtils.create(firmaSimple.calcularFirmantes.campo).firstLower()}.calcularFirmantes()""";
		return """
			@Util
			public static void ${StringUtils.firstLower(firmaSimple.name)}${controller.sufijoBoton}(${StringUtils.params(
				controller.allEntities.collect{it.typeId}, controller.extraParams
			)}){
				${getters}
				play.Logger.info("Metodo: ${StringUtils.firstLower(firmaSimple.name)}${controller.sufijoBoton}");
				Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams"); 
				Map<String, Object> vars = new HashMap<String, Object>(); 
				if (secure.checkAcceso("editarFirma", "editar", ids, vars)) { 
					if (${firmantes.firstLower()} == null || ${firmantes.firstLower()}.size() == 0){
						${firmantes.firstLower()} = ${calcularFirmantes};
						${firmantes.sinUltimoAtributo()}.save();
					}
					FirmaUtils.firmar(${documento.firstLower()}, ${firmantes.firstLower()}, firma, null);
				}
				else{
				//ERROR
					Messages.error("No tiene permisos suficientes para realizar la acción++");
				}
				if (!Messages.hasErrors()) {
					${strCampoToTrue}
					${strCampoSetTo}
					${strRegistroFirma}
					${entidadDoc.variable}.save();
				}
			}
		""";
	}
		
}
