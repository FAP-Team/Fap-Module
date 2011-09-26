package utils;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import templates.GTabla;
import es.fap.simpleled.led.Columna;
import es.fap.simpleled.led.LedFactory;
import es.fap.simpleled.led.impl.LedFactoryImpl;

public class ColumnaFuncionTest {

	LedFactory factory = LedFactoryImpl.init();
	
	@Test(expected=IllegalArgumentException.class)
	public void test0(){
		Columna c = factory.createColumna();
		Assert.assertNotNull(c);
		GTabla.camposDeColumna(c);
	}
	
	@Test
	public void test1(){
//		Columna c = factory.createColumna();
//		Assert.assertNotNull(c);
//		c.setCampo("campo0");
//		List<String> campos = GTabla.camposDeColumna(c);
//		Assert.assertTrue(campos.size() == 1);
//		Assert.assertTrue(campos.get(0).equals("campo0"));
	}
	
	@Test
	public void test2(){
//		Columna c = factory.createColumna();
//		Assert.assertNotNull(c);
//		c.setFuncion("${campo0} y ${campo1}");
//		
//		List<String> campos = GTabla.camposDeColumna(c);
//		
//		Assert.assertNotNull(campos);
//		Assert.assertTrue(campos.size() == 2);
//		Assert.assertTrue(campos.get(0).equals("campo0"));
//		Assert.assertTrue(campos.get(1).equals("campo1"));
	}
	
	@Test
	public void test3(){
//		Columna c = factory.createColumna();
//		Assert.assertNotNull(c);
//		c.setFuncion("{   {  {  $          ${campo0} y }}} ${campo1}  }}}}}");
//		
//		List<String> campos = GTabla.camposDeColumna(c);
//		
//		Assert.assertNotNull(campos);
//		Assert.assertTrue(campos.size() == 2);
//		Assert.assertTrue(campos.get(0).equals("campo0"));
//		Assert.assertTrue(campos.get(1).equals("campo1"));		
	}
	
	@Test
	public void test4(){
		Columna c = factory.createColumna();
		Assert.assertNotNull(c);
		c.setFuncion("${campo0} y ${campo1}");
		
		String renderer = GTabla.renderer(c);
		
		Assert.assertEquals("return '' + record['campo0'] + ' y ' + record['campo1'] + '';", renderer);
	}
}
