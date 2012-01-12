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
	
	private static Logger logger = Logger.getLogger(TiposDocumentosServiceTest.class); 
	
	@BeforeClass
	public static void init(){
		try {
			hasConnecion = tiposDocumentosService.getVersion() != null;
		}catch(Exception e){
			logger.warn("No hay conexión a "+ tiposDocumentosService.getEndPoint() + " , saltando los tests de AedServiceTest");
		}
	}
	
	@Test
	public void getVersion() throws Exception {
		assumeTrue(hasConnecion);
		tiposDocumentosService.getVersion();
	}
	
	@Test
	public void getTipoDocumentoBase() throws Exception {		
		assumeTrue(hasConnecion);
		String uriDocumentoBase = propertyPlaceholder.get("fap.aed.tiposdocumentos.base");
		TipoDocumento documentoBase = tiposDocumentosService.getTipoDocumento(uriDocumentoBase);
		assertNotNull(documentoBase);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void getTipoDocumentoNull() throws Exception {
		assumeTrue(hasConnecion);
		tiposDocumentosService.getTipoDocumento(null);
	}
	
	@Test(expected=TiposDocumentosExcepcion.class)
	public void getTipoDocumentoNoExistente() throws Exception{
		assumeTrue(hasConnecion);
		tiposDocumentosService.getTipoDocumento("http://not/correct");
	}
	
}
