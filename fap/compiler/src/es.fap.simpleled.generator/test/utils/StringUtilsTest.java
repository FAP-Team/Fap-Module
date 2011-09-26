package utils;

import generator.utils.StringUtils;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {
	
	@Test
	public void accent(){		
		Assert.assertEquals("aeiouAEIOU", StringUtils.noAccents("áéíóúÁÉÍÓÚ"));
		Assert.assertEquals("nN", StringUtils.noAccents("ñÑ"));
		Assert.assertEquals("aeiouAEIOU", StringUtils.noAccents("âêîôûÂÊÎÔÛ"));
	}
	
	@Test
	public void noBlank(){
		Assert.assertEquals("normal", StringUtils.noBlank("normal"));
		Assert.assertEquals("texto-con-espacios", StringUtils.noBlank("texto con espacios"));
	}
	
	@Test
	public void id(){
		Assert.assertEquals("identificador", StringUtils.id("identificador"));
		Assert.assertEquals("identificador-con-espacios", StringUtils.id("identificador con espacios"));
		Assert.assertEquals("identificadorcontildes", StringUtils.id("ídéntífícádórcóntîldês"));
		Assert.assertEquals("identificador-con-tildes-y-espacios", StringUtils.id("ídéntífícádór cón tîldês ÿ éspácíós"));
	}
	
	
}
