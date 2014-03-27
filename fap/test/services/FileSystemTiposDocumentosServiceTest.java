package services;

import java.util.ArrayList;
import java.util.List;

import models.DefinicionMetadatos;
import models.TipoDocumento;

import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

import play.test.UnitTest;
import services.filesystem.FileSystemTiposDocumentosServiceImpl;

public class FileSystemTiposDocumentosServiceTest extends UnitTest {
	
	public static String uriExpected = "fs://expectedUri";
	public static String nombreExpected = "Nombre esperado";
	public static TiposDocumentosService tiposDocumentosService;
	
	@BeforeClass 
	public static void setUp() {
		tiposDocumentosService =  new FileSystemTiposDocumentosServiceImpl();
	}
	
	@After
	public void eliminaExpected() {
		TipoDocumento t = TipoDocumento.find("byUri", uriExpected).first();
		if (t != null) {
			t.delete();
		}
		
	}
	
	
	@Test
	public void getTipoDocumentoEnBBDD() {
		String nombreExpected = "Nombre del documento";
		TipoDocumento expected = new TipoDocumento();
		expected.nombre = nombreExpected;
		expected.uri = uriExpected;
		expected.save();
		try {
			TipoDocumento actual = tiposDocumentosService.getTipoDocumento(uriExpected);
			assertThat(actual.nombre, is(equalTo(nombreExpected)));
			assertThat(actual.uri, is(equalTo(uriExpected)));
		} catch (GestorDocumentalServiceException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void getTipoDocumentoNoEnBBDD() {
		try {
			TipoDocumento actual = tiposDocumentosService.getTipoDocumento(uriExpected);
			assertThat(actual, is(nullValue()));
		} catch (GestorDocumentalServiceException e) {
			e.printStackTrace();
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void getTipoDocumentoUriNoPuedeSerNull() {
		try {
			tiposDocumentosService.getTipoDocumento(null);
		} catch (GestorDocumentalServiceException e) {
			e.printStackTrace();
		}
	}

	
	@Test(expected=NullPointerException.class)
	public void getMetadatosUriNoPuedeSerNull() {
		tiposDocumentosService.getDefinicionesMetadatos(null);
	}

	
	@Test
	public void getDefinicionesMetadatosExistentes() {
		List<DefinicionMetadatos> listaExpected = new ArrayList<DefinicionMetadatos>();
		for (int i = 0; i < 5; i++) {
			DefinicionMetadatos df = new DefinicionMetadatos();
			df.nombre = "" + i;
			df.descripcion = "Descripción de " + i;
			listaExpected.add(df);
			df.save();
		}
		TipoDocumento td = new TipoDocumento();
		td.uri = uriExpected;
		td.definicionMetadatos = listaExpected;
		td.save();
		
		List<DefinicionMetadatos> actual = tiposDocumentosService.getDefinicionesMetadatos(uriExpected);
		for(int i = 0; i < listaExpected.size();  i++) {
			assertThat(actual.get(i).nombre, is(equalTo(listaExpected.get(i).nombre)));
		}
	}
	
	@Test
	public void getDefinicionesMetadatosNoTieneMetadatos() {
		List<DefinicionMetadatos> listaExpected = new ArrayList<DefinicionMetadatos>();
		TipoDocumento td = new TipoDocumento();
		td.uri = uriExpected;
		td.definicionMetadatos = listaExpected;
		
		List<DefinicionMetadatos> actual = tiposDocumentosService.getDefinicionesMetadatos(uriExpected);
		assertThat(actual.isEmpty(), is(true));
		assertThat(actual, is(equalTo(listaExpected)));
		
	}
	
}
