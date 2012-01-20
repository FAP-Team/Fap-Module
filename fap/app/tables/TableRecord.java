package tables;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import play.db.jpa.Model;

import flexjson.JSONSerializer;

import models.Firmante;
import models.TableKeyValue;
import tags.ReflectionUtils;
import validation.ValueFromTable;

public class TableRecord<T> {
	
	public T objeto;
	public boolean permisoLeer;
	public boolean permisoEditar;
	public boolean permisoBorrar;

}
