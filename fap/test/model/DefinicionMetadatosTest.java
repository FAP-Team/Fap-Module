package model;

import static org.hamcrest.CoreMatchers.is;

import java.util.Arrays;
import java.util.List;

import models.DefinicionMetadatos;

import org.junit.Test;

import play.test.UnitTest;

public class DefinicionMetadatosTest extends UnitTest {
	
	@Test
	public void valorEsValido() {
		DefinicionMetadatos dmd = new DefinicionMetadatos();
		List<String> valoresPosibles = Arrays.asList("valor1","valor2","valor3","valor4");
		dmd.valoresPosibles = valoresPosibles;
		assertThat(dmd.esValido("valor3"), is(true));
	}
	
	@Test
	public void valorNoValido() {
		DefinicionMetadatos dmd = new DefinicionMetadatos();
		List<String> valoresPosibles = Arrays.asList("valor1","valor2","valor3","valor4");
		dmd.valoresPosibles = valoresPosibles;
		assertThat(dmd.esValido("valor999"), is(false));
	}

}
