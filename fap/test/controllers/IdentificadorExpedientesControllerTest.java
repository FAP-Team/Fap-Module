package controllers;

import controllers.fap.IdentificadorExpedientesController;
import org.junit.Test;
import play.test.UnitTest;
import properties.FapProperties;

import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;

public class IdentificadorExpedientesControllerTest extends UnitTest {

    public static final String FAP_AED_EXPEDIENTE_MODALIDAD = "fap.aed.expediente.modalidad";
    public static final String FAP_AED_EXPEDIENTE_PREFIJO = "fap.aed.expediente.prefijo";
    public static final String PREFIJO_EXP = "TEST";

    @Test
    public void devuelveIdentificadorConAnyo() {
        FapProperties.updateProperty(FAP_AED_EXPEDIENTE_MODALIDAD,"anual");
        FapProperties.updateProperty(FAP_AED_EXPEDIENTE_PREFIJO, PREFIJO_EXP);
        String anyo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        String idExp = IdentificadorExpedientesController.getNuevoIdExpediente("");

        boolean coincide = idExp.matches(PREFIJO_EXP +anyo+"\\d{4}");
        assertThat(coincide, is(true));
    }

    @Test
    public void devuelveIdentificadorSinAnyo() {
        FapProperties.updateProperty(FAP_AED_EXPEDIENTE_MODALIDAD,"convocatoria");
        FapProperties.updateProperty(FAP_AED_EXPEDIENTE_PREFIJO, PREFIJO_EXP);
        String anyo = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        String idExp = IdentificadorExpedientesController.getNuevoIdExpediente("");

        boolean noCoincide = idExp.matches(PREFIJO_EXP +anyo+"\\d{4}");
        boolean coincide = idExp.matches(PREFIJO_EXP +"\\d{4}");
        assertThat(noCoincide, is(false));
        assertThat(coincide, is(true));
    }

}

