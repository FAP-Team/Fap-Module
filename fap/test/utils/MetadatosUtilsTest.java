package utils;

import java.util.Arrays;
import java.util.List;

import models.DefinicionMetadatos;
import models.Metadato;
import models.TipoDocumento;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import play.data.binding.Binder;
import play.test.UnitTest;
import services.TiposDocumentosService;
import services.aed.AedTiposDocumentosServiceImpl;
import static org.hamcrest.CoreMatchers.*;

import static org.mockito.Mockito.*;

public class MetadatosUtilsTest extends UnitTest {
	
	@Before
	public void vaciarBD() {
		Metadato.deleteAll();
		DefinicionMetadatos.deleteAllDefiniciones();
		TipoDocumento.deleteAll();
	}
	
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
		tipoDoc = TipoDocumento.find("byUri",tipoDoc.uri).first();
		assertThat(tipoDoc.getDefinicionMetadatos("Metadato1").valoresPorDefecto, is(not(equalTo(null))));
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

	
	@Test
	public void cargaDefinicionesCorrectamente() {
		TipoDocumento td1 = new TipoDocumento();
		td1.uri = "uri://tipoDoc1";
		TipoDocumento td2 = new TipoDocumento();
		td2.uri = "uri://tipoDoc2";
		
		DefinicionMetadatos def1 = new DefinicionMetadatos();
		def1.nombre = "definicion1";
		DefinicionMetadatos def2 = new DefinicionMetadatos();
		def2.nombre = "definicion2";
		DefinicionMetadatos def3 = new DefinicionMetadatos();
		def3.nombre = "definicion3";
		
		td1.save();
		td2.save();
		
		TiposDocumentosService tds = mock(TiposDocumentosService.class);
		MetadatosUtils.tiposDocumentosService = tds;
		when(tds.getDefinicionesMetadatos(td1.uri)).thenReturn(Arrays.asList(def1,def2));
		when(tds.getDefinicionesMetadatos(td2.uri)).thenReturn(Arrays.asList(def3));
		List<String> uris = Arrays.asList(td1.uri, td2.uri);
		
		MetadatosUtils.cargarDefinicionesMetadatosPorUri(uris);
		td1 = TipoDocumento.find("byUri", td1.uri).first();
		td2 = TipoDocumento.find("byUri", td2.uri).first();
		
		assertThat(td1.definicionMetadatos.size(), is(equalTo(2)));
		assertThat(td2.definicionMetadatos.size(), is(equalTo(1)));
		assertThat(td1.definicionMetadatos.get(0).nombre, is(equalTo("definicion1")));
		assertThat(td1.definicionMetadatos.get(1).nombre, is(equalTo("definicion2")));
		assertThat(td2.definicionMetadatos.get(0).nombre, is(equalTo("definicion3")));
	}
	
	@Test
	public void noCargaDefinicionesUriIncorrecta() {
        TipoDocumento tipo = inicializaDefinicionesMetadatos();
        TiposDocumentosService tds = mock(TiposDocumentosService.class);
        MetadatosUtils.tiposDocumentosService = tds;
        List<String> uris = Arrays.asList("uri://no_existente");
        try {
            MetadatosUtils.cargarDefinicionesMetadatosPorUri(uris);
            fail("Debería saltar una excepción");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(equalTo("Tipo de documento " + "uri://no_existente" + " no encontrado")));
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
