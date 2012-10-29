package utils;

import java.util.List;

import javax.xml.datatype.DatatypeConstants;

import models.Solicitante;

import org.joda.time.DateTime;

import config.InjectorConfig;

import services.TercerosService;
import services.TercerosServiceException;
import es.gobcan.platino.servicios.localizaciones.IslaItem;
import es.gobcan.platino.servicios.localizaciones.MunicipioItem;
import es.gobcan.platino.servicios.localizaciones.PaisItem;
import es.gobcan.platino.servicios.localizaciones.ProvinciaItem;
import es.gobcan.platino.servicios.terceros.DomicilioItem;
import es.gobcan.platino.servicios.terceros.EmailItem;
import es.gobcan.platino.servicios.terceros.TelefonoItem;
import es.gobcan.platino.servicios.terceros.TerceroItem;
import es.gobcan.platino.servicios.terceros.TerceroListItem;
import es.gobcan.platino.servicios.terceros.TerceroMinimalItem;
import es.gobcan.platino.servicios.terceros.TipoDocumentoItem;
import es.gobcan.platino.servicios.terceros.TipoTerceroItem;

public class TercerosUtils {

	private static boolean isValid (String campo){
		if ((campo != null) && (!campo.isEmpty()))
			return true;
		return false;
	}
	
	public static String convertirSolicitanteAJS(Solicitante solicitante){
		String ret = "{";
		if (solicitante != null){
			String tipoDireccion=null;
			if (solicitante.isPersonaFisica()){
				if (isValid(solicitante.fisica.nombre))
					ret+="nombre%->%"+solicitante.fisica.nombre+"%,%";
				if (isValid(solicitante.fisica.primerApellido))
					ret+="primerApellido%->%"+solicitante.fisica.primerApellido+"%,%";
				if (isValid(solicitante.fisica.segundoApellido))
					ret+="segundoApellido%->%"+solicitante.fisica.segundoApellido+"%,%";
				if (isValid(solicitante.fisica.sexo))
					ret+="sexo%->%"+solicitante.fisica.sexo+"%,%";
				if ((solicitante.fisica.fechaNacimiento!= null) && (isValid(solicitante.fisica.fechaNacimiento.toString("dd/MM/yy"))))
					ret+="fechaNacimiento%->%"+solicitante.fisica.fechaNacimiento.toString("dd/MM/yy")+"%,%";
			} else {
				if (isValid(solicitante.juridica.entidad))
					ret+="entidad%->%"+solicitante.juridica.entidad+"%,%";
			}
			if (isValid(solicitante.email))
				ret+="email%->%"+solicitante.email+"%,%";
			if (isValid(solicitante.telefonoContacto))
				ret+="telefonoContacto%->%"+solicitante.telefonoContacto+"%,%";
			// Direccion
			if (isValid(solicitante.domicilio.calle))
				ret+="%&%calle%->%"+solicitante.domicilio.calle+"%,%";
			if (isValid(solicitante.domicilio.numero))
				ret+="%&%numero%->%"+solicitante.domicilio.numero+"%,%";	
			if (isValid(solicitante.domicilio.otros))
				ret+="%&%otros%->%"+solicitante.domicilio.otros+"%,%";
			if (isValid(solicitante.domicilio.codigoPostal))
				ret+="%&%codigoPostal%->%"+solicitante.domicilio.codigoPostal+"%,%";
			if ((!isValid(solicitante.domicilio.isla)) && (isValid(solicitante.domicilio.provinciaInternacional))){
				if (isValid(solicitante.domicilio.pais))
					ret+="%&%pais%->%"+solicitante.domicilio.pais+"%,%";
			}
			if ((!isValid(solicitante.domicilio.isla)) && (!isValid(solicitante.domicilio.provinciaInternacional))){
				if (isValid(solicitante.domicilio.comunidad))
					ret+="%&%comunidad%->%"+solicitante.domicilio.comunidad+"%,%";
			}
			if (!isValid(solicitante.domicilio.isla)){
				if (isValid(solicitante.domicilio.provinciaInternacional)){
					ret+="%&%provinciaInternacional%->%"+solicitante.domicilio.provinciaInternacional+"%,%";
					if (tipoDireccion == null)
						tipoDireccion = "internacional";
				}
			}
			if (isValid(solicitante.domicilio.isla)){
				if (isValid(solicitante.domicilio.provincia))
					ret+="%&%provinciaIsla%->%"+solicitante.domicilio.provincia+"%,%";
			} else if (!isValid(solicitante.domicilio.provinciaInternacional)){
				if (isValid(solicitante.domicilio.provincia))
					ret+="%&%provincia%->%"+solicitante.domicilio.provincia+"%,%";
			}
			if (isValid(solicitante.domicilio.isla)){
				ret+="%&%isla%->%"+solicitante.domicilio.isla+"%,%";
				if (tipoDireccion == null)
					tipoDireccion="canaria";
			}
			if (isValid(solicitante.domicilio.isla)){
				if (isValid(solicitante.domicilio.municipio))
					ret+="%&%municipioIsla%->%"+solicitante.domicilio.municipio+"%,%";
			} else if (!isValid(solicitante.domicilio.provinciaInternacional)){
				if (isValid(solicitante.domicilio.municipio))
					ret+="%&%municipio%->%"+solicitante.domicilio.municipio+"%,%";
			}
			
			if (tipoDireccion == null)
				tipoDireccion = "nacional";
			
			ret+="%&%tipo%->%"+tipoDireccion;
		}
		ret += "}";
		return ret;
	}
	
}
