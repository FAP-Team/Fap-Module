package model;

import models.DefinicionMetadatos;
import models.Documento;
import models.Metadato;
import org.junit.Test;
import play.test.UnitTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetadatoTest extends UnitTest {
    @Test
    public void valorCorrecto(){
        DefinicionMetadatos def = mock(DefinicionMetadatos.class);
        when(def.esValido("valor1")).thenReturn(true);
        Metadato md = new Metadato(def,"valor1",new Documento());
        assertThat(md.esValido(), is(equalTo(true)));
    }

    @Test
    public void valorNoCorrecto() {
        DefinicionMetadatos def = mock(DefinicionMetadatos.class);
        when(def.esValido("valor1")).thenReturn(false);
        Metadato md = new Metadato(def,"valor1",new Documento());
        assertThat(md.esValido(), is(equalTo(false)));
    }
}
