package services;

import properties.PropertyPlaceholder;
import services.platino.PlatinoRegistroServiceImpl;

public class PlatinoRegistroServiceTest extends RegistroServiceTest {

    public void beforeClass(){
        registroService = new PlatinoRegistroServiceImpl(propertyPlaceholder, firmaService, gestorDocumentalService);
    }
    
}
