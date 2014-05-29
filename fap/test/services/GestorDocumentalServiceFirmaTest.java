package services;

import es.gobcan.eadmon.aed.ws.AedExcepcion;
import models.Documento;
import models.Firma;
import models.Firmante;
import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;


public abstract class GestorDocumentalServiceFirmaTest extends UnitTest {
    protected static GestorDocumentalService gestorDocumentalService;
    protected static boolean isConfigured = false;

    protected abstract String getTipoDocumentoValido();
    protected abstract String getFirmaValida();
    protected abstract void assertFirmaGuardada(Firma firmaRecibida);
    protected abstract void assertContraFirmaGuardada(Firma firmaRecibida);
    protected abstract Firma contrafirma(Firma firmaRecibida);
    protected abstract void setPropiedadesDocumento(Documento documento, Firma firma) throws AedExcepcion;


    protected Documento stubDocumento(){
        Documento documento = new Documento();
        documento.tipo = getTipoDocumentoValido();
        documento.descripcion = "descripcion";
        documento.uri = "uri://de-prueba";
        return documento;
    }

    protected Firmante stubFirmante() {
        Firmante firmante = new Firmante();
        firmante.fechaFirma = new DateTime();
        return firmante;
    }

    protected Firma stubFirma() {
        Firma firma = new Firma(getFirmaValida(), stubFirmante());
        return firma;
    }


    @Test
    public void agregarFirmaSimple() throws Exception {
        Documento documento = stubDocumento();
        Firma firmaRecibida = stubFirma();

        setPropiedadesDocumento(documento, null);
        gestorDocumentalService.agregarFirma(documento, firmaRecibida);

        assertFirmaGuardada(firmaRecibida);
    }

    @Test
    public void agregarFirmaMultiple() throws Exception {
        Documento documento = stubDocumento();
        documento.clasificado = true;
        Firma firmaRecibida = stubFirma();
        Firma contrafirma = contrafirma(stubFirma());

        setPropiedadesDocumento(documento, firmaRecibida);
        gestorDocumentalService.agregarFirma(documento, contrafirma);

        assertContraFirmaGuardada(contrafirma);

    }
}
