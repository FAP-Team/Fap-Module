package services.BDOrganizacion;

import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;
import es.gobcan.platino.servicios.organizacion.DatosBasicosPersonaItem;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaCriteriaItem;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaItem;

public class FileSystemBDOrganizacionServiceImpl implements BDOrganizacionService{

	@Override
	public boolean isConfigured() {
		return true;
	}

	@Override
	public void mostrarInfoInyeccion() {
		play.Logger.info("El servicio de DBOrganizacion ha sido inyectado con FileSystem y está operativo.");
	}

	@Override
	public String recuperarURIPersona(String uid)
			throws DBOrganizacionException_Exception {

		return "platino://gobcan.es/servicios/organizacion/funcionario/0000000_LUKE_00000000";
	}

	@Override
	public DatosBasicosPersonaItem recuperarDatosPersona(String uri)
			throws DBOrganizacionException_Exception {
		
		DatosBasicosPersonaItem datosPersona = null;
		if (uri.equals("platino://gobcan.es/servicios/organizacion/funcionario/0000000_LUKE_00000000")){
			datosPersona = new DatosBasicosPersonaItem();
			datosPersona.setNombre("Luke SkyWalker");
			datosPersona.setUid("luke");
			datosPersona.setUri("platino://gobcan.es/servicios/organizacion/funcionario/0000000_LUKE_00000000");
		}
		return datosPersona;
	}

	@Override
	public List<UnidadOrganicaItem> buscarUnidadesPorCampos(
			UnidadOrganicaCriteriaItem campos)
			throws DBOrganizacionException_Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UnidadOrganicaItem> buscarUnidadesPorConsulta(String consulta)
			throws DBOrganizacionException_Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UnidadOrganicaItem> consultarPertenenciaUnidad(
			String uriFuncionario, XMLGregorianCalendar fecha)
			throws DBOrganizacionException_Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UnidadOrganicaItem consultaDetalladaDeUnidad(String uriUO,
			XMLGregorianCalendar fecha)
			throws DBOrganizacionException_Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> consultarPersonalAdscritoAUnidad(String uriUO,
			XMLGregorianCalendar fecha)
			throws DBOrganizacionException_Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
