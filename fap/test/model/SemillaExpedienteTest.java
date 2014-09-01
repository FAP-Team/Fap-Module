package model;

import models.Convocatoria;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import models.ExpedienteAed;
import models.SemillaExpediente;
import play.test.UnitTest;
import properties.FapProperties;
import properties.FapPropertiesKeys;

import java.util.Calendar;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class SemillaExpedienteTest extends UnitTest {

    public static final Long VALOR_SEMILLA = (long)23;
    private static final long VALOR_INICIAL_EXPEDIENTE = 0;
    private static final int ANYO_DIFERENTE = 2013;

    @Before
    public void borrarSemillas() {
        SemillaExpediente.deleteAll();
    }

	@Test
	public void semilla(){
		ExpedienteAed expediente1 = new ExpedienteAed();
		String id1 = expediente1.asignarIdAed();
		Assert.assertNotNull(id1);
		ExpedienteAed expediente2 = new ExpedienteAed();
		String id2 = expediente2.asignarIdAed();
		Assert.assertNotNull(id2);
		Assert.assertFalse(id1.equals(id2));
	}

    @Test
    public void semillaValorNuloDevuelveElId() {
        SemillaExpediente s1 = saveNuevaSemilla(null,2014);
        SemillaExpediente s2 = new SemillaExpediente();
        s2.save();
        assertThat(s2.semilla, is(equalTo(s1.getValorSemilla() + 1)));
    }

    @Test
    public void semillaSumaConsecutivaEnNoAnual() {
        FapProperties.updateProperty(FapPropertiesKeys.AED_EXPEDIENTE_MODALIDAD, "convocatoria");
        SemillaExpediente s1 = saveNuevaSemilla(VALOR_SEMILLA,2014);
        s1.save();
        SemillaExpediente s2 = new SemillaExpediente();
        s2.save();
        assertThat(s2.semilla, is(equalTo(s1.semilla + 1)));
    }

    @Test
    public void semillaSumaConsecutivaMismoAnyo() {
        FapProperties.updateProperty(FapPropertiesKeys.AED_EXPEDIENTE_MODALIDAD, Convocatoria.ANUAL);
        SemillaExpediente s1 = saveNuevaSemilla(VALOR_SEMILLA,getAnyo());
        SemillaExpediente s2 = new SemillaExpediente();
        s2.save();
        assertThat(s2.semilla, is(equalTo(s1.semilla + 1)));
    }

    @Test
    public void semillaSumaConsecutivaDistintoAnyoYPropertyNoActiva() {
        FapProperties.updateProperty(FapPropertiesKeys.AED_EXPEDIENTE_MODALIDAD, Convocatoria.ANUAL);
        FapProperties.updateProperty(FapPropertiesKeys.AED_EXPEDIENTE_PREFIJO_REINICIAR_ANUALMENTE, "false");
        SemillaExpediente s1 = saveNuevaSemilla(VALOR_SEMILLA, ANYO_DIFERENTE);
        SemillaExpediente s2 = new SemillaExpediente();
        s2.save();
        assertThat(s2.semilla, is(equalTo(s1.semilla + 1)));
    }

    @Test
    public void semillaReiniciaSiDistintoAnyoYPorpertyActiva() {
        FapProperties.updateProperty(FapPropertiesKeys.AED_EXPEDIENTE_MODALIDAD, Convocatoria.ANUAL);
        FapProperties.updateProperty(FapPropertiesKeys.AED_EXPEDIENTE_PREFIJO_REINICIAR_ANUALMENTE, "true");
        SemillaExpediente s1 = saveNuevaSemilla(VALOR_SEMILLA,ANYO_DIFERENTE);
        SemillaExpediente s2 = new SemillaExpediente();
        s2.save();
        assertThat(s2.semilla, is(equalTo(VALOR_INICIAL_EXPEDIENTE + 1)));
    }

    private SemillaExpediente saveNuevaSemilla(Long valor, int anyo) {
        SemillaExpediente semillaExpediente = new SemillaExpediente();
        semillaExpediente.anyo = anyo;
        semillaExpediente.semilla = valor;
        semillaExpediente.save();
        return semillaExpediente;
    }

    private int getAnyo() {
        Calendar now = Calendar.getInstance();
        return now.get(Calendar.YEAR);
	}
	
}
