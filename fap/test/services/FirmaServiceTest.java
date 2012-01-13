package services;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import platino.FirmaClient;
import platino.PlatinoActivo;
import play.modules.guice.InjectSupport;
import play.test.UnitTest;

import static org.junit.Assume.*;

@InjectSupport
public class FirmaServiceTest extends UnitTest {

	@Inject
	static FirmaService firmaService;
	
	static boolean hasConnection;
	
	@BeforeClass
	public static void beforeClass(){
		hasConnection = firmaService.hasConnection(); 
	}
	
	@Before
	public void before(){
		assumeTrue(hasConnection);
	}
	
	private void firmaryvalidar(String texto){
		String firma = firmaService.firmarPKCS7(texto);
		Boolean firmacorrecta = firmaService.verificarPKCS7(texto, firma);
		Assert.assertNotNull(firma);
		Assert.assertTrue(firmacorrecta);		
	}
	
	@Test
	public void firmaPKCS7(){
		firmaryvalidar("Hola, esto es un texto simple sin tildes");
	}
	
	@Test
	public void firmaPKCS7Tildes(){
		firmaryvalidar("Texto con tildes áéíóúÁÉÍÓÚ");
	}
	
}
