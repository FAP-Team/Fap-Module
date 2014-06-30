package controllers;

import models.DocumentoExterno;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.test.UnitTest;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentosExternosFAPControllerTest extends UnitTest {

    private static final String HASH_VALIDO = "Hash_de_documento_valido";
    private static final String URI_VALIDA = "uri://de-documento-valida";
    private static final String URI_NO_VALIDA = "uri://no-valida-o-sin-documento";

    private static DocumentoExterno mockDocumentoExterno() {
        DocumentoExterno documentoExterno;
        documentoExterno = new DocumentoExterno();
        documentoExterno.organo = "OrganoMock";
        documentoExterno.expediente = "ExpedienteMock";
        documentoExterno.tipo = "TipoDocumentoMock";
        documentoExterno.uri = URI_VALIDA;
        return documentoExterno;
    }

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private static GestorDocumentalService mockGestorDocumentalService;

    @Test
    public void documentoExternoGuardaHashSiUriCorrecta () {
        DocumentoExterno docExternoMock = mockDocumentoExterno();
        assertThat(docExternoMock.hash, is(equalTo(null)));
        setMockGDServiceDevuelveHashNull();
        String accion = "editar";
        DocumentoExterno dbDocumentoExterno = new DocumentoExterno();

        DocumentosExternosFAPController.DocumentosExternosFAPValidateCopy(accion, dbDocumentoExterno, docExternoMock);

        assertThat(dbDocumentoExterno.hash, is(equalTo(HASH_VALIDO)));
    }


    @Test
    public void documentoExternoNoGuardaHashSiUriIncorrecta() {
        DocumentoExterno docExternoMock = mockDocumentoExterno();
        docExternoMock.uri = URI_NO_VALIDA;
        assertThat(docExternoMock.hash, is(equalTo(null)));
        setMockGDServiceDevuelveHashNoValido();
        String accion = "editar";
        DocumentoExterno dbDocumentoExterno = new DocumentoExterno();

        DocumentosExternosFAPController.DocumentosExternosFAPValidateCopy(accion, dbDocumentoExterno, docExternoMock);

        assertThat(dbDocumentoExterno.hash, is(equalTo(null)));
    }

    @Test
    public void documentoExternoNoGuardaHashSiNoRecibeUri() {
        DocumentoExterno docExternoMock = mockDocumentoExterno();
        docExternoMock.uri = null;
        assertThat(docExternoMock.hash, is(equalTo(null)));
        setMockGDServiceDevuelveHashNoValido();
        String accion = "editar";
        DocumentoExterno dbDocumentoExterno = new DocumentoExterno();

        DocumentosExternosFAPController.DocumentosExternosFAPValidateCopy(accion, dbDocumentoExterno, docExternoMock);

        assertThat(dbDocumentoExterno.hash, is(equalTo(null)));
    }

    private void setMockGDServiceDevuelveHashNoValido() {
        try {
            when(mockGestorDocumentalService
                    .getDocumentoByUri(URI_NO_VALIDA).getPropiedades().getSellado().getHash())
                    .thenReturn(null);
        } catch (GestorDocumentalServiceException e) {
            e.printStackTrace();
        }
    }

    private void setMockGDServiceDevuelveHashNull() {
        try {
            when(mockGestorDocumentalService
                    .getDocumentoByUri(URI_VALIDA).getPropiedades().getSellado().getHash())
                    .thenReturn(HASH_VALIDO);
        } catch (GestorDocumentalServiceException e) {
            e.printStackTrace();
        }
        DocumentosExternosFAPController.gestorDocumentalService =
                mockGestorDocumentalService;
    }
}
