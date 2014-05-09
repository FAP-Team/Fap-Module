package model;

import models.DefinicionMetadatos;
import models.Documento;
import models.Metadato;
import models.TipoDocumento;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Arrays;
import java.util.List;

import static utilsTesting.Fixtures.*;

import static org.hamcrest.CoreMatchers.*;

public class DocumentoTest extends UnitTest {

    public Documento documento;

    private static void vaciarBD() {
        DefinicionMetadatos.deleteAllDefiniciones();
        Metadato.deleteAll();
        TipoDocumento.deleteAll();
    }

    @Before
    public void setUp() {
        vaciarBD();
    }

    @After
    public void tearDown(){
        vaciarBD();
        documento.delete();
    }


    @Test
    public void devuelveMetadatosAsociados() {
        documento = getNuevoDocumento();
        documento.save();
        DefinicionMetadatos def = getNuevaDefinicion().save();
        for (String valor : Arrays.asList("valor0", "valor1", "valor2")) {
            Metadato md = new Metadato(def,valor,documento).save();
        }

        assertThat(documento.getMetadatos().size(), is(equalTo(3)));
        assertThat(documento.getMetadatos(1).valor, is(equalTo("valor1")));
        for(Metadato md : documento.getMetadatos()) {
            md.delete();
        }
    }

    @Test
    public void rellenaMetadatosAutomaticamente() {
        TipoDocumento tipo = getNuevoTipoDocumento();
        DefinicionMetadatos definicionMetadatos = getNuevaDefinicion();
        definicionMetadatos.save();
        tipo.definicionMetadatos.add(definicionMetadatos);
        tipo.save();
        assertThat(tipo.definicionMetadatos.get(0), is(not(equalTo(null))));
        assertThat(tipo.definicionMetadatos.get(0).nombre, is(equalTo(DEF_METADATOS_NOMBRE)));

        documento = getNuevoDocumento();
        documento.tipo = tipo.uri;
        documento.save();
        documento.rellenarMetadatosAutomaticos();


        assertThat(documento.getMetadatos(), is(notNullValue()));
        assertThat(documento.getMetadatos().size(), is(not(equalTo(0))));
        assertThat(documento.getMetadatos().get(0).nombre, is(equalTo(tipo.definicionMetadatos.get(0).nombre)));

    }

}
