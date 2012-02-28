package platino;

import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import utils.WSUtils;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firma;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firmante;

public class DatosDocumento {
	private XMLGregorianCalendar fecha;
	private String tipoDoc;
	private String descripcion;
	private String tipoMime;
	private boolean admiteVersionado = false;
	private List<DatosFirmante> firmantes;
	private String firmaXml;
	private String uriPlatino = null;
	private DataSource contenido;
	
	public List<DatosFirmante> getFirmantes() {
		return firmantes;
	}
	public void setFirmantes(List<DatosFirmante> firmantes) {
		this.firmantes = firmantes;
	}

	public String getUriPlatino() {
		return uriPlatino;
	}
	public void setUriPlatino(String uriPlatino) {
		this.uriPlatino = uriPlatino;
	}
	public DataSource getContenido() {
		return contenido;
	}
	public void setContenido(DataSource contenido) {
		this.contenido = contenido;
	}
	public XMLGregorianCalendar getFecha() {
		return fecha;
	}
	public void setFecha(XMLGregorianCalendar fecha) {
		this.fecha = fecha;
	}
	public String getTipoDoc() {
		return tipoDoc;
	}
	public void setTipoDoc(String tipoDoc) {
		this.tipoDoc = tipoDoc;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getTipoMime() {
		return tipoMime;
	}
	public void setTipoMime(String tipoMime) {
		this.tipoMime = tipoMime;
	}
	public boolean isAdmiteVersionado() {
		return admiteVersionado;
	}
	public void setAdmiteVersionado(boolean admiteVersionado) {
		this.admiteVersionado = admiteVersionado;
	}
	public String getFirmaXml() {
		return firmaXml;
	}
	public void setFirmaXml(String firmaXml) {
		this.firmaXml = firmaXml;
	}

	public void setFirma(models.Firma firma){
	    this.firmaXml = firma.getContenido();
     	firmantes = new ArrayList<DatosFirmante>();
     	if(firma.getFirmantes() != null){
    	    for (models.Firmante firmante : firma.getFirmantes()) {
                DatosFirmante datFirm = new DatosFirmante();
                datFirm.setIdFirmante(firmante.idvalor);
                datFirm.setDescFirmante(firmante.nombre);
                datFirm.setFechaFirma(WSUtils.getXmlGregorianCalendar(firmante.fechaFirma));
                
                // TODO: Cambiar cuando se use BD de terceros platino
                datFirm.setCargoFirmante("Solicitante");
                datFirm.setUriFirmante("URITest");
                firmantes.add(datFirm);
    	    }
     	}
	}
	
}
