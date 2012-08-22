package utils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;


import messages.Messages;
import models.ConfigurarMensaje;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

import play.Play;
import play.PlayPlugin;
import play.data.binding.Binder;
import play.db.Model;
import play.exceptions.YAMLException;
import play.vfs.VirtualFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses;
import play.data.binding.Binder;
import play.data.binding.types.DateBinder;
import play.db.DB;
import play.db.DBPlugin;
import play.db.Model;
import play.db.jpa.Transactional;
import play.exceptions.UnexpectedException;
import play.exceptions.YAMLException;
import play.vfs.VirtualFile;
import properties.FapProperties;

public class Fixtures extends play.test.Fixtures {
	
	static Pattern keyPattern = Pattern.compile("([^(]+)\\(([^)]+)\\)");
	
	private static String[] yamlExtension = {"yaml", "yml"};
	 
	public static void loadFolder(File file){
		loadFolder(file.getAbsolutePath());
	}
	
	public static void loadFolder(String path){
		VirtualFile vfolder = VirtualFile.fromRelativePath(path);
		if (!vfolder.exists()){
			vfolder = VirtualFile.open(path);
			if (!vfolder.exists()){
				return;
			}
		}
		List<VirtualFile> all = utils.FileUtils.findByExtensionRecursively(vfolder, yamlExtension);
		if (all != null){
			for(VirtualFile vf : all){
				load(vf);
			}
		}
	}
	
	public static void loadFolderFromAppAndFap(String path){
		loadFolder(path);
		
		if(FapProperties.isApplication()){
			loadFolder(utils.FileUtils.join(Play.modules.get("fap").getRealFile().getAbsolutePath(), path));
		}
	}
	
    public static void delete(Class<? extends Model>... types) {
        disableForeignKeyConstraints();
        for (Class<? extends Model> type : types) {
            Model.Manager.factoryFor(type).deleteAll();
        }
        enableForeignKeyConstraints();
    }

    public static void delete(List<Class<? extends Model>> classes) {
        @SuppressWarnings("unchecked")
        Class<? extends Model>[] types = new Class[classes.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = classes.get(i);
        }
        delete(types);
    }

    @SuppressWarnings("unchecked")
    public static void deleteAllModels() {
        List<Class<? extends Model>> classes = new ArrayList<Class<? extends Model>>();
        for (ApplicationClasses.ApplicationClass c : Play.classes.getAssignableClasses(Model.class)) {
            classes.add((Class<? extends Model>)c.javaClass);
        }
        Fixtures.delete(classes);
    }

    private static void disableForeignKeyConstraints() {
        if (DBPlugin.url.startsWith("jdbc:oracle:")) {
            DB.execute("begin\n" +
                    "for i in (select constraint_name, table_name from user_constraints where constraint_type ='R'\n" +
                    "and status = 'ENABLED') LOOP\n" +
                    "execute immediate 'alter table '||i.table_name||' disable constraint '||i.constraint_name||'';\n" +
                    "end loop;\n" +
                    "end;");
            return;
        }

        if (DBPlugin.url.startsWith("jdbc:hsqldb:")) {
            DB.execute("SET REFERENTIAL_INTEGRITY FALSE");
            return;
        }

        if (DBPlugin.url.startsWith("jdbc:mysql:")) {
            DB.execute("SET foreign_key_checks = 0;");
            return;
        }

        // Maybe Log a WARN for unsupported DB ?
        Logger.warn("Fixtures : unable to disable constraints, unsupported database : " + DBPlugin.url);
    }

    private static void enableForeignKeyConstraints() {
        if (DBPlugin.url.startsWith("jdbc:oracle:")) {
             DB.execute("begin\n" +
                     "for i in (select constraint_name, table_name from user_constraints where constraint_type ='R'\n" +
                     "and status = 'DISABLED') LOOP\n" +
                     "execute immediate 'alter table '||i.table_name||' enable constraint '||i.constraint_name||'';\n" +
                     "end loop;\n" +
                     "end;");
            return;
        }

        if (DBPlugin.url.startsWith("jdbc:hsqldb:")) {
            DB.execute("SET REFERENTIAL_INTEGRITY TRUE");
            return;
        }

        if (DBPlugin.url.startsWith("jdbc:mysql:")) {
            DB.execute("SET foreign_key_checks = 1;");
            return;
        }

        // Maybe Log a WARN for unsupported DB ?
        Logger.warn("Fixtures : unable to enable constraints, unsupported database : " + DBPlugin.url);
    }


