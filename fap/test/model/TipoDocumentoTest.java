package model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import models.DefinicionMetadatos;
import models.TipoDocumento;

import org.junit.Test;

import play.mvc.After;
import play.test.UnitTest;

public class TipoDocumentoTest extends UnitTest {
	
	private TipoDocumento crearDefinicionMetadatos() {
		TipoDocumento tipoDoc = new TipoDocumento();
		DefinicionMetadatos dmd = new DefinicionMetadatos();
		dmd.nombre = "Definicion1";
		tipoDoc.definicionMetadatos.add(dmd);
		dmd = new DefinicionMetadatos();
		dmd.nombre = "Definicion2";
		tipoDoc.definicionMetadatos.add(dmd);
		tipoDoc.save();
		return tipoDoc;
	}
	
	@After
	public void eliminarTiposDocumentos() {
		TipoDocumento.deleteAll();
	}
	
	@Test
	public void obtieneDefinicionExistente() {
		TipoDocumento tipoDoc = crearDefinicionMetadatos();
		assertThat(tipoDoc.getDefinicionMetadatos("Definicion1").nombre, is(equalTo("Definicion1")));
	}
	
	@Test
	public void noObtieneDefinicionNoExistente() {
		TipoDocumento tipoDoc = crearDefinicionMetadatos();
		assertThat(tipoDoc.getDefinicionMetadatos("No valido"), is(equalTo(null)));
	}
	

}
