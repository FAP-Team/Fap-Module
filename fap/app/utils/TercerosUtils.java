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
	
	public static TerceroMinimalItem convertirSolicitanteATerceroMinimal (Solicitante solicitante){
		if (solicitante != null) {
			TerceroMinimalItem tercero = new TerceroMinimalItem();
			tercero.setNumeroDocumento(solicitante.getNumeroId());
			if (solicitante.isPersonaFisica()){
				tercero.setTipoDocumento(convertirTipoNipATipoDocumentoItem(solicitante.fisica.nip.tipo));
				TipoTerceroItem tipo = new TipoTerceroItem();
				tipo.setId("FISICO");
				tercero.setTipoTercero(tipo);
				tercero.setNombre(solicitante.fisica.nombre);
				tercero.setApellido1(solicitante.fisica.primerApellido);
				if (solicitante.fisica.segundoApellido != null) {
					tercero.setApellido2(solicitante.fisica.segundoApellido);
				}
			} else {
				tercero.setTipoDocumento(convertirTipoNipATipoDocumentoItem("cif"));
				TipoTerceroItem tipo = new TipoTerceroItem();
				tipo.setId("JURIDICO");
				tercero.setTipoTercero(tipo);
				tercero.setNombre(solicitante.juridica.entidad);
			}
			tercero.setFechaCreacion(XMLGregorianCalendarConverter.asXMLGregorianCalendar(new DateTime()));
			return tercero;
		} else {
			return null;
		}
	}

	public static TipoDocumentoItem convertirTipoNipATipoDocumentoItem (String tipoNipCif){
		TipoDocumentoItem ret = new TipoDocumentoItem();
		if ("nif".equals(tipoNipCif)){
			ret.setId("NIF");
		} else if ("nie".equals(tipoNipCif)){
			ret.setId("NIE");
		} else if ("cif".equals(tipoNipCif)){ 
			ret.setId("CIF");
		} else { // Pasaporte
			ret.setId("PASAPORTE");
		}
		return ret;
	}

	/**
	 * Método para mapear los datos de tercero del objeto TerceroItem a la clase Solicitante de FAP
	 * @param tercero 
	 * @return Solicitante Devuelve un objeto solicitante(FAP)
	 */

	public static Solicitante convertirTerceroASolicitante(TerceroItem tercero) throws TercerosServiceException{

		Solicitante  s= null;
		if(tercero!=null){
			s = new Solicitante();

			if(tercero.getTipoTercero().getId().equalsIgnoreCase("JURIDICO")||
					tercero.getTipoTercero().getDescripcion().equalsIgnoreCase("ORGANISMO")){
				s.tipo="juridica";
				s.juridica.cif = tercero.getNumeroDocumento();
				s.juridica.entidad = tercero.getNombre();
				if(tercero.getEmails()!=null &&tercero.getEmails().size()>0)
				{	

					EmailItem correo = buscarCorreoPrincipal(tercero.getEmails());
					if(correo!=null){
						s.juridica.email = correo.getDireccion();
					}
				}

			}
			if(tercero.getTipoTercero().getId().equalsIgnoreCase("FISICO")){
				s.tipo="fisica";
				if(tercero.getTipoDocumento()!=null)
					s.fisica.nip.tipo = tercero.getTipoDocumento().getId();
				s.fisica.nip.valor=tercero.getNumeroDocumento();
				s.fisica.nombre = tercero.getNombre();
				s.fisica.primerApellido = tercero.getApellido1();
				s.fisica.segundoApellido = tercero.getApellido2();
			}
			if(tercero.getEmails()!=null &&tercero.getEmails().size()>0)
			{	
				EmailItem correo = buscarCorreoPrincipal(tercero.getEmails());
				if(correo!=null)
					s.email = correo.getDireccion();
			}

			//EXISTEN LA POSIBILIDAD DE RECIBIR HASTA TRES DOMICILIOS DE LA BASE DE DATOS DE TERCERO SEGÚN
			//EL MODELO DE GESTIÓN DE LA BASE DE DATOS DE TERCEROS. SE ASOCIARÁ SOLAMENTE EL MARCADO COMO PRINCIPAL.
			//En cualquier caso FAP solo admite uno por tipo en el objeto de la clase
			//solicitante
			if(tercero.getDomicilios()!=null && tercero.getDomicilios().size()>0){
				for(int i=0;i<tercero.getDomicilios().size();i++){
					DomicilioItem d = tercero.getDomicilios().get(0);
					if(d.isPrincipal()){
						s.domicilio.calle=d.getVia();
						s.domicilio.codigoPostal=d.getCodigoPostal();
						TercerosService terceros = InjectorConfig.getInjector().getInstance(TercerosService.class);
						MunicipioItem mun = terceros.recuperarMunicipio(d.getIdProvincia(), d.getIdMunicipio());
						if(mun!=null)
							s.domicilio.municipio="_"+getCodigoMuncipioFapFromTerceros(mun.getIdProvincia(), mun.getId(), mun.getDigitoControl());
						s.domicilio.numero=d.getPortal();
						s.domicilio.otros=d.getOtros();
						PaisItem pais = terceros.recuperarPais(d.getIdPais());
						if(pais!=null)
							s.domicilio.pais=pais.getLiteral(); // TODO:
						ProvinciaItem prov = terceros.recuperarProvincia(d.getIdProvincia());
						if(prov!=null){
							s.domicilio.provincia="_"+getCodigoProvinciaFapFromTerceros(prov.getId());
							s.domicilio.comunidad="_"+convertirProvinciaAComunidadAutonoma(getCodigoProvinciaFapFromTerceros(prov.getId()));
						}
						s.domicilio.provinciaInternacional=d.getEstado();
						IslaItem isla = terceros.recuperarIsla(d.getIdIsla());
						if (isla!=null)
							s.domicilio.isla=isla.getLiteral(); // TODO:
					}
				}
			}

			//Existe posibilidad de recibir varios número de teléfono si tener claro cuantos de cada tipo puede almacenarse
			//en la base de datos de tercero. En cualquier caso FAP solo admite uno por tipo en el objeto de la clase
			//solicitante
			if(tercero.getTelefonos()!=null && tercero.getTelefonos().size()>0){
				TelefonoItem tfno = buscarTfnoPrincipalRegistrado(tercero.getTelefonos(),"FIJO");
				String tfnoContacto = null;
				if(tfno!=null){
					s.telefonoFijo = tfno.getNumero();
					tfnoContacto = tfno.getNumero();
				}

				TelefonoItem tfnoMovil = buscarTfnoPrincipalRegistrado(tercero.getTelefonos(),"MOVIL");
				if(tfnoMovil!=null){
					s.telefonoMovil = tfnoMovil.getNumero();
					if (tfnoContacto == null)
						tfnoContacto = tfnoMovil.getNumero();
				}

				TelefonoItem fax = buscarTfnoPrincipalRegistrado(tercero.getTelefonos(),"FAX");
				if(fax!=null)
					s.fax = fax.getNumero();

			}
		}

		return s;
	}

	private static EmailItem buscarCorreoPrincipal(List<EmailItem> emails) {
		EmailItem correo = null;
		if(emails!=null){
			for(int i=0;i<emails.size();i++){
				if(emails.get(i).isPrincipal()){
					if((correo!=null && correo.getFechaActualizacion().compare(emails.get(i).getFechaActualizacion())== DatatypeConstants.LESSER)||
							(correo!=null && !correo.isPrincipal())){
						correo = emails.get(i);
					}else{
						if(correo==null){
							correo= emails.get(i);
						}
					}

				}else{
					if(correo==null)
						correo = emails.get(i);
				}
			}
		}
		return correo;
	}


	private static TelefonoItem buscarTfnoPrincipalRegistrado (List<TelefonoItem> telefonos,String tipo) {
		TelefonoItem tfno = null;
		if(telefonos!=null){
			for(int i=0;i<telefonos.size();i++){

				if(telefonos.get(i).getTipo().getId().equalsIgnoreCase(tipo)&& telefonos.get(i).isPrincipal()){
					if((tfno!=null && tfno.getFechaActualizacion().compare(telefonos.get(i).getFechaActualizacion())== DatatypeConstants.LESSER)||
							(tfno!=null && !tfno.isPrincipal())){
						tfno= telefonos.get(i);
					}else{
						if(tfno==null){
							tfno= telefonos.get(i);
						}
					}
				}
				else{
					if(tfno==null && telefonos.get(i).getTipo().getId().equalsIgnoreCase(tipo))
						tfno = telefonos.get(i);
				}
			}
		}
		return tfno;
	}
	
	public static TerceroItem convertirTerceroListItemATerceroItem(TerceroListItem tercero) {
		TerceroItem ret = new TerceroItem();
		ret.setApellido1(tercero.getApellido1());
		ret.setApellido2(tercero.getApellido2());
		ret.setFechaNacimiento(tercero.getFechaNacimiento());
		ret.setNombre(tercero.getNombre());
		ret.setTipoDocumento(tercero.getTipoDocumento());
		ret.setSexo(tercero.getSexo());
		ret.setTipoTercero(tercero.getTipoTercero());
		ret.setNumeroDocumento(tercero.getNumeroDocumento());
		return ret;
	}
	
	public static String getCodigoMuncipioFapFromTerceros (Long idProvincia, Long idMunicipio, String idCodigoControl){
		String ret="";
		ret = String.format("%02d", idProvincia)+String.format("%03d", idMunicipio)+idCodigoControl;
		return ret;
	}
	
	public static String getCodigoProvinciaFapFromTerceros (Long idProvincia){
		String ret="";
		ret = String.format("%02d", idProvincia);
		return ret;
	}
	
	public static String convertirProvinciaAComunidadAutonoma(String idProvincia){
		if (idProvincia == null)
			return null;
		if (idProvincia.equals("04") || idProvincia.equals("11") || idProvincia.equals("14") || idProvincia.equals("18") || idProvincia.equals("21") || idProvincia.equals("23") || idProvincia.equals("29") || idProvincia.equals("41"))
			return "01";
		else if (idProvincia.equals("22") || idProvincia.equals("44") || idProvincia.equals("50"))
			return "02";
		else if (idProvincia.equals("33"))
			return "03";
		else if (idProvincia.equals("07"))
			return "04";
		else if (idProvincia.equals("35") || idProvincia.equals("38"))
			return "05";
		else if (idProvincia.equals("39"))
			return "06";
		else if (idProvincia.equals("05") || idProvincia.equals("09") || idProvincia.equals("24") || idProvincia.equals("34") || idProvincia.equals("37") || idProvincia.equals("40") || idProvincia.equals("42") || idProvincia.equals("47") || idProvincia.equals("49"))
			return "07";
		else if (idProvincia.equals("02") || idProvincia.equals("13") || idProvincia.equals("16") || idProvincia.equals("19") || idProvincia.equals("45"))
			return "08";
		else if (idProvincia.equals("08") || idProvincia.equals("17") || idProvincia.equals("25") || idProvincia.equals("43"))
			return "09";
		else if (idProvincia.equals("03") || idProvincia.equals("12") || idProvincia.equals("46"))
			return "10";
		else if (idProvincia.equals("06") || idProvincia.equals("10"))
			return "11";
		else if (idProvincia.equals("15") || idProvincia.equals("27") || idProvincia.equals("32") || idProvincia.equals("36"))
			return "12";
		else if (idProvincia.equals("28"))
			return "13";
		else if (idProvincia.equals("30"))
			return "14";
		else if (idProvincia.equals("31"))
			return "15";
		else if (idProvincia.equals("01") || idProvincia.equals("20") || idProvincia.equals("48"))
			return "16";
		else if (idProvincia.equals("26"))
			return "17";
		else if (idProvincia.equals("51"))
			return "18";
		else if (idProvincia.equals("52"))
			return "19";
		return null;
	}
	
	public static String getCodigoPaisFapFromTerceros (String idPais){
		if (idPais == null)
			return null;
		if (idPais.equals("64")) // España
			return "724";
		return null;
	}
}
