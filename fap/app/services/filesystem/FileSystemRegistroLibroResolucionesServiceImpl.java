package services.filesystem;

import java.util.ArrayList;
import java.util.List;

import models.ResolucionFAP;

import org.joda.time.DateTime;

import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Resolucion;

import registroresolucion.AreaResolucion;
import registroresolucion.RecursoResolucion;
import registroresolucion.RegistroResolucion;
import registroresolucion.TipoResolucion;
import services.RegistroLibroResolucionesService;
import services.RegistroLibroResolucionesServiceException;

public class FileSystemRegistroLibroResolucionesServiceImpl implements RegistroLibroResolucionesService {

	public boolean isConfigured() {
		// No necesita configuración
		return true;
	}

	@Override
	public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Registro de Libro de Resoluciones ha sido inyectado con FileSystem y está operativo.");
		else
			play.Logger.info("El servicio de Registro de Libro de Resoluciones ha sido inyectado con FileSystem y NO está operativo.");
	}

	@Override
	public RegistroResolucion crearResolucion(ResolucionFAP fapResolucion) {
		return new RegistroResolucion(1, 1, 3, new DateTime());
	}

	@Override
	public List<TipoResolucion> leerTipos() throws RegistroLibroResolucionesServiceException {
		List<TipoResolucion> tipos = new ArrayList<TipoResolucion>();
		TipoResolucion tipo = new TipoResolucion(1l, "ABC", "Abono de Cursos");
		tipos.add(tipo);
		return tipos;
	}

	@Override
	public List<AreaResolucion> leerAreas() throws RegistroLibroResolucionesServiceException {
		List<AreaResolucion> areas = new ArrayList<AreaResolucion>();
		AreaResolucion area = new AreaResolucion(1l, "AG", "Asuntos Generales");
		areas.add(area);
		return areas;
	}

	@Override
	public List<RecursoResolucion> leerRecursos() throws RegistroLibroResolucionesServiceException {
		List<RecursoResolucion> recursos = new ArrayList<RecursoResolucion>();
		RecursoResolucion recurso = new RecursoResolucion(1l, "ALE", "Alegaciones");
		recursos.add(recurso);
		return recursos;
	}

}
