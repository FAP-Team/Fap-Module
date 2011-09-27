package generator.utils;

import java.util.List;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.CompoundType;
import es.fap.simpleled.led.Entity
import generator.utils.StringUtils
import generator.utils.CampoUtils
import generator.utils.HashStack.HashStackName; 

public class EntidadUtils implements Comparable{

	public Entity entidad;
	
    public static EntidadUtils create(String campo){
		return create(CampoUtils.create(campo)?.entidad);
    }

	public static EntidadUtils create(CampoUtils campo){
		EntidadUtils util = new EntidadUtils();
		util.entidad = campo?.getEntidad();
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
		
	public String getClase(){
		return entidad.getName();
	}

    public String getVariable(){
		if (entidad == null){
			return "";
		}
		if (entidad.name.equals("SolicitudGenerica")){
			return "solicitud";
		}
        return StringUtils.firstLower(entidad.name);
    }
	
	public String getString(){
		if (entidad == null){
			return "";
		}
		return entidad.name;
	}

    public boolean isSingleton(){
        return entidad?.getExtends()?.getName().equals("Singleton");
    }

    public String getId(){
		if (entidad == null){
			return "";
		}
		if (entidad.name.equals("SolicitudGenerica")){
			return "idSolicitud";
		}
        return "id${entidad.name}";
    }

    public String getTypeId(){
		if (entidad == null){
			return "";
		}
        return "Long $id";
    }

    public String getTypeVariable(){
		if (entidad == null){
			return "";
		}
        return "$entidad.name ${getVariable()}";
    }

	public String getTypeDb(){
		if (entidad == null){
			return "";
		}
		return "$entidad.name ${getVariableDb()}";
	}
	
    public String getVariableDb(){
		if (entidad == null){
			return "";
		}
		if (entidad.name.equals("SolicitudGenerica")){
			return "dbSolicitud";
		}
        return "db" + entidad.name;
    }
	
	@Override
	public boolean equals(Object util){
		EntidadUtils e = (EntidadUtils)util;
		return entidad.name.equals(e.entidad.name);
	}
	
	@Override
	public int compareTo(Object util){
		EntidadUtils e = (EntidadUtils)util;
		if (equals(e)){
			return 0;
		}
		return 1;
	}
	
	public static void addToSaveEntity(String campo){
		EntidadUtils.create(campo).addToSaveEntity();
	}
	
	public static void addToIndexEntity(String campo){
		EntidadUtils.create(campo).addToIndexEntity();
	}
	
	public static void addToSaveEntity(CampoUtils campo){
		EntidadUtils.create(campo).addToSaveEntity();
	}
	
	public static void addToIndexEntity(CampoUtils campo){
		EntidadUtils.create(campo).addToIndexEntity();
	}
	
	public void addToIndexEntity(){
		if (entidad != null){
			HashStack.push(HashStackName.INDEX_ENTITY, this);
		}
	}
	
	public void addToSaveEntity(){
		if (entidad != null){
			HashStack.push(HashStackName.SAVE_ENTITY, this);
		}
	}
	
}