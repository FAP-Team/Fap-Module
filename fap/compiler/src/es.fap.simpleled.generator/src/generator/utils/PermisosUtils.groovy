package generator.utils;

public class PermisosUtils {
	
	public static String className(){
		if (LedUtils.generatingModule){
			return "secure.PermissionFap.";
		}
		return "secure.Permission.";
	}

}
