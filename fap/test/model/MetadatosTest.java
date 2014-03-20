package model;

import java.util.ArrayList;
import java.util.List;

import models.EsquemaMetadato;
import models.Metadato;
import models.MetadatoTipoPatron;
import models.MetadatoTipoTabla;
import models.ValoresValidosMetadatos;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import static org.hamcrest.CoreMatchers.*;
import play.Logger;
import play.test.UnitTest;
import utils.MetadatosUtils;

public class MetadatosTest extends UnitTest{
	
	static EsquemaMetadato expectedEsquema;
	static String nombreExpEsquema = "nombre";
	
	@BeforeClass
	public static void generaExpectedEsquema() {
		expectedEsquema = new EsquemaMetadato();
		expectedEsquema.nombre = nombreExpEsquema;
		expectedEsquema.definicion = "definicion";
		expectedEsquema.obligatoriedad = "no";
		expectedEsquema.tipoDeDato = "texto";
		expectedEsquema.repeticion ="unico";
		expectedEsquema.equivalencia = "ninguna";
		expectedEsquema.automatizable = "no";
		generaEsquemaTipoTabla(expectedEsquema);
	}
	
	private static void generaEsquemaTipoTabla(EsquemaMetadato esq) {
		expectedEsquema = (esq != null) ? esq : EsquemaMetadato.get(nombreExpEsquema);
		List<ValoresValidosMetadatos> valores = new ArrayList<ValoresValidosMetadatos>();
		ValoresValidosMetadatos elemento = new ValoresValidosMetadatos();
		elemento.clave="clave1";
		elemento.valor="valor1";
		valores.add(elemento);
		elemento = new ValoresValidosMetadatos();
		elemento.clave="clave2";
		elemento.valor="valor2";
		valores.add(elemento);
		expectedEsquema.tipoDeDato = "tabla codificada";
		expectedEsquema.valores = valores;
	}
	
	
	@Before
	public void cargaInicialEsquema() {
		if (EsquemaMetadato.count() <= 0) {
			String esquema1 = "{\"esquema metadatos\":[{\"nombre\":\"Versión NTI\",\"definicion\":\"URI del Identificador normalizado de la versión de la Norma Técnica de Interoperabilidad de documento electrónico conforme a la cual se estructura el expediente\",\"obligatoriedad\":\"obligatorio\",\"tipoDeDato\":\"texto\",\"repeticion\":\"unico\",\"equivalencia\":\"e-EMGDE 2.1\",\"automatizable\":\"si\"},{\"nombre\":\"Nombre\",\"definicion\":\"Título o nombre dado al documento\",\"obligatoriedad\":\"obligatorio\",\"tipoDeDato\":\"texto\",\"repeticion\":\"unico\",\"equivalencia\":\"e-EMGDE 3\",\"automatizable\":\"no\"}]}";
			String esquema2 = String.format(
					"{\"esquema metadatos\":[{\"nombre\":\"%s\",\"definicion\":\"%s\",\"obligatoriedad\":\"%s\",\"tipoDeDato\":\"%s\",\"repeticion\":\"%s\",\"equivalencia\":\"%s\",\"automatizable\":\"%s\", \"valores\":[{\"clave\":\"%s\",\"valor\":\"%s\"},{\"clave\":\"%s\",\"valor\":\"%s\"}]}]}",
					expectedEsquema.nombre, expectedEsquema.definicion, expectedEsquema.obligatoriedad, expectedEsquema.tipoDeDato, expectedEsquema.repeticion, expectedEsquema.repeticion, expectedEsquema.equivalencia, expectedEsquema.automatizable, expectedEsquema.valores.get(0).clave, expectedEsquema.valores.get(0).valor, expectedEsquema.valores.get(1).clave, expectedEsquema.valores.get(1).valor);
			MetadatosUtils.esquemaFromJson(esquema2);
		}
	}
	
	@Test
	public void seCargaElEsquema(){
		List<EsquemaMetadato> esquema = EsquemaMetadato.findAll();
		assertFalse(esquema.isEmpty());
		EsquemaMetadato actual = esquema.get(0);
		assertEquals(expectedEsquema, actual);
	}

	@Test
	public void factoriaDevuelveTipoTabla(){
		Metadato expected;
		Metadato actual;
		expectedEsquema = EsquemaMetadato.get(expectedEsquema.nombre);
		expected = new Metadato();
		expectedEsquema.tipoDeDato = "tabla codificada";
		expectedEsquema.save();
		expected = new MetadatoTipoTabla();
		
		actual = MetadatosUtils.SimpleFactory.getMetadato(expectedEsquema.nombre);
		assertEquals(expected.getClass(), actual.getClass());
	}
	
