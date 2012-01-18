package services;

import static org.junit.Assume.assumeTrue;

import java.util.List;

import javax.inject.Inject;

import messages.Messages;
import models.TableKeyValue;
import models.Tramite;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import play.modules.guice.InjectSupport;
import play.test.Fixtures;
import play.test.UnitTest;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;
import es.gobcan.eadmon.procedimientos.ws.ProcedimientosExcepcion;
import es.gobcan.eadmon.procedimientos.ws.dominio.Procedimiento;

import static org.mockito.Mockito.*;

@InjectSupport
public class ProcedimientosServiceTest extends UnitTest {

	private static final String NOMBRE_DOCUMENTO_TEST = "nombreDocumentoTest";

	private static final String TKVT_TABLE_TIPOS_DOCUMENTOS = "tiposDocumentos";

	ProcedimientosService procedimientosService;
	
	@Inject
	static PropertyPlaceholder propertyPlaceholder;
	
	boolean hasConnecion = false;
	
	
	@Before
	public void setUp() throws Exception {
		TiposDocumentosService tiposDocumentosServiceStub = mock(TiposDocumentosService.class);
		TipoDocumento tipoDocumentoStub = new TipoDocumento(); 
		tipoDocumentoStub.setDescripcion(NOMBRE_DOCUMENTO_TEST);
		when(tiposDocumentosServiceStub.getTipoDocumento(anyString())).thenReturn(tipoDocumentoStub);
		
		procedimientosService = new ProcedimientosServiceImpl(propertyPlaceholder, tiposDocumentosServiceStub);
		hasConnecion = procedimientosService.hasConnection();
		assumeTrue(hasConnecion);
	}
	
	@Test
	public void getVersion() throws Exception {
		procedimientosService.getVersion();
	}
	
	@Ignore
	@Test
	public void obtenerDocumentosEnTramite() throws Exception {
		String procedimiento = FapProperties.get("fap.aed.procedimientos.procedimiento.uri");
		String tramiteSolicitud = "eadmon://gobcan.es/tramitesProcedimientos/TRP000000000000000012";
		List<models.TipoDocumento> documentos = procedimientosService.obtenerDocumentosEnTramite(procedimiento, tramiteSolicitud);
		assertNotNull(documentos);
		assertTrue(documentos.size() > 0);
		
		for(models.TipoDocumento documento : documentos){
			assertNotNull(documento.uri);
			assertNotNull(documento.aportadoPor);
			assertNotNull(documento.obligatoriedad);
			assertNotNull(documento.nombre);
			assertEquals(NOMBRE_DOCUMENTO_TEST, documento.nombre);
		}
		
	}
	
	@Test(expected=NullPointerException.class)
	public void obtenerDocumentosEnTramiteNull() throws Exception {
		String procedimiento = FapProperties.get("fap.aed.procedimientos.procedimiento.uri");
		procedimientosService.obtenerDocumentosEnTramite(procedimiento, null);
	}
	
	@Test(expected=NullPointerException.class)
	public void obtenerDocumentosEnTramiteProcedimientoNull() throws Exception {
		procedimientosService.obtenerDocumentosEnTramite(null, "tramite");
	}
	
	@Test
	public void obtenerTramiteActual() throws Exception {
		List<Tramite> tramites = procedimientosService.obtenerTramites();
		assertTramitesNotNull(tramites);
	}

	private void assertTramitesNotNull(List<Tramite> tramites) {
		assertNotNull(tramites);
		assertTrue(tramites.size() > 0);
		
		for(Tramite tramite : tramites){
			assertNotNull(tramite.uri);
			assertNotNull(tramite.nombre);
			assertNotNull(tramite.documentos.size() > 0);
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void obtenerTramiteNull() throws Exception {
		procedimientosService.obtenerTramites(null);
	}
	
	@Test(expected=ProcedimientosExcepcion.class)
	public void obtenerTramiteNoExistente() throws Exception {
		procedimientosService.obtenerTramites("no existe");
	}
	
	@Test
	public void actualizarTramitesFromEmpty() throws Exception {
		deleteTramitesTables();
		
		boolean result = procedimientosService.actualizarTramites();
		assertTrue(result);
		
		List<models.Tramite> tramites = models.Tramite.findAll();
		assertTramitesNotNull(tramites);
		
		List<TableKeyValue> tkv = TableKeyValue.findByTable(TKVT_TABLE_TIPOS_DOCUMENTOS);
		assertNotNull(tkv);
		assertTrue(tkv.size() > 0 );
		for(TableKeyValue entry : tkv){
			assertNotNull(entry.key);
			assertNotNull(entry.table);
			assertNotNull(entry.value);
		}
	}

	private void deleteTramitesTables() {
		Fixtures.delete(models.Tramite.class);
		Fixtures.delete(models.TipoDocumento.class);
		TableKeyValue.deleteTable(TKVT_TABLE_TIPOS_DOCUMENTOS);
	}

	@Test
	public void actualizarTramitesFromNonEmpty() {
		deleteTramitesTables();
		Tramite t = new Tramite();
		t.save();
		models.TipoDocumento td = new models.TipoDocumento();
		td.save();
		String test_key = "tipoTest";
		TableKeyValue.setValue(TKVT_TABLE_TIPOS_DOCUMENTOS, test_key, "");
		
		procedimientosService.actualizarTramites();
		
		// Comprueba que se borran los trámites antiguos, los tipos de documentos antiguos, 
		// y la tabla de tabla de tipos de documentos
		
		assertNull(Tramite.findById(t.id));
		assertNull(models.TipoDocumento.findById(td.id));
		assertNull(TableKeyValue.getValue(TKVT_TABLE_TIPOS_DOCUMENTOS, test_key));
	}
	
	@Test(expected=NullPointerException.class)
	public void actualizarTramitesProcedimientoNull(){
		procedimientosService.actualizarTramites(null);
	}
	
	@Test
	public void actualizarTramitesProcedimientoNoExistente(){
		Messages.clear();
		boolean result = procedimientosService.actualizarTramites("http://no/existe");
		assertFalse(result);
		assertTrue(Messages.hasErrors());
		play.Logger.info(Messages.allMessages());
	}
	
	@Test
	public void getProcedimientos(){
		List<Procedimiento> procedimientos = procedimientosService.getProcedimientos();
		assertNotNull(procedimientos);
		assertTrue(procedimientos.size() > 0);
	}
}
