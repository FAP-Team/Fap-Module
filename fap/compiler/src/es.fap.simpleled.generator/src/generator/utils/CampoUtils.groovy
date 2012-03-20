package generator.utils;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.CampoAtributos;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.LedFactory;
import es.fap.simpleled.led.Tabla
import es.fap.simpleled.led.util.ModelUtils;
import es.fap.simpleled.led.LedPackage;
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.Pattern.First;


public class CampoUtils implements Comparable{

	Campo campo;
	String str;
	
	public static CampoUtils create(Campo campo){
		if (campo == null) return null;
		CampoUtils field = new CampoUtils();
		field.campo = campo;
		field.str = getCampoStr(campo);
		return field;
	}
	
	public static CampoUtils create(Entity entity){
		if (entity == null) return null;
		CampoUtils campo = new CampoUtils();
		campo.campo = LedFactory.eINSTANCE.createCampo();
		campo.campo.setEntidad(entity);
		campo.campo.setAtributos(null);
		campo.str = getCampoStr(campo.campo);
		return campo;
	}
	
	public static CampoUtils create(String campoStr){
		String entidad = entidadRaiz(campoStr);
		List<String> atributos = sinEntidad(campoStr).split("\\.");
		if (atributos.get(0).equals(""))
			atributos.clear();
		Entity entity = ModelUtils.getVisibleNode(LedPackage.Literals.ENTITY, entidad, LedUtils.resource);
		if (entity == null){
			if (entidad.equals("Solicitud"))
				entity = EntidadUtils.findSolicitud();
			else return null;
		}
		Campo campoResult = LedFactory.eINSTANCE.createCampo();
		campoResult.setEntidad(entity);
		if (atributos.size() == 0)
			return CampoUtils.create(campoResult);
		CampoAtributos attrsResult = LedFactory.eINSTANCE.createCampoAtributos();
		campoResult.setAtributos(attrsResult);
		for (int i = 0; i < atributos.size(); i++){
			String atributo = atributos.get(i);
			if (entity == null) return null;
			Attribute attr = LedEntidadUtils.getAttribute(entity, atributo);
			if (attr == null) return null;
			entity = attr.getType().getCompound()?.getEntidad();
			attrsResult.setAtributo(attr);
			if (i < atributos.size() - 1){
				if (LedEntidadUtils.xToMany(attr)) return null;
				attrsResult.setAtributos(LedFactory.eINSTANCE.createCampoAtributos());
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
	
	
	public boolean isMethod(){
		return campo.method != null;
	}
	
	public static String getCampoStr(Campo campo){
		if (campo == null) return null;
		if (campo.method) return campo.method;
		String campoStr = campo.getEntidad().getName();
		if (campoStr.equals("SolicitudGenerica"))
			campoStr = "Solicitud";
		CampoAtributos attrs = campo.getAtributos();
		while (attrs != null){
			campoStr += "." + attrs.getAtributo().getName();
			attrs = attrs.getAtributos();
		}
		return campoStr;
	}
	
	public String dbStr(){
		return "db" + str;
	}
	
	public String firstLower(){
		if (campo.method) return str;
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
	
	public boolean simple(){
		return campo.getAtributos() == null;
	}
	
	/*
	 * Solicitud.documento ----> solicitud?.documento?.id
	 */
	public String idWithNullCheck(){
		return CampoUtils.withNullCheck(firstLower() + '.id');
	}
	
	public static boolean hayCamposGuardables(Object o){
		if(o.metaClass.respondsTo(o,"getElementos")){
			for (Object elemento: o.elementos){
				if (hayCamposGuardables(elemento))
					return true;
			}
			return false;
		}
		if(o.metaClass.respondsTo(o,"getCampo")){
			if(! (o instanceof Tabla))
				return true;
		}
	}
	
	/*
	 * solicitud.documento.uri ----> solicitud?.documento?.uri
	 */
	public static String withNullCheck(String campo){
		String[] segmentos = campo.split("\\.");
		String result = "";
		for (int i = 0; i < segmentos.length - 1; i++)
			result += segmentos[i] + "?.";
		return result + segmentos[segmentos.length - 1];
	}
	
	@Override
	public boolean equals(Object campo){
		CampoUtils f = (CampoUtils)campo;
		return f.str.equals(str);
	}
	
	@Override
	public int compareTo(Object campo){
		CampoUtils f = (CampoUtils)campo;
		if (equals(f)) return 0;
		return 1;
	}
	
	private static String entidadRaiz(String campoStr){
		def matcher = ( campoStr =~ /([^.]+).*/ )
		if (matcher.matches()) return matcher[0][1];
		return null;
	}
	
	private static String sinEntidad(String campoStr){
		int index = campoStr.findIndexOf{ it == '.' };
		if(index == -1) return "";
		return campoStr.substring(campoStr.findIndexOf{ it == '.' } + 1)
	}
	
	public String sinUltimoAtributo(){
		int last = str.lastIndexOf('.');
		if (last == -1) return StringUtils.firstLower(str);
		return StringUtils.firstLower(str).substring(0, last);
	}

	
	public String getStr_(){
		return StringUtils.firstLower(str.replace('.', '_'));
	}
	
}
