package platino;

import java.util.List;

import net.java.dev.jaxb.array.StringArray;

public class InfoCert {
	public String nombrecompleto;
	public String nombre;
	public String apellido1;
	public String apellido2;
	public String apellidos;
	public String nif;
	public String cif;
	public String tipo;
	
	public InfoCert(){}
	
	public InfoCert(List<StringArray> certInfo){
		if (certInfo != null) {
			for (StringArray array : certInfo) {
				String key = array.getItem().get(0);
				if (key.toLowerCase().equals("pj")) {
					tipo = "personajuridica";
				} else if (key.toLowerCase().equals("pf")) {
					tipo = "personafisica";
				} else if(key.toLowerCase().equals("nif")){
					nif = array.getItem().get(1);
				} else if(key.toLowerCase().equals("cif")){
					cif = array.getItem().get(1);
				} else if(key.toLowerCase().equals("apellidos")){
					apellidos = array.getItem().get(1);
				} else if(key.toLowerCase().equals("apellido1")){
					apellido1 = array.getItem().get(1);
				} else if(key.toLowerCase().equals("apellido2")){
					apellido2 = array.getItem().get(1);
				} else if(key.toLowerCase().equals("nombrecompleto")){
					nombrecompleto = array.getItem().get(1);
				}
			}
		}
	}
	
	public String getNombreCompleto(){
		if(nombrecompleto != null) return nombrecompleto;
		if(apellidos != null) return nombre + " " + apellidos;
		String out = nombre + " " + apellido1;
		if(apellido2 != null) out += " " + apellido2;
		return out;
	}

	@Override
	public String toString() {
		return "InfoCert [nombrecompleto=" + nombrecompleto + ", nombre="
				+ nombre + ", apellido1=" + apellido1 + ", apellido2="
				+ apellido2 + ", apellidos=" + apellidos + ", nif=" + nif
				+ ", cif=" + cif + ", getNombreCompleto()="
				+ getNombreCompleto() + "]";
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
	
}
