package model;

import models.TableKeyValueDependency;
import org.junit.Assert;
import org.junit.Test;
import play.test.UnitTest;

public class TableKeyValueDependencyTest extends UnitTest {

	@Test
	public void loadFromFile(){
        long count = TableKeyValueDependency.loadFromFiles();
        Assert.assertTrue(count > 0);
	}
	
}
