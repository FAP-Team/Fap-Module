package model;

import models.Firmante;
import models.Firmantes;
import models.Persona;
import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FirmantesTest extends UnitTest {

    public static final String TIPO_REPRESENTANTE = "representante";
    public static final String CARDINALIDAD_MULTIPLE = "multiple";
    public static final String CARDINALIDAD_UNICO = "unico";
    public static final String TIPO_FISICA = "fisica";

    private Firmante stubFirmante(String nombrePersona, String tipo, String cardinalidad, boolean haFirmado) {
        Persona persona = new Persona();
        Firmante f = new Firmante(persona, tipo, cardinalidad);
        f.nombre = nombrePersona;
        if (haFirmado) {
            f.fechaFirma = new DateTime();
        }
        return f;
    }

    private Firmantes stubFirmantes(List<Firmante> listaFirmantes) {
        return new Firmantes(listaFirmantes);
    }

    @Test
    public void haFirmadoPersonaFisicaUnica() {
        Firmante fisica = stubFirmante("persona1", TIPO_FISICA, CARDINALIDAD_UNICO, true);
        Firmantes firmantes = stubFirmantes(Arrays.asList(fisica));

        assertThat(firmantes.hanFirmadoTodos(), is(true));
    }

    @Test
    public void noHaFirmadoPersonaFisicaUnica() {
        Firmante fisica = stubFirmante("persona1", TIPO_FISICA, CARDINALIDAD_UNICO, false);
        Firmantes firmantes = stubFirmantes(Arrays.asList(fisica));

        assertThat(firmantes.hanFirmadoTodos(), is(false));
    }


    @Test
    public void haFirmadoRepresentantePersonaFisica() {
        Firmante fisica = stubFirmante("persona1", TIPO_FISICA, CARDINALIDAD_UNICO, false);
        Firmante representante = stubFirmante("representante1", TIPO_REPRESENTANTE, CARDINALIDAD_UNICO, true);
        Firmantes firmantes = stubFirmantes(Arrays.asList(fisica, representante));

        assertThat(firmantes.hanFirmadoTodos(), is(equalTo(true)));
    }

    @Test
    public void noHaFirmadoRepresentantePersonaFisica() {
        Firmante fisica = stubFirmante("persona1", TIPO_FISICA, CARDINALIDAD_UNICO, false);
        Firmante representante = stubFirmante("representante1", TIPO_REPRESENTANTE, CARDINALIDAD_UNICO, false);
        Firmantes firmantes = stubFirmantes(Arrays.asList(fisica, representante));

        assertThat(firmantes.hanFirmadoTodos(), is(equalTo(false)));
    }

    @Test
    public void haFirmadoRepresentantePersonaFisicaExistiendoOtrosRepresentantes() {
        Firmante fisica = stubFirmante("persona1", TIPO_FISICA, CARDINALIDAD_UNICO, false);
        Firmante representante = stubFirmante("representante1", TIPO_REPRESENTANTE, CARDINALIDAD_UNICO, true);
        Firmante representanteMancomunado = stubFirmante("representanteMancomunado", TIPO_REPRESENTANTE, CARDINALIDAD_MULTIPLE, false);
        Firmantes firmantes = stubFirmantes(Arrays.asList(fisica, representanteMancomunado, representante));

        assertThat(firmantes.hanFirmadoTodos(), is(equalTo(true)));
    }

    @Test
    public void hanFirmadoTodosLosMancomunados(){
        Firmante representante1 = stubFirmante("representante1", TIPO_REPRESENTANTE, CARDINALIDAD_MULTIPLE, true);
        Firmante representante2 = stubFirmante("representante2", TIPO_REPRESENTANTE, CARDINALIDAD_MULTIPLE, true);
        Firmante representante3 = stubFirmante("representante3", TIPO_REPRESENTANTE, CARDINALIDAD_MULTIPLE, true);
        Firmantes firmantes = stubFirmantes(Arrays.asList(representante1,representante2, representante3));

        assertThat(firmantes.hanFirmadoTodos(), is(equalTo(true)));
    }

    @Test
    public void noHanFirmadoTodosLosMancomunados() {
        Firmante representante1 = stubFirmante("representante1", TIPO_REPRESENTANTE, CARDINALIDAD_MULTIPLE, true);
        Firmante representante2 = stubFirmante("representante2", TIPO_REPRESENTANTE, CARDINALIDAD_MULTIPLE, false);
        Firmante representante3 = stubFirmante("representante3", TIPO_REPRESENTANTE, CARDINALIDAD_MULTIPLE, true);
        Firmantes firmantes = stubFirmantes(Arrays.asList(representante1,representante2, representante3));

        assertThat(firmantes.hanFirmadoTodos(), is(equalTo(false)));
    }


    @Test
    public void hanFirmadoTodosLosMancomunadosExistiendoRepresentanteSolidario(){
        Firmante representante1 = stubFirmante("representante1", TIPO_REPRESENTANTE, CARDINALIDAD_MULTIPLE, true);
        Firmante representante2 = stubFirmante("representante2", TIPO_REPRESENTANTE, CARDINALIDAD_MULTIPLE, true);
        Firmante representante3 = stubFirmante("representante3", TIPO_REPRESENTANTE, CARDINALIDAD_MULTIPLE, true);
        Firmante representanteSolidario = stubFirmante("representante1", TIPO_REPRESENTANTE, CARDINALIDAD_UNICO, false);
        Firmantes firmantes = stubFirmantes(Arrays.asList(representante1,representante2, representante3, representanteSolidario));

        assertThat(firmantes.hanFirmadoTodos(), is(equalTo(true)));
    }

}
