package model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;
import java.util.Arrays;
import java.util.List;

import models.DefinicionMetadatos;
import models.Documento;
import models.Metadato;

import models.TipoDocumento;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import static utilsTesting.Fixtures.*;

public class DefinicionMetadatosTest extends UnitTest {

    private static void vaciarBD() {
        List<Metadato> metadatos = Metadato.findAll();
        for (Metadato metadato : metadatos){
            metadato.delete();
        }
        DefinicionMetadatos.deleteAllDefiniciones();
    }

    private static void eliminaDocumento(Documento documento) {
        List<Metadato> metadatos = documento.getMetadatos();
        for(Metadato md : metadatos) {
            md.delete();
        }
        documento.delete();
    }

    @Before
    public void setUp() {
        vaciarBD();
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

    @Test
    public void guardaListasValores() {
        DefinicionMetadatos dmdExpected = getNuevaDefinicion();
        dmdExpected.save();

        DefinicionMetadatos dmdActual = DefinicionMetadatos.findById(dmdExpected.id);
        assertThat(dmdActual.nombre, is(equalTo(dmdExpected.nombre)));
        assertThat(dmdActual.valoresPosibles, hasItem("valor2"));
        assertThat(dmdActual.valoresPorDefecto, hasItem("valor1"));
        assertThat(dmdActual.valoresPorDefecto, hasItem("valor1"));

    }

    @AfterClass
    public static void limpiarTest() {
        vaciarBD();
    }
}
