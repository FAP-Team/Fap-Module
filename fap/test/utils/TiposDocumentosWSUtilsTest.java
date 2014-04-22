package utils;

import models.DefinicionMetadatos;

import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.*;

import play.test.UnitTest;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.DefinicionMetadato;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.ListaDefinicionMetadato;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.ListaValorPosible;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;

public class TiposDocumentosWSUtilsTest extends UnitTest{
	
	static String nombreExpected = "Descripción o nombre";
	static String uriExpected = "fs://uriExpected";
	
	@Test
	public void convertTipoAed2TipoFapCorrecto() {
		TipoDocumento tipoAed = new TipoDocumento();
		tipoAed.setDescripcion(nombreExpected);
		tipoAed.setDefinicionesMetadatos(getNuevaListaDefinicionMetadato());
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
		tipoAed.setDefinicionesMetadatos(getNuevaListaDefinicionMetadato());
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

	@Test
	public void convertirDefinicionAed2FapCorrecta() {
		DefinicionMetadato defAed = new DefinicionMetadato();
		defAed.setDescripcion("Descripción");
		defAed.setAutoGenerado(true);
		defAed.setIdentificador("Nombre");
		defAed.setValoresPosibles(getNuevaListaValorPosible());
		
		DefinicionMetadatos defFap = TiposDocumentosWSUtils.convertDefinicionAed2Fap(defAed);
		
		assertThat(defFap.descripcion, is(equalTo(defAed.getDescripcion())));
		assertThat(defFap.autogenerado, is(equalTo(defAed.isAutoGenerado())));
		assertThat(defFap.nombre, is(equalTo(defAed.getIdentificador())));
		
		assertThat(defFap.valoresPosibles.size(), is(equalTo(2)));
		assertThat(defFap.valoresPosibles.contains("valor1"), is(equalTo(true)));
		assertThat(defFap.valoresPosibles.contains("valor2"), is(equalTo(true)));
	}
	
	
	private ListaDefinicionMetadato getNuevaListaDefinicionMetadato() {
		ListaDefinicionMetadato lista = new ListaDefinicionMetadato();
		lista.getDefinicionMetadato().add(getNuevaDefinicionMetadato());
		return lista;
	}
	
	private DefinicionMetadato getNuevaDefinicionMetadato() {
		DefinicionMetadato definicion = new DefinicionMetadato();
		definicion.setDescripcion("NombreDefinicion");
		definicion.setValoresPosibles(getNuevaListaValorPosible());
		return definicion;
	}
	
	private ListaValorPosible getNuevaListaValorPosible() {
		ListaValorPosible valores = new ListaValorPosible();
		valores.getValorPosible().add("valor1");
		valores.getValorPosible().add("valor2");
		return valores;
	}
}
