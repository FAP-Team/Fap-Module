package utils;

import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.*;

import play.test.UnitTest;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;

public class TiposDocumentosWSUtilsTest extends UnitTest{
	
	static String nombreExpected = "Descripción o nombre";
	static String uriExpected = "fs://uriExpected";
	
	@Test
	public void convertTipoAed2TipoFapCorrecto() {
		TipoDocumento tipoAed = new TipoDocumento();
		tipoAed.setDescripcion(nombreExpected);
		models.TipoDocumento tipoFap = 
				TiposDocumentosWSUtils.convertTipoAed2TipoFap(tipoAed);
		assertThat(tipoFap.nombre, is(equalTo((nombreExpected))));
	}
	
	@Test
	public void convertTipoAed2TipoFapCorrectoEnBBDD() {
		TipoDocumento tipoAed = new TipoDocumento();
		models.TipoDocumento tipoExpected = new models.TipoDocumento();
		tipoExpected.uri = uriExpected;
		tipoExpected.nombre = nombreExpected;
		tipoExpected.save();
		
		tipoAed.setUri(uriExpected);
		models.TipoDocumento tipoFap = 
				TiposDocumentosWSUtils.convertTipoAed2TipoFap(tipoAed);
		
		assertThat(tipoFap.nombre, is(equalTo(nombreExpected)));
		assertThat(tipoFap.uri, is(equalTo(uriExpected)));
	} 
	
	@Test
	public void convertTipoFap2TipoAedCorrecto(){
		models.TipoDocumento tipoFap = new models.TipoDocumento();
		tipoFap.nombre = nombreExpected;
		tipoFap.uri = uriExpected;
		
		TipoDocumento tipoAed = 
				TiposDocumentosWSUtils.convertTipoFap2Aed(tipoFap);
		
		assertThat(tipoAed.getDescripcion(), is(equalTo(nombreExpected)));
		assertThat(tipoAed.getUri(), is(equalTo(uriExpected)));
	}

}
