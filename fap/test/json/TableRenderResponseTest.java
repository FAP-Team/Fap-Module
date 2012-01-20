package json;

import models.Agente;

import org.junit.Test;

import play.test.UnitTest;

import tables.TableRenderResponse;
import java.util.List;
import java.util.ArrayList;

import junit.framework.Assert;

public class TableRenderResponseTest extends UnitTest {

	@Test
	public void emptyRows(){
		TableRenderResponse<TableRenderResponseTestMock> response = new TableRenderResponse<TableRenderResponseTestMock>(null);
		String json = response.toJSON("campo1", "campo2");
		Assert.assertEquals("{\"rows\":[],\"total\":0}", json);
	}

	@Test
	public void normal(){
		List<TableRenderResponseTestMock> mocks = new ArrayList<TableRenderResponseTestMock>();
		mocks.add(new TableRenderResponseTestMock("a", "b"));
		mocks.add(new TableRenderResponseTestMock("c", "d"));
		TableRenderResponse<TableRenderResponseTestMock> response = TableRenderResponse.<TableRenderResponseTestMock>sinPermisos(mocks);
		String json = response.toJSON("campo1", "campo2");
		Assert.assertEquals("{\"rows\":[{\"campo1\":\"a\",\"campo2\":\"b\"},{\"campo1\":\"c\",\"campo2\":\"d\"}],\"total\":2}", json);
	}	
}
