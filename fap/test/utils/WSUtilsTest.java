package utils;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

public class WSUtilsTest {

	@Test
	public void getXmlGregorianCalendar() throws Exception {
		Date d = new Date();
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(d);
		XMLGregorianCalendar xml = WSUtils.getXmlGregorianCalendar(d);
		Assert.assertEquals(gc.get(GregorianCalendar.YEAR), xml.getYear());
		Assert.assertEquals(gc.get(GregorianCalendar.MONTH), xml.getMonth() - 1);
		Assert.assertEquals(gc.get(GregorianCalendar.DAY_OF_MONTH), xml.getDay());
		Assert.assertEquals(gc.get(GregorianCalendar.HOUR_OF_DAY), xml.getHour());
		Assert.assertEquals(gc.get(GregorianCalendar.MINUTE), xml.getMinute());
		Assert.assertEquals(gc.get(GregorianCalendar.SECOND), xml.getSecond());
	}
	
}
