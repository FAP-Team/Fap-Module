package generator.utils;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.CompoundType;
import es.fap.simpleled.led.Entity
import generator.utils.StringUtils
import generator.utils.CampoUtils
import generator.utils.HashStack.HashStackName;
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.led.util.ModelUtils;
import es.fap.simpleled.led.LedPackage;

public class EntidadUtils implements Comparable{

	public Entity entidad;
	
    public static EntidadUtils create(String campo){
		return create(CampoUtils.create(campo)?.entidad);
    }

	public static EntidadUtils create(CampoUtils campo){
		EntidadUtils util = new EntidadUtils();
		util.entidad = campo.getEntidad();
		return util;
	}
	
	public static EntidadUtils create(Entity entidad){
		EntidadUtils util = new EntidadUtils();
		util.entidad = entidad;
		return util;
	}
	
	public static EntidadUtils create(){
		return new EntidadUtils();
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
		if (entidad == null)
			return "";
		return "${variable}?.id";
	}

	public String getIdNoCheck(){
		if (entidad == null)
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
		if (entidad == null)
			return "";
		if (entidad.name.equals("SolicitudGenerica"))
			return "idSolicitud";
        return "id${entidad.name}";
    }

    public String getTypeId(){
		if (entidad == null)
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
		EntidadUtils e = (EntidadUtils)util;
		return entidad?.name.equals(e.entidad?.name);
	}
	
	@Override
	public int compareTo(Object util){
		EntidadUtils e = (EntidadUtils)util;
		if (equals(e)){
			return 0;
		}
		return 1;
	}
	
	/*
	* Devuelve la entidad Solicitud, y si no la encuentra (porque se está generando el
	* módulo en vez de la aplicación), devuelve la entidad SolicitudGenerica.
	*/
   public static Entity findSolicitud(){
	   Entity solicitud = ModelUtils.getVisibleNode(LedPackage.Literals.ENTITY, "Solicitud", LedUtils.resource);
	   if (solicitud == null)
		   solicitud = ModelUtils.getVisibleNode(LedPackage.Literals.ENTITY, "SolicitudGenerica", LedUtils.resource);
	   return solicitud;
   }
	
	public static void addToSaveEntity(String campo){
		EntidadUtils.create(campo).addToSaveEntity();
	}
	
	public static void addToSaveEntity(CampoUtils campo){
		EntidadUtils.create(campo).addToSaveEntity();
	}
	
	public void addToSaveEntity(){
		if (entidad != null){
			HashStack.push(HashStackName.SAVE_ENTITY, this);
		}
	}
	
}