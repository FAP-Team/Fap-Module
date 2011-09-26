package tags;

import org.joda.time.DateTime;

public class FapJavaExtensions extends play.templates.JavaExtensions {
	
	public static String format(DateTime date) {
	     return format(date.toDate());
	}
}
