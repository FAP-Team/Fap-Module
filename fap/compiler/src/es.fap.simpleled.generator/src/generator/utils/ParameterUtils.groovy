package generator.utils

class ParameterUtils {
	
 	static final properties = "@";

	public static String parameter (String param) {
		if (isProperties(param)) {
			return "\$" + "{properties.FapProperties.get('${param.substring(1)}')}"
		}
		return param
	}
	
	static boolean isProperties(String param) {
		return param.startsWith(properties)
	}
}
