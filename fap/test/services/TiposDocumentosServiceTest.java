package services;

import javax.inject.Inject;
import javax.naming.ldap.HasControls;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.TiposDocumentosExcepcion;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;
import es.gobcan.platino.servicios.registro.Asunto;

import play.modules.guice.InjectSupport;
import play.test.UnitTest;
import properties.PropertyPlaceholder;
import static org.junit.Assume.*;
import static org.mockito.Mockito.*;
@InjectSupport
public class TiposDocumentosServiceTest extends UnitTest {

	@Inject
	static TiposDocumentosService tiposDocumentosService;
	
	@Inject
	static PropertyPlaceholder propertyPlaceholder;
	
	static boolean hasConnecion = false;
		
	@BeforeClass
	public static void init(){
		hasConnecion = tiposDocumentosService.hasConnection();
	}
	
	@Before
	public void before(){
		assumeTrue(hasConnecion);
	}
	
	@Test
	public void getVersion() throws Exception {
		tiposDocumentosService.getVersion();
	}
	
	@Test
	public void getTipoDocumentoBase() throws Exception {		
		String uriDocumentoBase = propertyPlaceholder.get("fap.aed.tiposdocumentos.base");
		TipoDocumento documentoBase = tiposDocumentosService.getTipoDocumento(uriDocumentoBase);
		assertNotNull(documentoBase);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void getTipoDocumentoNull() throws Exception {
		tiposDocumentosService.getTipoDocumento(null);
	}
	
	@Test(expected=TiposDocumentosExcepcion.class)
	public void getTipoDocumentoNoExistente() throws Exception{
		tiposDocumentosService.getTipoDocumento("http://not/correct");
	}
	
}
