package services;

import static org.junit.Assume.assumeTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import models.Documento;
import models.ExpedienteAed;
import models.Persona;
import models.RepresentantePersonaJuridica;
import models.Solicitante;
import models.SolicitudGenerica;
import models.TableKeyValue;
import models.Tramite;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import play.Play;
import play.libs.Codec;
import play.libs.IO;
import play.modules.guice.InjectSupport;
import play.test.Fixtures;
import play.test.UnitTest;
import properties.FapProperties;
import properties.MapPropertyPlaceholder;
import properties.PropertyPlaceholder;
import services.aed.AedGestorDocumentalServiceImpl;
import utils.BinaryResponse;
import utils.StringUtils;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.excepciones.CodigoErrorEnum;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;

public class AedGestorDocumentalServiceTest extends GestorDocumentalServiceTest {

    private static PropertyPlaceholder propertyPlaceholder;

    @BeforeClass
    public static void configure() throws Exception {
        propertyPlaceholder = getPropertyPlaceholder();
        gestorDocumentalService = new AedGestorDocumentalServiceImpl(propertyPlaceholder);
        isConfigured = gestorDocumentalService.isConfigured();
    }

    private static PropertyPlaceholder getPropertyPlaceholder() {
        InputStream is = AedGestorDocumentalServiceTest.class.getClassLoader().getResourceAsStream(
                "services/aed.properties");
        return MapPropertyPlaceholder.load(is);
    }

    @Override
    protected String getTipoDocumentoValido() {
        return propertyPlaceholder.get("fap.aed.tiposdocumentos.base");
    }
    
}