    static String getDeleteTableStmt(String name) {
        if (DBPlugin.url.startsWith("jdbc:mysql:") ) {
            return "TRUNCATE TABLE " + name;
        } else if (DBPlugin.url.startsWith("jdbc:postgresql:")) {
            return "TRUNCATE TABLE " + name + " cascade";
        } else if (DBPlugin.url.startsWith("jdbc:oracle:")) {
            return "TRUNCATE TABLE " + name;
        }
        return "DELETE FROM " + name;
    }

    public static void deleteAll() {
        try {
            List<String> names = new ArrayList<String>();
            ResultSet rs = DB.getConnection().getMetaData().getTables(null, null, null, new String[]{"TABLE"});
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME");
                names.add(name);
            }
            disableForeignKeyConstraints();
            for (String name : names) {
                Logger.trace("Dropping content of table %s", name);
                DB.execute(getDeleteTableStmt(name) + ";");
            }
            enableForeignKeyConstraints();
            for(PlayPlugin plugin : Play.plugins) {
                plugin.afterFixtureLoad();
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot delete all table data : " + e.getMessage(), e);
        }
    }

    public static void load(VirtualFile yamlFile) {
        try {
            if (!yamlFile.exists()) {
                throw new RuntimeException("Cannot load fixture " + yamlFile.getName() + ", the file was not found");
            }
            InputStream is = yamlFile.inputstream();
            Yaml yaml = new Yaml();
            Object o = yaml.load(is);
            if (o instanceof LinkedHashMap<?, ?>) {
                @SuppressWarnings("unchecked") LinkedHashMap<Object, Map<?, ?>> objects = (LinkedHashMap<Object, Map<?, ?>>) o;
                Map<String, Object> idCache = new HashMap<String, Object>();
                for (Object key : objects.keySet()) {
                    Matcher matcher = keyPattern.matcher(key.toString().trim());
                    if (matcher.matches()) {
                        String type = matcher.group(1);
                        String id = matcher.group(2);
                        if (!type.startsWith("models.")) {
                            type = "models." + type;
                        }
                        if (idCache.containsKey(type + "-" + id)) {
                            throw new RuntimeException("Cannot load fixture " + yamlFile.getName() + ", duplicate id '" + id + "' for type " + type);
                        }
                        Map<String, String[]> params = new HashMap<String, String[]>();
                        if (objects.get(key) == null) {
                            objects.put(key, new HashMap<Object, Object>());
                        }
                        serialize(objects.get(key), "object", params);
                        @SuppressWarnings("unchecked")
                        Class<Model> cType = (Class<Model>)Play.classloader.loadClass(type);
                        resolveDependencies(cType, params, idCache);
                        Model model = (Model)Binder.bind("object", cType, cType, null, params);
                        for(Field f : model.getClass().getFields()) {
                            // TODO: handle something like FileAttachment
                            if (f.getType().isAssignableFrom(Map.class)) {
                                f.set(model, objects.get(key).get(f.getName()));
                            }

                        }
                        model._save();
                        Class<?> tType = cType;
                        while (!tType.equals(Object.class)) {
                            idCache.put(tType.getName() + "-" + id, Model.Manager.factoryFor(cType).keyValue(model));
                            tType = tType.getSuperclass();
                        }
                    }
                }
            }
            // Most persistence engine will need to clear their state
            for(PlayPlugin plugin : Play.plugins) {
                plugin.afterFixtureLoad();
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class " + e.getMessage() + " was not found", e);
        } catch (ScannerException e) {
            throw new YAMLException(e, yamlFile);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot load fixture " + yamlFile.getName() + ": " + e.getMessage(), e);
        }
    }

    static void serialize(Map<?, ?> values, String prefix, Map<String, String[]> serialized) {
        for (Object key : values.keySet()) {
            Object value = values.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof Map<?, ?>) {
                serialize((Map<?, ?>) value, prefix + "." + key, serialized);
            } else if (value instanceof Date) {
                serialized.put(prefix + "." + key.toString(), new String[]{new SimpleDateFormat(DateBinder.ISO8601).format(((Date) value))});
            } else if (value instanceof List<?>) {
                List<?> l = (List<?>) value;
                String[] r = new String[l.size()];
                int i = 0;
                for (Object el : l) {
                    r[i++] = el.toString();
                }
                serialized.put(prefix + "." + key.toString(), r);
            } else if (value instanceof String && value.toString().matches("<<<\\s*\\{[^}]+}\\s*")) {
                Matcher m = Pattern.compile("<<<\\s*\\{([^}]+)}\\s*").matcher(value.toString());
                m.find();
                String file = m.group(1);
                VirtualFile f = Play.getVirtualFile(file);
                if (f != null && f.exists()) {
                    serialized.put(prefix + "." + key.toString(), new String[]{f.contentAsString()});
                }
            } else {
                serialized.put(prefix + "." + key.toString(), new String[]{value.toString()});
            }
        }
    }

