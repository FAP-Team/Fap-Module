package controllers;

import org.apache.cxf.ws.policy.Assertor;
import org.junit.Test;

import models.TableKeyValue;
import models.TipoDocumento;
import models.Tramite;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Scope;
import play.mvc.Scope.Session;
import play.test.FunctionalTest;
import utils.Fixtures;
import utils.SessionUtils;

import java.util.*;
import java.util.Map.Entry;

public class AedControllerTest extends FunctionalTest {

    private static final String TEST_TIPODOCUMENTO_NAME = "fakeTipoDocumento";

    private static final String TEST_TRAMITE_NAME = "fake";

    private static String authenticity_token;
    
    private Map<String, String> newAuthParams(){
        Map<String, String> params = new HashMap<String, String>();
        putAuthenticityToken(params);
        return params;
    }
    
    private void putAuthenticityToken(Map<String, String> parameters) {
        if(authenticity_token == null)
            throw new NullPointerException();
        parameters.put("authenticityToken", authenticity_token);
    }
    
    private void loginAsAdmin() {
        Response loginFormResponse = GET("/login");
        assertIsOk(loginFormResponse);
        
        Cookie sessionCookie = loginFormResponse.cookies.get("PLAY_SESSION");
        Session session = SessionUtils.parseSession(sessionCookie.value);
        authenticity_token = session.getAuthenticityToken();
        
        Map<String, String> parameters = newAuthParams();
        parameters.put("username", "admin");
        parameters.put("password", "a");
        
        Response loginResponse = POST("/login/password", parameters);
        assertRedirect(loginResponse);
    }

    private void assertRedirect(Response response){
        assertEquals(302, response.status.intValue());
    }
    
    private void changeRol(String rol){
        Map<String, String> params = newAuthParams();
        params.put("rolActivo", rol);
        Response changeRolResponse = POST("/changerol", params);
        assertRedirect(changeRolResponse);
    }
    
    @Test
    public void actualizarTramites() {
        loginAsAdmin();
        changeRol("administrador");
        
        Tramite.deleteAllTramitesAndTipoDocumentos();
        assertEquals(0, Tramite.count());
        
        saveFakeTramite();
        
        Response response = POST("/Administracion/aed/actualizarTramites", newAuthParams());
        assertRedirect(response);
        assertTrue(Tramite.count() > 0);
        
        //Comprueba que se borran los trámites antiguos
        Tramite tramite = Tramite.find("byNombre", TEST_TRAMITE_NAME).first();
        assertNull(tramite);
        
        //Comprueba que registra los tipos de documentos
        long nTipoDocumento = TipoDocumento.count();
        List<TableKeyValue> findByTable = TableKeyValue.findByTable("tiposDocumentos");
        assertTrue(nTipoDocumento > 0);
        assertEquals(nTipoDocumento, findByTable.size());
    }

    private void saveFakeTramite() {
        Tramite fakeTramite = new Tramite();
        fakeTramite.nombre = TEST_TRAMITE_NAME;
        TipoDocumento d = new TipoDocumento();
        d.nombre = TEST_TIPODOCUMENTO_NAME;
        fakeTramite.documentos.add(d);
        fakeTramite.save();
    }

}
