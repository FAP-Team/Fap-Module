package models;

import java.util.*;
import javax.persistence.*;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.data.validation.*;
import org.joda.time.DateTime;
import models.*;
import messages.Messages;
import validation.*;
import audit.Auditable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// === IMPORT REGION START ===
import play.cache.Cache;
import utils.Fixtures;

// === IMPORT REGION END ===

@Entity
public class TableKeyValue extends FapModel {
	// Código de los atributos

	@Column(name = "t")
	public String table;

	@Column(name = "k")
	public String key;

	@Column(columnDefinition = "LONGTEXT", name = "v")
	public String value;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	//Prefijo para identificar la cache
	private static final String CACHEPREFIX = "TableKeyValue_";
	private static final String CACHEPREFIXLIST = CACHEPREFIX + "list_";
	private static final String CACHEPREFIXMAP = CACHEPREFIX + "map_";

	public TableKeyValue() {
		super();
	}

	public TableKeyValue(String table, String key, String value) {
		super();
		this.table = table;
		this.key = key;
		this.value = value;
	}

	/**
	 * Guarda la lista en la cache
	 * Se guardan dos campos
	 * 		CACHEPREFIXLIST + table  : Lista de TableKeyValue
	 * 		CACHEPREFIXMAP  + table  : Map<key,value> para hacer las búsquedas más rapidas
	 * @param table
	 */
	public static void renewCache(String table) {
		Logger.debug("Renovando cache de " + table);

		List<TableKeyValue> list = TableKeyValue.find("byTable", table).fetch();
		Logger.debug("Nºde filas encontradas " + list.size());
		Cache.set(CACHEPREFIXLIST + table, list);
		Map<String, String> m = new HashMap<String, String>();
		for (TableKeyValue tkv : list) {
			m.put(tkv.key, tkv.value);
		}
		Cache.set(CACHEPREFIXMAP + table, m);
	}

	/**
	 * Borra la cache(la lista y el map)
	 * 
	 * @param table
	 */
	private static void deleteCache(String table) {
		Cache.delete(CACHEPREFIXLIST + table);
		Cache.delete(CACHEPREFIXMAP + table);
	}

	/**
	 * Recupera una tabla como un Map
	 * @param table
	 * @return
	 */
	public static Map<String, String> findByTableAsMap(String table) {
		Map<String, String> m = Cache.get(CACHEPREFIXMAP + table, Map.class);
		if ((m == null) || (m.isEmpty())) {
			renewCache(table); //Almacena la tabla en cache
			m = Cache.get(CACHEPREFIXMAP + table, Map.class);
		}
		return m;
	}

	/**
	 * Recupera una tabla. Busca primero en cache.
	 * Si no está busca en base de datos
	 * @param table
	 * @return
	 */
	public static List<TableKeyValue> findByTable(String table) {
		List<TableKeyValue> m = Cache.get(CACHEPREFIXLIST + table, List.class);
		if (m == null) {
			renewCache(table); //Almacena la tabla en cache
			m = Cache.get(CACHEPREFIXLIST + table, List.class);
		}
		return m;
	}

	/**
	 * Almacena un nuevo elemento en la tabla
	 * Con el parámetro renewCache se indica si se quiere renovar la cache
	 * En el caso de que vayamos a hacer varios setValue juntos, renovar la cache
	 * únicamente en el último
	 * @param table
	 * @param key
	 * @param value
	 * @param renewCache
	 */
	public static void setValue(String table, String key, String value, boolean renewCache) {
		TableKeyValue first = TableKeyValue.find("byTableAndKey", table, key).first();
		if (first == null) {
			first = new TableKeyValue(table, key, value);
		} else {
			first.value = value;
		}
		first.save();

		//Refresca la cache
		if (renewCache) {
			deleteCache(table);
			findByTable(table);
		}
	}

	/**
	 * Almacena un nuevo elemento en la tabla renovando la cache
	 * @param table
	 * @param key
	 * @param value
	 */
	public static void setValue(String table, String key, String value) {
		setValue(table, key, value, true);
	}

	public static void setValue(TableKeyValue tkv) {
		setValue(tkv.table, tkv.key, tkv.value, true);
	}

