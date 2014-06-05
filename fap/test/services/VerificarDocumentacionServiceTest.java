package services;

import models.Documento;
import models.Firmantes;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;
import properties.FapProperties;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerificarDocumentacionServiceTest extends UnitTest {

    private Documento nuevoDocumento() {
        Documento doc = new Documento();
        doc.uri = "uri://deprueba";
        doc.firmantes = new Firmantes();
        return doc;
    }

    private boolean comprobarAnexosActivado() {
        return FapProperties.getBoolean("fap.documentacion.comprobarAnexosFirmados");
    }

    @Test
    public void compruebaLasFirmasCorrectas() {
        assumeTrue(comprobarAnexosActivado());
        Documento doc = nuevoDocumento();
        Firmantes firmantes = mock(Firmantes.class);
        when(firmantes.hanFirmadoTodos()).thenReturn(true);
        doc.firmantes = firmantes;

        boolean resultado = VerificarDocumentacionService.comprobarFirma(doc);

        assertThat(resultado, is(equalTo(true)));
    }

    @Test
    public void comprobarFirmasDevuelveFalseSiNoFirmado() {
        assumeTrue(comprobarAnexosActivado());

        Documento doc = nuevoDocumento();
        doc.firmantes = null;

        boolean resultado = VerificarDocumentacionService.comprobarFirma(doc);

        assertThat(resultado, is(equalTo(false)));
    }

    @Test
    public void comprobarFirmasDevuelveFalseSiNoTodosHanFirmado(){
        assumeTrue(comprobarAnexosActivado());

        Documento doc = nuevoDocumento();
        Firmantes firmantes = mock(Firmantes.class);
        when(firmantes.hanFirmadoTodos()).thenReturn(false);
        doc.firmantes = firmantes;

        boolean resultado = VerificarDocumentacionService.comprobarFirma(doc);

        assertThat(resultado, is(equalTo(false)));
    }


}
