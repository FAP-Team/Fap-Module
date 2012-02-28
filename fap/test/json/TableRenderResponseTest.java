package json;



import messages.Messages;
import messages.Messages.MessageType;
import models.Agente;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import play.test.UnitTest;

import tables.TableRenderResponse;
import java.util.List;
import java.util.ArrayList;

import static junit.framework.Assert.*;

public class TableRenderResponseTest extends Assert {

	@Test
	public void emptyRows(){
		TableRenderResponse<TableRenderResponseTestMock> response = new TableRenderResponse<TableRenderResponseTestMock>(null);
		String json = response.toJSON("campo1", "campo2");
		assertEqualsJson("{\"obj\":{\"mensajes\":{\"error\":null,\"fatal\":null,\"info\":null,\"ok\":null,\"warning\":null},\"rows\":null}}", json); 
	}

	@Test
	public void normal(){
		List<TableRenderResponseTestMock> mocks = new ArrayList<TableRenderResponseTestMock>();
		mocks.add(new TableRenderResponseTestMock("a", "b"));
		mocks.add(new TableRenderResponseTestMock("c", "d"));
		TableRenderResponse<TableRenderResponseTestMock> response = TableRenderResponse.sinPermisos(mocks);
		String json = response.toJSON("campo1", "campo2");
		assertEqualsJson("{\"obj\":{\"mensajes\":{\"error\":null,\"fatal\":null,\"info\":null,\"ok\":null,\"warning\":null},\"rows\":[{\"objeto\":{\"campo1\":\"a\",\"campo2\":\"b\"},\"permisoBorrar\":true,\"permisoEditar\":true,\"permisoLeer\":true},{\"objeto\":{\"campo1\":\"c\",\"campo2\":\"d\"},\"permisoBorrar\":true,\"permisoEditar\":true,\"permisoLeer\":true}]}}", json);
	}
	
	@Test
	public void conMensajes(){
        List<TableRenderResponseTestMock> mocks = new ArrayList<TableRenderResponseTestMock>();
        mocks.add(new TableRenderResponseTestMock("a", "b"));
        mocks.add(new TableRenderResponseTestMock("c", "d"));
        
        Messages.error("mensaje error");
        Messages.fatal("mensaje fatal");
        Messages.warning("mensaje warning");
        Messages.ok("mensaje ok");
        Messages.info("mensaje info");
        
        TableRenderResponse<TableRenderResponseTestMock> response = TableRenderResponse.sinPermisos(mocks);
        
        String json = response.toJSON("campo1", "campo2");
        assertEqualsJson("{\"obj\":{\"mensajes\":{\"error\":[\"mensaje error\"],\"fatal\":[\"mensaje fatal\"],\"info\":[\"mensaje info\"],\"ok\":[\"mensaje ok\"],\"warning\":[\"mensaje warning\"]},\"rows\":[{\"objeto\":{\"campo1\":\"a\",\"campo2\":\"b\"},\"permisoBorrar\":true,\"permisoEditar\":true,\"permisoLeer\":true},{\"objeto\":{\"campo1\":\"c\",\"campo2\":\"d\"},\"permisoBorrar\":true,\"permisoEditar\":true,\"permisoLeer\":true}]}}", json);
	}
	
	
	/**
	 * Compara dos json sin importar el orden de sus campos
	 * @param expected
	 * @param actual
	 * @return
	 */
	private void assertEqualsJson(String jsonExpected, String jsonActual){
		JsonParser parser = new JsonParser();
		JsonElement expected = parser.parse(jsonExpected);
		JsonElement actual = parser.parse(jsonActual);
		assertEquals(expected, actual);
	}
	
}
