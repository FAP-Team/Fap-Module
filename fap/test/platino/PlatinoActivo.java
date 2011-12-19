package platino;

public class PlatinoActivo {
	static Boolean activo = false;
	static {
		try {
			FirmaClient.getVersion();
			activo = true; 
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
