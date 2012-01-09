package properties;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

public class MapPropertyPlaceholderTest {

	@Test
	public void defaultValues(){
		MapPropertyPlaceholder prop = new MapPropertyPlaceholder(new HashMap<String, String>());
		Assert.assertEquals(prop.get("notProperty"), null);
		Assert.assertEquals(prop.getInt("notProperty"), 0);
		Assert.assertEquals(prop.getBoolean("notProperty"), null);
		Assert.assertEquals(prop.getLong("notProperty"), 0);
	}
	
	@Test
	public void parameterConstructor(){
		MapPropertyPlaceholder prop = new MapPropertyPlaceholder("str", "str", "number", "1", "boolean", "true");
		Assert.assertEquals(prop.get("str"), "str");
		Assert.assertEquals(prop.getInt("number"), 1);
		Assert.assertEquals(prop.getBoolean("boolean"), true);
		Assert.assertEquals(prop.getLong("number"), 1);
	}
}
