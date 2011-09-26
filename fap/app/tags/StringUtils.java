package tags;

public class StringUtils {
	
	public static String firstLower(String str) {
		if (str == null || str.equals("")) return str;
		return str.substring(0, 1).toLowerCase() + str.substring(1, str.length());
	}
	
	public static String firstUpper(String str) {
		if (str == null || str.equals("")) return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
	}
	
}
