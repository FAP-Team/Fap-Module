package services;

import java.util.List;

import models.ResolucionFAP;

import registroresolucion.AreaResolucion;
import registroresolucion.RecursoResolucion;
import registroresolucion.RegistroResolucion;
import registroresolucion.TipoResolucion;

public interface RegistroLibroResolucionesService {
	
	public void mostrarInfoInyeccion();
	
	public List<TipoResolucion> leerTipos() throws RegistroLibroResolucionesServiceException;
	
	public List<AreaResolucion> leerAreas() throws RegistroLibroResolucionesServiceException;
	
	public List<RecursoResolucion> leerRecursos() throws RegistroLibroResolucionesServiceException;
	
	public RegistroResolucion crearResolucion(ResolucionFAP resolucionFAP) throws RegistroLibroResolucionesServiceException;
	
}