	public static void setValue(TableKeyValue tkv, boolean renewCache) {
		setValue(tkv.table, tkv.key, tkv.value, renewCache);
	}

	/**
	 * Obtiene el value de un elemento
	 * Busca en la cache, si no está cacheado, cachea la tabla y busca el
	 * value en el map
	 * @param table
	 * @param key
	 * @return
	 */
	public static String getValue(String table, String key) {
		if (table == null || key == null)
			return null;

		Map<String, String> m = findByTableAsMap(table);
		if (m != null)
			return m.get(key);
		return null;
	}

	public static boolean contains(String table, String key) {
		//TODO si en cache hacer un contains del hash
		return getValue(table, key) != null;
	}

	/**
	 * borra un elemento de la tabla
	 * Con el parámetro renewCache se indica si se quiere renovar la cache
	 * En el caso de que vayamos a hacer varios remove juntos, renovar la cache
	 * únicamente en el último
	 * @param table
	 * @param key
	 * @param value
	 * @param renewCache
	 */
	public static void removeValue(String table, String key, boolean renewCache) {
		int borradas = TableKeyValue.delete("table=? and key=?", table, key);

		//Refresca la cache
		if (renewCache) {
			deleteCache(table);
			findByTable(table);
		}
	}

	public static void removeValue(TableKeyValue tkv) {
		removeValue(tkv.table, tkv.key, true);
	}

	public static void removeValue(TableKeyValue tkv, boolean renewCache) {
		removeValue(tkv.table, tkv.key, renewCache);
	}

	/**
	 * actualiza un elemento
	 * Con el parámetro renewCache se indica si se quiere renovar la cache
	 * En el caso de que vayamos a hacer varios update juntos, renovar la cache
	 * únicamente en el último
	 * Si el elemento antiguo y el nuevo es de la misma tabla solo se actualiza
	 * al final.
	 * @param table
	 * @param key
	 * @param value
	 * @param renewCache
	 */
	public static void updateValue(String oldTable, String oldKey, String newTable, String newKey, String newValue, boolean renewCache) {
		removeValue(oldTable, oldKey, renewCache);
		setValue(newTable, newKey, newValue, renewCache);
	}

	public static void updateValue(TableKeyValue oldTkv, TableKeyValue newTkv) {
		updateValue(oldTkv.table, oldTkv.key, newTkv.table, newTkv.key, newTkv.value, true);
	}

	public static void updateValue(TableKeyValue oldTkv, TableKeyValue newTkv, boolean renewCache) {
		updateValue(oldTkv.table, oldTkv.key, newTkv.table, newTkv.key, newTkv.value, renewCache);
	}

	/**
	 * Borra la cache y la tabla de base de datos
	 * @param table
	 */
	public static void deleteTable(String table) {
		deleteCache(table);
		TableKeyValue.delete("table = ?", table);
	}

	/**
	 * Devuelve la lista de claves
	 * @param tabla
	 * @return
	 */
	public static List<String> keys(List<TableKeyValue> tabla) {
		if (tabla == null)
			return null;

		List<String> keys = new ArrayList<String>();
		for (TableKeyValue tkv : tabla) {
			keys.add(tkv.key);
		}
		return keys;
	}

	public static long loadFromFiles() {
		return loadFromFiles(false);
	}

	public static long loadFromFiles(boolean cargadoPreviamente) {
		// Si antes de hacer esta llamada se realiza la homologa en TableKeyValueDependency o similar, para que no duplique las entradas
		if (!cargadoPreviamente)
			Fixtures.loadFolderFromAppAndFap("app/listas/gen/");

		List<TableKeyValue> all = TableKeyValue.findAll();
		HashSet<String> tables = new HashSet<String>();
		for (TableKeyValue keyValue : all) {
			tables.add(keyValue.table);
		}
		for (String table : tables) {
			TableKeyValue.renewCache(table);
		}

		return TableKeyValue.count();
	}

	@Override
	public String toString() {
		return "TableKeyValue [table=" + table + ", key=" + key + ", value=" + value + ", id=" + id + "]";
	}

	// === MANUAL REGION END ===

}
