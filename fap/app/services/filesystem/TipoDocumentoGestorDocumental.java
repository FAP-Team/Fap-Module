package services.filesystem;

import java.util.ArrayList;
import java.util.List;

import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;

public class TipoDocumentoGestorDocumental {

	String uri;
	
	String descripcion;
	
	Integer version;
	
	String etiqueta;
	
		
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getEtiqueta() {
		return etiqueta;
	}

	public void setEtiqueta(String etiqueta) {
		this.etiqueta = etiqueta;
	}
	

	public static List<TipoDocumentoGestorDocumental> ConversorTipoDocumento(List<TipoDocumento> lista){
		List<TipoDocumentoGestorDocumental> listaResult = new ArrayList<TipoDocumentoGestorDocumental>();
		for (TipoDocumento tipoDoc : lista) {
			TipoDocumentoGestorDocumental result = new TipoDocumentoGestorDocumental();
			result.uri = tipoDoc.getUri();
			result.descripcion = tipoDoc.getDescripcion();
			result.version = tipoDoc.getVersion();
			result.etiqueta = tipoDoc.getEtiqueta();
			listaResult.add(result);
		}

		return listaResult;
	}
	
}