	@Test
	public void factoriaDevuelveTipoPatron(){
		Metadato expected;
		Metadato actual;
		expectedEsquema = EsquemaMetadato.get(expectedEsquema.nombre);
		expectedEsquema.tipoDeDato = "texto";
		expectedEsquema.patron = new ArrayList<String>();
		expectedEsquema.patron.add("123");
		expectedEsquema.save();
		expected = new MetadatoTipoPatron();
		
		actual = MetadatosUtils.SimpleFactory.getMetadato(expectedEsquema.nombre);
		assertEquals(expected.getClass(), actual.getClass());
	}
	
	@Test
	public void factoriaDevuelveTipoPadre(){
		Metadato expected;
		Metadato actual;
		expectedEsquema = EsquemaMetadato.get(expectedEsquema.nombre);
		expectedEsquema.tipoDeDato = "texto";
		expectedEsquema.patron = null;
		expectedEsquema.save();
		expected = new Metadato();
		
		actual = MetadatosUtils.SimpleFactory.getMetadato(expectedEsquema.nombre);
		assertEquals(expected.getClass(), actual.getClass());
	}

	@Test
	public void factoriaDevuelveNombre() {
		Metadato actual;
		EsquemaMetadato esq = EsquemaMetadato.get(expectedEsquema.nombre);
		actual = MetadatosUtils.SimpleFactory.getMetadato(esq.nombre);
		assertEquals(actual.nombre, esq.nombre);
	}

	@Test
	public void metodoValidarGenericoEsValidoSinLongitud() {
		EsquemaMetadato esq = EsquemaMetadato.get(expectedEsquema.nombre);
		esq.tipoDeDato = "texto";
		esq.longitud = null;
		esq.save();
		Metadato actual = MetadatosUtils.SimpleFactory.getMetadato(esq.nombre);
		String nuevoValor = "Cadena que debería ser válida"; 
		actual.valor = nuevoValor;
		assertThat(actual.esValido(), is(true));
	}
	
	@Test
	public void metodoValidarGenericoEsValidoConLongitud() {
		EsquemaMetadato esq = EsquemaMetadato.get(expectedEsquema.nombre);
		esq.tipoDeDato = "texto";
		esq.longitud = "100";
		esq.save();
		Metadato actual = MetadatosUtils.SimpleFactory.getMetadato(esq.nombre);
		String nuevoValor = "Cadena que debería ser válida"; 
		actual.valor = nuevoValor;
		assertThat(actual.esValido(), is(true));
	}
	
	@Test
	public void metodoValidarGenericoNoEsValidoPorLongitud() {
		EsquemaMetadato esq = EsquemaMetadato.get(expectedEsquema.nombre);
		esq.tipoDeDato = "texto";
		esq.longitud = "10";
		esq.save();
		Metadato actual = MetadatosUtils.SimpleFactory.getMetadato(esq.nombre);
		String nuevoValor = "Cadena que es demasiado larga para ser válida"; 
		actual.valor = nuevoValor;
		assertThat(actual.esValido(), is(false));
	}
	
	@Test
	public void metodoValidarGenericoNoEsValidoPorNull() {
		EsquemaMetadato esq = EsquemaMetadato.get(expectedEsquema.nombre);
		esq.tipoDeDato = "texto";
		esq.save();
		Metadato actual = MetadatosUtils.SimpleFactory.getMetadato(esq.nombre);
		String nuevoValor = null; 
		actual.valor = nuevoValor;
		assertThat(actual.esValido(), is(false));
	}
	
	@Test
	public void metodoValidarTipoTablasEsValido(){
		EsquemaMetadato esq = EsquemaMetadato.get(expectedEsquema.nombre);
		esq.tipoDeDato = "tabla codificada";
		esq.save();
		Metadato actual = MetadatosUtils.SimpleFactory.getMetadato(esq.nombre);
		String nuevoValor = esq.valores.get(0).clave; 
		actual.valor = nuevoValor;
		assertThat(actual.esValido(), is(true));
	}
	
	@Test
	public void metodoValidarTipoTablasEsNoValido(){
		EsquemaMetadato esq = EsquemaMetadato.get(expectedEsquema.nombre);
		esq.tipoDeDato = "tabla codificada";
		esq.save();
		Metadato actual = MetadatosUtils.SimpleFactory.getMetadato(esq.nombre);
		String nuevoValorNoValido = "~~cadena~No~Valida~~"; 
		actual.valor = nuevoValorNoValido;
		assertThat(actual.esValido(), is(false));
	}
}
