package validation;

import org.junit.*;

import play.data.validation.CheckWithCheck;
import models.Nip;

public class NipCheckTest {

	NipCheck nipCheck = new NipCheck();
	
	@Test
	public void nifs(){
		Nip nip = new Nip();
		nip.tipo = "nif";
		
		//Comprueba validos
		nip.valor = "48967077P"; 
		Assert.assertTrue(nipCheck.isSatisfied(null, nip));
		
		nip.valor = "26885506R"; 
		Assert.assertTrue(nipCheck.isSatisfied(null, nip));
		
		nip.valor = "20745204D"; 
		Assert.assertTrue(nipCheck.isSatisfied(null, nip));
		
		nip.valor = "47636427T"; 
		Assert.assertTrue(nipCheck.isSatisfied(null, nip));
		
		nip.valor = "32205723L"; 
		Assert.assertTrue(nipCheck.isSatisfied(null, nip));
		
		nip.valor = "46586444J"; 
		Assert.assertTrue(nipCheck.isSatisfied(null, nip));
		
		nip.valor = "17261029N"; 
		Assert.assertTrue(nipCheck.isSatisfied(null, nip));
		
		//Comprueba invalidos
		nip.valor="123";
		nipCheck.checkWithCheck = new CheckWithCheck();
		Assert.assertFalse(nipCheck.isSatisfied(null, nip));
		Assert.assertEquals("validation.nip.nif.format", nipCheck.getCheckWithCheck().getMessage());
		
		nip.valor="17261029A";
		nipCheck.checkWithCheck = new CheckWithCheck();
		Assert.assertFalse(nipCheck.isSatisfied(null, nip));
		Assert.assertEquals("validation.nip.nif.letter", nipCheck.getCheckWithCheck().getMessage());
		
	}
	
}
