package model;

import models.Convocatoria;
import org.junit.Test;
import play.test.UnitTest;
import properties.FapProperties;
import properties.FapPropertiesKeys;
import properties.PropertyPlaceholder;

import java.util.Calendar;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConvocatoriaTest extends UnitTest {
    @Test
    public void devuelveIdentificadorDeConvocatoriaNoAnual() {
        FapProperties.updateProperty(FapPropertiesKeys.AED_CONVOCATORIA, "anual");
        PropertyPlaceholder mockPropertyPlaceholder = mock(PropertyPlaceholder.class);
        String expected = "C2001";
        when(mockPropertyPlaceholder.get(anyString())).thenReturn(expected);
        String actual = Convocatoria.getIdentificadorConvocatoria(mockPropertyPlaceholder);
        FapProperties.updateProperty(FapPropertiesKeys.AED_CONVOCATORIA, "fap.aed.convocatoria");
        assertThat(actual,is(equalTo(expected)));
    }

    @Test
    public void devuelveIdentificadorConAnyoActualSiConvocatoriaAnual() {
        FapProperties.updateProperty(FapPropertiesKeys.AED_CONVOCATORIA, Convocatoria.PREFIJO_CONVOCATORIA_ANUAL);
        PropertyPlaceholder mockPropertyPlaceholder = mock(PropertyPlaceholder.class);
        String expected = Convocatoria.PREFIJO_CONVOCATORIA_ANUAL + Calendar.getInstance().get(Calendar.YEAR);
        String actual = Convocatoria.getIdentificadorConvocatoria(mockPropertyPlaceholder);
        FapProperties.updateProperty(FapPropertiesKeys.AED_CONVOCATORIA, "fap.aed.convocatoria");
        assertThat(actual,is(equalTo(expected)));
    }
}
