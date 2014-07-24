package properties;

import org.junit.Test;
import play.test.UnitTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

public class UpdatePropertiesTest extends UnitTest {

    public static final String NUEVA_PROPERTY = "testProperties.nueva.property";
    public static final String NUEVO_VALOR = "nuevo valor";
    public static final String PROPERTY_A_SOBRESCRIBIR = "testProperties.propertyEditable";
    public static final String VALOR1 = "valor1";
    public static final String VALOR_2 = "valor2";

    @Test
    public void addNuevaProperty() {
        FapProperties.updateProperty(NUEVA_PROPERTY, NUEVO_VALOR);
        assertThat(FapProperties.get(NUEVA_PROPERTY), is(equalTo(NUEVO_VALOR)));
    }

    @Test
    public void sobreescribePropertiesTest() {
        FapProperties.updateProperty(PROPERTY_A_SOBRESCRIBIR, VALOR1);
        assertThat(FapProperties.get(PROPERTY_A_SOBRESCRIBIR), is(equalTo(VALOR1)));
        FapProperties.updateProperty(PROPERTY_A_SOBRESCRIBIR, VALOR_2);
        assertThat(FapProperties.get(PROPERTY_A_SOBRESCRIBIR), is(not(equalTo(VALOR1))));
        assertThat(FapProperties.get(PROPERTY_A_SOBRESCRIBIR), is(equalTo(VALOR_2)));
    }
}
