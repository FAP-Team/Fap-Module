package platino;

import java.util.List;

import validation.NipCheck;

import net.java.dev.jaxb.array.StringArray;

public class InfoCert {
	public String nombrecompleto;
	public String nombre;
	public String fullname;
	public String entidad;
	public String apellido1;
	public String apellido2;
	public String apellidos;
	public String nif;
	public String cif;
	public String tipo;
	public String email;
	public String cargo;
	public String departamento;
	public String finalidad;
	public String organizacion;
	public String serialNumber;
	public String issuer;
	public String subject;
	public String notBefore;
	public String notAfter;
	
	public InfoCert(){}
	
	public InfoCert(List<StringArray> certInfo){
		if (certInfo != null) {
			String datosCertificado="[";
			for (StringArray array : certInfo) {
				String key = array.getItem().get(0).trim();
				datosCertificado+=key+", "+array.getItem().get(1).trim()+" | ";
				if (key.toLowerCase().equals("tipo")) {
					if ("pj".equals(array.getItem().get(1).toLowerCase().trim()))
						tipo = "personajuridica";
					else // pf, pv o rep
						tipo = "personafisica";
				} else if(key.toLowerCase().equals("nif")){
					nif = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("cif")){
					cif = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("apellidos")){
					apellidos = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("apellido1")){
					apellido1 = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("apellido2")){
					apellido2 = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("nombrecompleto")){
					nombrecompleto = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("entidad")){
					entidad = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("nombre")){
					nombre = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("fullname")){
					fullname = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("email")){
					email = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("cargo")){
					cargo = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("departamento")){
					departamento = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("finalidad")){
					finalidad = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("organizacion")){
					organizacion = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("serialnumber")){
					serialNumber = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("issuer")){
					issuer = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("subject")){
					subject = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("notbefore")){
					notBefore = array.getItem().get(1).trim();
				} else if(key.toLowerCase().equals("notafter")){
					notAfter = array.getItem().get(1).trim();
				}
			}
			datosCertificado+="]";
			play.Logger.info("Certificado Leído: "+datosCertificado);
		}
	}
	
	public String getNombreCompleto(){
		if (("personajuridica".equals(tipo)) && (entidad != null)) return entidad;
		if(nombrecompleto != null) return nombrecompleto;
		if(apellidos != null) return nombre + " " + apellidos;
		String out = nombre + " " + apellido1;
		if(apellido2 != null) out += " " + apellido2;
		return out;
	}
	
	public String getFinalidad(){
		if (finalidad == null) return "";
		if (finalidad.equalsIgnoreCase("f")) return "firma";
		else if (finalidad.equalsIgnoreCase("a")) return "autenticacion";
		else if (finalidad.equalsIgnoreCase("fa")) return "firmaryautenticacion";
		return "";
	}

	@Override
	public String toString() {
		return "InfoCertUsada [IdTipo: "+getIdTipo()+", Id: "+getId()+", Name: "+getNombreCompleto()+"]\nInfoCertTotal [nombrecompleto=" + nombrecompleto + ", nombre="
				+ nombre + ", apellido1=" + apellido1 + ", apellido2="
				+ apellido2 + ", apellidos=" + apellidos + ", nif=" + nif
				+ ", cif=" + cif + ", entidad=" + entidad + "]";
	}
	
	public String getId(){
		if (tipo != null && !tipo.isEmpty()) {
			if (tipo.equals("personajuridica"))
				return cif;
			else
				return nif;
		}
		if(cif != null) return cif;
		return nif;
	}
	
	public String getIdTipo(){
		if (tipo != null && !tipo.isEmpty()) {
			if (tipo.equals("personajuridica"))
				return "cif";
			else
				return "nif";
		}
		if(cif != null) return "cif";
		return "nif";
	}
	
}
