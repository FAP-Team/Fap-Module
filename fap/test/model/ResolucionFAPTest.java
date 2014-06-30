package model;

import models.Agente;
import models.ResolucionFAP;
import org.junit.Test;
import play.test.UnitTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ResolucionFAPTest extends UnitTest {

    public static final String CADENA_VACIA = "";
    public static final String USERNAME_NO_VALIDO = "username_no_valido";
    public static final String USERNAME_VALIDO = "username_valido";

    @Test
    public void seDevuelveElAgenteSolicitadoParaFirmarOficios (){
        ResolucionFAP resolucionFAP = stubResolucionFAP();
        Agente esperado = stubAgente(USERNAME_VALIDO);
        esperado.cargo = "cargo_valido";
        esperado.save();
        resolucionFAP.idSolicitudFirmaOficiosRemision = USERNAME_VALIDO;

        Agente obtenido = resolucionFAP.getAgenteSolicitadoFirmaOficiosRemision();
        assertThat(obtenido.username, is(equalTo(esperado.username)));
        assertThat(obtenido.cargo, is(equalTo(esperado.cargo)));

        esperado.delete();
    }

    @Test
    public void seDevuelveAgenteVacioSiElAgenteSolicitadoNoExiste (){
        ResolucionFAP resolucionFAP = stubResolucionFAP();
        resolucionFAP.idSolicitudFirmaOficiosRemision = USERNAME_NO_VALIDO;
        String usernameEsperado = CADENA_VACIA;
        String cargoEsperado = CADENA_VACIA;

        Agente obtenido = resolucionFAP.getAgenteSolicitadoFirmaOficiosRemision();
        assertThat(obtenido.username, is(equalTo(usernameEsperado)));
        assertThat(obtenido.cargo, is(equalTo(cargoEsperado)));
    }


    private Agente stubAgente(String username) {
        Agente agente = new Agente();
        agente.username = username;
        return agente;
    }

    private ResolucionFAP stubResolucionFAP() {
        ResolucionFAP resolucionFAP = new ResolucionFAP();
        return resolucionFAP;
    }
}
