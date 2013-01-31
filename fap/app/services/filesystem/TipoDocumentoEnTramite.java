package services.filesystem;

import es.gobcan.eadmon.procedimientos.ws.dominio.AportadoPorEnum;
import es.gobcan.eadmon.procedimientos.ws.dominio.CardinalidadEnum;
import es.gobcan.eadmon.procedimientos.ws.dominio.ObligatoriedadEnum;

import java.util.ArrayList;
import java.util.List;

import models.TipoDocumento;

public class TipoDocumentoEnTramite {
	
	AportadoPorEnum aportadoPor;
	
	CardinalidadEnum cardinalidad;
	
	String identificador;
	
	public String getIdentificador() {
		return identificador;
	}

	public void setIdentificador(String identificador) {
		this.identificador = identificador;
	}


	ObligatoriedadEnum obligatoriedad;
	
	String uri;
	
	Integer version;
	
	
	public AportadoPorEnum getAportadoPor() {
		return aportadoPor;
	}

	public void setAportadoPor(AportadoPorEnum aportadoPor) {
		this.aportadoPor = aportadoPor;
	}

	public CardinalidadEnum getCardinalidad() {
		return cardinalidad;
	}

	public void setCardinalidad(CardinalidadEnum cardinalidad) {
		this.cardinalidad = cardinalidad;
	}

	public ObligatoriedadEnum getObligatoriedad() {
		return obligatoriedad;
	}

	public void setObligatoriedad(ObligatoriedadEnum obligatoriedad) {
		this.obligatoriedad = obligatoriedad;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public static List<TipoDocumentoEnTramite> conversor2TipoDocumentoEnTramite (List<TipoDocumento> tipoDocumento){
		
		List<TipoDocumentoEnTramite> listaResult = new ArrayList<TipoDocumentoEnTramite>();
		for (TipoDocumento tipoDoc : tipoDocumento) {
			TipoDocumentoEnTramite result = new TipoDocumentoEnTramite();
			result.aportadoPor = AportadoPorEnum.fromValue(tipoDoc.aportadoPor);
			result.cardinalidad = CardinalidadEnum.fromValue(tipoDoc.cardinalidad);
			result.identificador = tipoDoc.nombre;
			result.obligatoriedad = ObligatoriedadEnum.valueOf(tipoDoc.obligatoriedad);
			result.uri = tipoDoc.uri;
			result.version = 1;
			listaResult.add(result);
		}
		
		return listaResult;
	}
	
	
	public static List<TipoDocumentoEnTramite> conversorTipoDocumentoEnTramite (List<es.gobcan.eadmon.procedimientos.ws.dominio.TipoDocumentoEnTramite> tipoDocumentoEnTramite){
		
		List<TipoDocumentoEnTramite> listaResult = new ArrayList<TipoDocumentoEnTramite>();
		
		for (es.gobcan.eadmon.procedimientos.ws.dominio.TipoDocumentoEnTramite tipoDoc : tipoDocumentoEnTramite) {
			TipoDocumentoEnTramite result = new TipoDocumentoEnTramite();
			result.aportadoPor = tipoDoc.getAportadoPor();
			result.cardinalidad = tipoDoc.getCardinalidad();
			result.identificador = tipoDoc.getIdentificador();
			result.obligatoriedad = tipoDoc.getObligatoriedad();
			result.uri = tipoDoc.getUri();
			result.version = tipoDoc.getVersion();
			listaResult.add(result);
		}
		
		return listaResult;
	}
	
}
