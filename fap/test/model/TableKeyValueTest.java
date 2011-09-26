package model;

import models.TableKeyValue;
import org.junit.Assert;
import org.junit.Test;
import play.test.UnitTest;

public class TableKeyValueTest extends UnitTest {

//	@Test
//	public void prueba(){
//		List<TableKeyValue> list = TableKeyValue.findByTable(Tables.ROLES);
//		Assert.assertEquals(list.size(), 2);
//	}
	
	@Test
	public void loadFromFile(){
        long count = TableKeyValue.loadFromFiles();
        Assert.assertTrue(count > 0);
	}
	
}
