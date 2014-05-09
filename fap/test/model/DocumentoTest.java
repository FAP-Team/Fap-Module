package model;

import models.DefinicionMetadatos;
import models.Documento;
import models.Metadato;
import models.TipoDocumento;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Arrays;
import java.util.List;

import static utilsTesting.Fixtures.*;

import static org.hamcrest.CoreMatchers.*;

public class DocumentoTest extends UnitTest {

    private static void vaciarBD() {
        DefinicionMetadatos.deleteAllDefiniciones();
        List<Documento> documentos = Documento.findAll();
        for (Documento documento : documentos) {
            documento.delete();
        }
        Metadato.deleteAll();
        TipoDocumento.deleteAll();
    }

    @Before
    public void setUp() {
        vaciarBD();
    }


    @Test
    public void devuelveMetadatosAsociados() {
        Documento documento = getNuevoDocumento();
        documento.save();
        DefinicionMetadatos def = getNuevaDefinicion().save();
        for (String s : Arrays.asList("valor0", "valor1", "valor2")) {
            Metadato md = new Metadato();
            md.documento = documento;
            md.save();
        }
        assertThat(documento.getMetadatos().size(), is(equalTo(3)));
        assertThat(documento.getMetadatos(1).nombre, is(equalTo("valor1")));
    }


    @AfterClass
    static public void limpiarTest() {
        vaciarBD();
    }
}
