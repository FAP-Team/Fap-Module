package utils;

import org.junit.Assert;
import org.junit.Test;

public class CharsetUtilsTest {

	@Test
	public void convert(){
		String utf = "ó";
		System.out.println(utf);
		String iso = CharsetUtils.fromUTF82ISO(utf);
		System.out.println(iso);
		Assert.assertFalse(utf.equals(iso));
	}
	
}
