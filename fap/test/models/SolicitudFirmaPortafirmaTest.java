package models;

import models.SolicitudFirmaPortafirma;
import org.junit.Test;
import play.libs.Crypto;
import play.test.UnitTest;
import properties.FapProperties;
import properties.FapPropertiesKeys;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

public class SolicitudFirmaPortafirmaTest extends UnitTest{

    private static final String PASS_PLANA = "CONTRASEÑA EN TEXTO PLANO";
    private static final String SECRETO_DIFERENTE = "SECRETODIFERENTE PARA ENCRIPTAR".substring(0, 16);

    private static SolicitudFirmaPortafirma stubSolicitudFirmaPortafirma() {
        SolicitudFirmaPortafirma solicitudFirmaPortafirma = new SolicitudFirmaPortafirma();
        solicitudFirmaPortafirma.passwordSolicitante = PASS_PLANA;
        return solicitudFirmaPortafirma;
    }

    @Test
    public void seEncriptaLaPassAlGuardar() {
        SolicitudFirmaPortafirma solicitudFirmaPortafirma = stubSolicitudFirmaPortafirma();
        String passExpected = Crypto.encryptAES(PASS_PLANA, solicitudFirmaPortafirma.getSecretoEncriptacion());
        solicitudFirmaPortafirma.save();

        assertThat(solicitudFirmaPortafirma.passwordSolicitante, is(equalTo(passExpected)));
    }

    @Test
    public void elPassEncriptadoEsDistintoDeLaPassDelSolicitante() {
        SolicitudFirmaPortafirma solicitudFirmaPortafirma = stubSolicitudFirmaPortafirma();
        assertThat(solicitudFirmaPortafirma.passwordSolicitante, is(equalTo(PASS_PLANA)));

        solicitudFirmaPortafirma.encriptarPassword();

        assertThat(solicitudFirmaPortafirma.passwordSolicitante, is(not(equalTo(PASS_PLANA))));
    }

    @Test
    public void laPassSePuedeDesencriptar() {
        SolicitudFirmaPortafirma solicitudFirmaPortafirma = stubSolicitudFirmaPortafirma();
        assertThat(solicitudFirmaPortafirma.passwordSolicitante, is(equalTo(PASS_PLANA)));

        solicitudFirmaPortafirma.encriptarPassword();
        assertThat(solicitudFirmaPortafirma.passwordSolicitante, is(not(equalTo(PASS_PLANA))));

        String passwordDesencriptada = solicitudFirmaPortafirma.desencriptarPassword();
        assertThat(passwordDesencriptada, is(equalTo(PASS_PLANA)));
    }

    @Test
    public void laPassNoSeDesencriptaConOtraClaveSecreta() {
        SolicitudFirmaPortafirma solicitudFirmaPortafirma = stubSolicitudFirmaPortafirma();
        assertThat(solicitudFirmaPortafirma.passwordSolicitante, is(equalTo(PASS_PLANA)));

        solicitudFirmaPortafirma.encriptarPassword();
        assertThat(solicitudFirmaPortafirma.passwordSolicitante, is(not(equalTo(PASS_PLANA))));

        String passwordDesencriptada = solicitudFirmaPortafirma.desencriptarPassword(SECRETO_DIFERENTE);
        assertThat(passwordDesencriptada, is(not(equalTo(PASS_PLANA))));
    }

}
