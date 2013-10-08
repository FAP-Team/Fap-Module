package services.async;

import java.util.List;

import play.libs.F.Promise;

import models.ResolucionFAP;
import registroresolucion.AreaResolucion;
import registroresolucion.RecursoResolucion;
import registroresolucion.RegistroResolucion;
import registroresolucion.TipoResolucion;
import services.RegistroLibroResolucionesServiceException;

public interface RegistroLibroResolucionesServiceAsync {

	public Promise<Integer> mostrarInfoInyeccion();
	
	public Promise<List<TipoResolucion> > leerTipos() throws RegistroLibroResolucionesServiceException;
	
	public Promise<List<AreaResolucion> > leerAreas() throws RegistroLibroResolucionesServiceException;
	
	public Promise<List<RecursoResolucion> > leerRecursos() throws RegistroLibroResolucionesServiceException;
	
	public Promise<RegistroResolucion> crearResolucion(ResolucionFAP resolucionFAP) throws RegistroLibroResolucionesServiceException;
}
