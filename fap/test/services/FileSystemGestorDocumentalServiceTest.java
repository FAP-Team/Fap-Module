package services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import models.Documento;
import models.ExpedienteAed;
import models.SolicitudGenerica;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.libs.IO;
import play.test.UnitTest;
import properties.FapProperties;
import properties.MapPropertyPlaceholder;
import properties.PropertyPlaceholder;
import services.filesystem.FileSystemGestorDocumentalServiceImpl;
import utils.BinaryResponse;

public class FileSystemGestorDocumentalServiceTest extends GestorDocumentalServiceTest {

    static File base;

    private static String uriOtros = FapProperties.get("fap.aed.tiposdocumentos.otros");

    @BeforeClass
    public static void setup() throws GestorDocumentalServiceException {
        gestorDocumentalService = new FileSystemGestorDocumentalServiceImpl(getProperties());
        gestorDocumentalService.configure();
        isConfigured = gestorDocumentalService.isConfigured();
    }

    private static PropertyPlaceholder getProperties() {
        PropertyPlaceholder propertyPlaceholder = new MapPropertyPlaceholder("fap.aed.tiposdocumentos.otros", uriOtros,
                "fap.fs.gestorDocumental.path", "/tmp/test/gestorDocumental");
        return propertyPlaceholder;
    }

    @Override
    protected String getTipoDocumentoValido() {
        return "fs://tipo1";
    }

    @Override
    protected String getTipoDocumentoOtros() {
        return uriOtros;
    }

}
