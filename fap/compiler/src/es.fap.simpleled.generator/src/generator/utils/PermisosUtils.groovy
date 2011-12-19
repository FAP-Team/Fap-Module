package generator.utils;

@Deprecated
public class PermisosUtils {
	@Deprecated
	public static String className(){
		if (LedUtils.generatingModule){
			return "secure.PermissionFap.";
		}
		return "secure.Permission.";
	}

}
