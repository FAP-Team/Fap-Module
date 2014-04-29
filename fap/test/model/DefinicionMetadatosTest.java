package model;

import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;
import java.util.List;

import models.DefinicionMetadatos;
import models.Metadato;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;

public class DefinicionMetadatosTest extends UnitTest {

    @Before
    public void vaciarBD() {
        DefinicionMetadatos.deleteAllDefiniciones();
    }

	public DefinicionMetadatos getNuevaDefinicion() {
		DefinicionMetadatos dmd = new DefinicionMetadatos();
		List<String> valoresPosibles = Arrays.asList("valor1","valor2","valor3","valor4");
		dmd.valoresPosibles = valoresPosibles;
		return dmd;
	}
	
	@Test
	public void valorEsValido() {
		DefinicionMetadatos dmd = getNuevaDefinicion();
		assertThat(dmd.esValido("valor3"), is(true));
	}
	
	@Test
	public void valorNoValido() {
		DefinicionMetadatos dmd = getNuevaDefinicion();
		assertThat(dmd.esValido("valor999"), is(false));
	}
	
	@Test
	public void eliminaLasDefiniciones() {
		DefinicionMetadatos dmd = getNuevaDefinicion();
		dmd.save();
		dmd = getNuevaDefinicion();
		dmd.id = null;
		dmd.save();
		List<DefinicionMetadatos> definiciones = DefinicionMetadatos.findAll();
		assertThat(definiciones.size(), is(equalTo(2)));
		DefinicionMetadatos.deleteAllDefiniciones();
		definiciones = DefinicionMetadatos.findAll();
		assertThat(definiciones.size(), is(equalTo(0)));
	}

}
