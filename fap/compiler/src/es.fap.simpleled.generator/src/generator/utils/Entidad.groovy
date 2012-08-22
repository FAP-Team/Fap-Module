package generator.utils;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Campo
import es.fap.simpleled.led.CompoundType;
import es.fap.simpleled.led.Entity
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.led.util.ModelUtils;
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.LedFactory;

public class Entidad implements Comparable{

	public Entity entidad;
	
	public boolean singletonsId = false;
	
	public static Entidad create(Entity entidad){
		Entidad util = new Entidad();
		util.entidad = entidad;
		return util;
	}
	
	public boolean nulo(){
		return entidad == null;
	}
	
	public String getClase(){
		if (entidad == null)
			return "";
		return entidad.getName();
	}
	
	public String getIdCheck(){
		if (entidad == null || (!singletonsId && isSingleton()))
			return "";
		return "${variable}?.id";
	}

	public String getIdNoCheck(){
		if (entidad == null || (!singletonsId && isSingleton()))
			return "";
		return "${variable}.id";
	}
	
    public String getVariable(){
		if (entidad == null)
			return "";
		if (entidad.name.equals("SolicitudGenerica"))
			return "solicitud";
        return StringUtils.firstLower(entidad.name);
    }
	
	public String getString(){
		if (entidad == null)
			return "";
		return entidad.name;
	}

    public boolean isSingleton(){
		if (entidad == null)
			return false;
		return LedEntidadUtils.esSingleton(entidad);
    }

    public String getId(){
		if (entidad == null || (!singletonsId && isSingleton()))
			return "";
		if (entidad.name.equals("SolicitudGenerica"))
			return "idSolicitud";
        return "id${entidad.name}";
    }

    public String getTypeId(){
		if (entidad == null || (!singletonsId && isSingleton()))
			return "";
        return "Long $id";
    }

    public String getTypeVariable(){
		if (entidad == null)
			return "";
        return "$entidad.name ${getVariable()}";
    }

	public String getTypeDb(){
		if (entidad == null)
			return "";
		return "$entidad.name ${getVariableDb()}";
	}
	
    public String getVariableDb(){
		if (entidad == null)
			return "";
		if (entidad.name.equals("SolicitudGenerica"))
			return "dbSolicitud";
        return "db" + entidad.name;
    }
	
	@Override
	public boolean equals(Object util){
		Entidad e = (Entidad)util;
		return entidad == e || entidad?.name.equals(e.entidad?.name);
	}
	
	@Override
	public int hashCode() {
		if (entidad == null) return 0;
		entidad.name.hashCode();
	}
	
	@Override
	public int compareTo(Object util){
		Entidad e = (Entidad)util;
		if (equals(e)){
			return 0;
		}
		return 1;
	}
	
	public EntidadInfo getInfo(Campo campo){
		List<EntidadInfo> subcampos = CampoUtils.calcularSubcampos(campo);
		for (EntidadInfo info: subcampos){
			if (equals(info.entidad))
				return info;
		}
		return new EntidadInfo(this);
	}
	
	/*
	* Devuelve la entidad Solicitud, y si no la encuentra (porque se está generando el
	* módulo en vez de la aplicación), devuelve la entidad SolicitudGenerica.
	*/
   public static Entity findSolicitud(){
	   Entity solicitud = ModelUtils.getVisibleNode(LedFactory.eINSTANCE.getLedPackage().getEntity(), "Solicitud", LedUtils.resource);
	   if (solicitud == null)
		   solicitud = ModelUtils.getVisibleNode(LedFactory.eINSTANCE.getLedPackage().getEntity(), "SolicitudGenerica", LedUtils.resource);
	   return solicitud;
   }
	
}