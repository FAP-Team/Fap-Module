package resolucion;

import properties.FapProperties;
import models.Registro;
import models.Resolucion;

public class ResolucionApp extends ResolucionBase {

	private final static String BODY_REPORT = "reports/resolucion/resolucion.html";
	private final static String TIPO_RESOLUCION = FapProperties.get("fap.aed.tiposdocumentos.resolucion");
	
	public ResolucionApp(Resolucion resolucion) {
		super(resolucion);
	}
	
	public boolean isPublicable() {
		return false;
	}
	
	public String getBodyReport() {
		return ResolucionApp.BODY_REPORT;
	}
	
	public String getTipoRegistroResolucion() {
		return ResolucionApp.TIPO_RESOLUCION;
	}
	
	public boolean hasAnexoConcedido() {
		
		return true;
	}
	
	public boolean hasAnexoDenegado() {
		return true;
	}
	
	public boolean hasAnexoExcluido() {
		return true;
	}

}
