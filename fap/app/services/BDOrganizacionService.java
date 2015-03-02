package services;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;
import es.gobcan.platino.servicios.organizacion.DatosBasicosPersonaItem;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaCriteriaItem;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaItem;

public interface BDOrganizacionService {

    public boolean isConfigured();
    public void mostrarInfoInyeccion();
    public String recuperarURIPersona(String uid) throws DBOrganizacionException_Exception;
    public DatosBasicosPersonaItem recuperarDatosPersona(String uri) throws DBOrganizacionException_Exception;
    public List<UnidadOrganicaItem> buscarUnidadesPorCampos(UnidadOrganicaCriteriaItem campos) throws DBOrganizacionException_Exception;
    public List<UnidadOrganicaItem> buscarUnidadesPorConsulta(String consulta) throws DBOrganizacionException_Exception;
    public List<UnidadOrganicaItem> consultarPertenenciaUnidad(String uriFuncionario, XMLGregorianCalendar fecha) throws DBOrganizacionException_Exception;
    public UnidadOrganicaItem consultaDetalladaDeUnidad(String uriUO, XMLGregorianCalendar fecha) throws DBOrganizacionException_Exception;
    public List<String> consultarPersonalAdscritoAUnidad(String uriUO, XMLGregorianCalendar fecha) throws DBOrganizacionException_Exception;
}
