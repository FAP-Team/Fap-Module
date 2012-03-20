package services;

import static org.junit.Assume.assumeTrue;

import org.junit.Before;

import properties.PropertyPlaceholder;
import services.platino.PlatinoRegistroServiceImpl;

public class PlatinoRegistroServiceTest extends RegistroServiceTest {

	@Before
	@Override
    public void before(){
        registroService = new PlatinoRegistroServiceImpl(propertyPlaceholder, firmaService, gestorDocumentalService);
        assumeTrue(registroService.isConfigured());
    }
    
}
