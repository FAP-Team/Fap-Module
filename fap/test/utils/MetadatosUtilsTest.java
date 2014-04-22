package utils;

import java.util.Arrays;

import models.DefinicionMetadatos;
import models.TipoDocumento;

import org.junit.Test;

import play.test.UnitTest;
import static org.hamcrest.CoreMatchers.*;

public class MetadatosUtilsTest extends UnitTest {
	
	
	private TipoDocumento inicializaDefinicionesMetadatos() {
		TipoDocumento tipo = new TipoDocumento();
		tipo.uri = "uri://deprueba";
		DefinicionMetadatos dmd = new DefinicionMetadatos();
		dmd.nombre = "Metadato1";
		dmd.descripcion = "Descripcion metadato1";
		dmd.valoresPosibles = Arrays.asList("valor1", "valor2", "valor3", "valor4");
		dmd.save();
		tipo.definicionMetadatos.add(dmd);
		dmd = new DefinicionMetadatos();
		dmd.nombre = "Metadato2";
		dmd.descripcion = "Descripcion metadato2";
		dmd.valoresPosibles = Arrays.asList("valor21", "valor22", "valor23", "valor24");
		dmd.save();
		tipo.definicionMetadatos.add(dmd);
		tipo.save();
		return tipo;
	}
	

	
	@Test
	public void jsonCorrecto() {
		String nombreMD1 = "Metadato1";
		String nombreMD2 = "Metadato2";
		String valorMD1 = "valor1";
		String valorMD2 = "valor2";
		String valorMD3 = "valor21";
		String valorMD4 = "valor22";
		String json = generaStringJson(nombreMD1, nombreMD2, valorMD1, valorMD2, valorMD3, valorMD4);
		TipoDocumento tipoDoc = inicializaDefinicionesMetadatos();
		MetadatosUtils.cargarStringJsonMetadatosTipoDocumento(json);
		tipoDoc.refresh();
		assertThat(tipoDoc.getDefinicionMetadatos("Metadato1").valoresPorDefecto.contains("valor1"), is(true));
		assertThat(tipoDoc.getDefinicionMetadatos("Metadato1").valoresPorDefecto.contains("valor2"), is(true));
		assertThat(tipoDoc.getDefinicionMetadatos("Metadato2").valoresPorDefecto.contains("valor21"), is(true));
		assertThat(tipoDoc.getDefinicionMetadatos("Metadato2").valoresPorDefecto.contains("valor22"), is(true));
	}
	
	@Test
	public void jsonConTipoDocumentoNoValido() {
		String json = "{\"metadatos documento\":[" +
			    "{" +
		        "\"tipoDocumento\": \"fs://uriNoValida\"}]}";
		inicializaDefinicionesMetadatos();
		try {
			MetadatosUtils.cargarStringJsonMetadatosTipoDocumento(json);
			fail("No saltó la exepción esperada");
		} catch(NullPointerException e) {
			assertThat(e.getMessage(), is(equalTo("Tipo de documento no existente")));
		}
	}
	
	@Test
	public void jsonConDefinicionMetadatoNoValida() {
		String nombreMD1 = "Metadato1";
		String nombreMD2 = "Metadato2-NoValido";
		String valorMD1 = "valor1";
		String valorMD2 = "valor2";
		String valorMD3 = "valor21";
		String valorMD4 = "valor22";
		String json = generaStringJson(nombreMD1, nombreMD2, valorMD1, valorMD2, valorMD3, valorMD4);
		TipoDocumento tipoDoc = inicializaDefinicionesMetadatos();
		try {
			MetadatosUtils.cargarStringJsonMetadatosTipoDocumento(json);
			fail("No saltó la excepción");
		}catch (IllegalArgumentException e){
			assertThat(e.getMessage(), is(equalTo(String.format(
					"Definición de metadato '%s' no encontrada para el tipo de documento '%s'",
					nombreMD2, tipoDoc.uri))));
		}

	}
	
	@Test
	public void jsonConValorNoValido(){
		String nombreMD1 = "Metadato1";
		String nombreMD2 = "Metadato2";
		String valorMD1 = "valor1";
		String valorMD2 = "valor2";
		String valorMD3 = "valor21-noValido";
		String valorMD4 = "valor22";
		String json = generaStringJson(nombreMD1, nombreMD2, valorMD1, valorMD2, valorMD3, valorMD4);
		TipoDocumento tipoDoc = inicializaDefinicionesMetadatos();
		try {
			MetadatosUtils.cargarStringJsonMetadatosTipoDocumento(json);
			fail("No saltó la excepción");
		}catch (IllegalArgumentException e){
			assertThat(e.getMessage(), is(equalTo(String.format(
					"Valor '%s' de metadato '%s' no válido",
					valorMD3, nombreMD2))));
		}
	}

	
	
	private String generaStringJson(String nombreMD1, String nombreMD2, 
									String valorMD11, String valorMD12, 
									String valorMD21, String valorMD22) {
		return String.format("{\"metadatos documento\":[" +
			    "{" +
		        "\"tipoDocumento\": \"uri://deprueba\"," +
		        "\"listaMetadatos\": ["+
		                            "{" +
		                                "\"nombre\": \"%s\","+
		                                "\"valor\":  \"%s\"" +
		                            "},"+
		                            "{"+
		                                "\"nombre\": \"%s\","+
		                                "\"valor\":  \"%s\""+
		                            "},"+
		                            "{"+
		                                "\"nombre\": \"%s\","+
		                                "\"valor\":  \"%s\""+
		                            "},"+
		                            "{"+
		                                "\"nombre\": \"%s\","+
		                                "\"valor\":  \"%s\""+
		                            "} ] } ]}",
				nombreMD1,
				valorMD11,
				nombreMD1,
				valorMD12,
				nombreMD2,
				valorMD21,
				nombreMD2,
				valorMD22);
	}
}