    static void resolveDependencies(Class<Model> type, Map<String, String[]> serialized, Map<String, Object> idCache) {
        Set<Field> fields = new HashSet<Field>();
        Class<?> clazz = type;
        while (!clazz.equals(Object.class)) {
            Collections.addAll(fields, clazz.getDeclaredFields());
            clazz = clazz.getSuperclass();
        }
        for (Model.Property field : Model.Manager.factoryFor(type).listProperties()) {
            if (field.isRelation) {
                String[] ids = serialized.get("object." + field.name);
                if (ids != null) {
                    for (int i = 0; i < ids.length; i++) {
                        String id = ids[i];
                        id = field.relationType.getName() + "-" + id;
                        if (!idCache.containsKey(id)) {
                            throw new RuntimeException("No previous reference found for object of type " + field.name + " with key " + ids[i]);
                        }
                        ids[i] = idCache.get(id).toString();
                    }
                }
                serialized.remove("object." + field.name);
                serialized.put("object." + field.name + "." + Model.Manager.factoryFor((Class<? extends Model>)field.relationType).keyName(), ids);
            }
        }
    }

    public static void deleteDirectory(String path) {
        try {
            FileUtils.deleteDirectory(Play.getFile(path));
        } catch (IOException ex) {
            throw new UnexpectedException(ex);
        }
    }

	@Transactional
	public static void loadConfigurarMensaje(String paginasFile) {
		Pattern keyPattern = Pattern.compile("([^(]+)\\(([^)]+)\\)");
		Object o = play.test.Fixtures.loadYaml(paginasFile);
		
		//Obtengo las paginas que ya están almacenadas
		List<ConfigurarMensaje> paginasMensaje = ConfigurarMensaje.findAll();
		
		if (o instanceof LinkedHashMap<?, ?>) {
			@SuppressWarnings("unchecked")
			LinkedHashMap<Object, Map<?, ?>> objects = (LinkedHashMap<Object, Map<?, ?>>) o;
			for (Object key : objects.keySet()) {
				//Llamo al Load del modelo específico que quiero cargar desde fichero.
				Model myModel = ConfigurarMensaje.loadModel(key, keyPattern, objects, o);
				
				// En este momento tengo el ConfigurarMensaje
				// completo y compruebo que no esté en BBDD
				// Para guardarlo
				if (myModel != null){
					ConfigurarMensaje cm = (ConfigurarMensaje) myModel;
					boolean encontrado = false;
					for (ConfigurarMensaje configurarMensaje : paginasMensaje) {
						if(configurarMensaje.nombrePagina.equals(cm.nombrePagina)){
							encontrado = true;
							break; //Encontrado, dejamos de buscar.
						}									
					}
					if (!encontrado)
						myModel._save();
				}
				else{
					Messages.error("Se produjo un error cargando los modelos desde fichero");
				}
			}
		} 
	}

	
}
