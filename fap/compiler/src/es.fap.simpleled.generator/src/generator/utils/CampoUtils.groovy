package generator.utils;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.CampoAtributos;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.LedFactory;
import es.fap.simpleled.led.impl.LedFactoryImpl;
import java.util.regex.Matcher
import java.util.regex.Pattern

public class CampoUtils implements Comparable{

	Campo campo;
	String str;
	
	public static CampoUtils create(Campo campo){
		if (campo == null){
			return null;
		}
		CampoUtils field = new CampoUtils();
		field.campo = campo;
		field.str = getCampoStr(campo);
		return field;
	}
	
	public static CampoUtils create(Entity entity){
		if (entity == null){
			return null;
		}
		CampoUtils campo = new CampoUtils();
		LedFactory factory = new LedFactoryImpl();
		campo.campo = factory.createCampo();
		campo.campo.setEntidad(entity);
		campo.campo.setAtributos(null);
		campo.str = getCampoStr(campo.campo);
		return campo;
	}
	
	public static CampoUtils create(String campoStr){
		String entidad = entidadRaiz(campoStr);
		List<String> atributos = sinEntidad(campoStr).split("\\.");
		if (atributos.get(0).equals("")){
			atributos.clear();
		}
		Entity entity = LedUtils.getNode(Entity, entidad);
		if (entity == null){
			if (entidad.equals("Solicitud")){
				entity = LedUtils.findSolicitud();
			}
			else{
				return null;
			}
		}
		LedFactory factory = new LedFactoryImpl();
		Campo campoResult = factory.createCampo();
		campoResult.setEntidad(entity);
		if (atributos.size() == 0){
			return CampoUtils.create(campoResult);
		}
		CampoAtributos attrsResult = factory.createCampoAtributos();
		campoResult.setAtributos(attrsResult);
		for (int i = 0; i < atributos.size(); i++){
			String atributo = atributos.get(i);
			if (entity == null){
				return null;
			}
			Attribute attr = LedEntidadUtils.getAttribute(entity, atributo);
			if (attr == null){
				return null;
			}
			entity = attr.getType().getCompound()?.getEntidad();
			attrsResult.setAtributo(attr);
			if (i < atributos.size() - 1){
				if (LedEntidadUtils.xToMany(attr)){
					return null;
				}
				attrsResult.setAtributos(factory.createCampoAtributos());
				attrsResult = attrsResult.getAtributos();
			}
		}
		return CampoUtils.create(campoResult);
	}
	
	public static CampoUtils create(Campo campo, Attribute attr){
		return create(addAttribute(campo, attr));
	}
	
	public static CampoUtils create(Campo campo, String more){
		return create(addMore(campo, more));
	}
	
	public Entity getEntidad(){
		return campo?.getEntidad();
	}
	
	public static String getCampoStr(Campo campo){
		if (campo == null){
			return null;
		}
		String campoStr = campo.getEntidad().getName();
		if (campoStr.equals("SolicitudGenerica")){
			campoStr = "Solicitud";
		}
		CampoAtributos attrs = campo.getAtributos();
		while (attrs != null){
			campoStr += "." + attrs.getAtributo().getName();
			attrs = attrs.getAtributos();
		}
		return campoStr;
	}
	
	public String firstLower(){
		return StringUtils.firstLower(str);
	}
	
	public Attribute getUltimoAtributo(){
		return LedCampoUtils.getUltimoAtributo(campo);
	}
	
	public Entity getUltimaEntidad(){
		return LedCampoUtils.getUltimaEntidad(campo);
	}
	
	public static Campo addMore(Campo campo, String more){
		return CampoUtils.create(CampoUtils.create(campo).str + "." + more)?.campo;
	}
	
	public Campo addMore(String more){
		return CampoUtils.addMore(campo, more);
	}
	
	public Campo addAttribute(Attribute atributo){
		return CampoUtils.addAttribute(campo, atributo);
	}
	
	public static Campo addAttribute(Campo campo, Attribute atributo){
		return addMore(campo, atributo.name);
	}
	
	public String sinEntidad(){
		return sinEntidad(str);
	}
	
	@Override
	public boolean equals(Object campo){
		CampoUtils f = (CampoUtils)campo;
		return f.str.equals(str);
	}
	
	@Override
	public int compareTo(Object campo){
		CampoUtils f = (CampoUtils)campo;
		if (equals(f)){
			return 0;
		}
		return 1;
	}
	
	private static String entidadRaiz(String campoStr){
		def matcher = ( campoStr =~ /([^.]+).*/ )
		if (matcher.matches()) {
			return matcher[0][1];
		}
		return null;
	}
	
	private static String sinEntidad(String campoStr){
		int index = campoStr.findIndexOf{ it == '.' };
		if(index == -1){
			return "";
		}
		return campoStr.substring(campoStr.findIndexOf{ it == '.' } + 1)
	}
	
	public String getStr_() {
		return StringUtils.firstLower(str.replace('.', '_'));
	}
	
}
