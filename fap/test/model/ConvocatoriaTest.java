package model;

import models.Convocatoria;
import org.junit.Test;
import play.test.UnitTest;
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
        PropertyPlaceholder mockPropertyPlaceholder = mock(PropertyPlaceholder.class);
        String expected = "C2001";
        when(mockPropertyPlaceholder.get(anyString())).thenReturn(expected);
        String actual = Convocatoria.getIdentificadorConvocatoria(mockPropertyPlaceholder);
        assertThat(actual,is(equalTo(expected)));
    }

    @Test
    public void devuelveIdentificadorConAnyoActualSiConvocatoriaAnual() {
        PropertyPlaceholder mockPropertyPlaceholder = mock(PropertyPlaceholder.class);
        String expected = Convocatoria.PREFIJO_CONVOCATORIA_ANUAL + Calendar.getInstance().get(Calendar.YEAR);
        when(mockPropertyPlaceholder.get(anyString())).thenReturn(Convocatoria.PREFIJO_CONVOCATORIA_ANUAL);
        String actual = Convocatoria.getIdentificadorConvocatoria(mockPropertyPlaceholder);
        assertThat(actual,is(equalTo(expected)));
    }
}
