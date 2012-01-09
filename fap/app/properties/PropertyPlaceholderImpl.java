package properties;


/**
 * Wrapper para inyectar las properties
 * 
 * Utilizar en los servicios que utilicen guice para poder
 * testear de forma mas fácil sin añadir tantas dependencias
 * 
 */
public class PropertyPlaceholderImpl implements PropertyPlaceholder {

	@Override
	public String get(String key) {
		return FapProperties.get(key);
	}

	@Override
	public Boolean getBoolean(String key) {
		return FapProperties.getBoolean(key);
	}

	@Override
	public int getInt(String key) {
		return FapProperties.getInt(key);
	}

	@Override
	public long getLong(String key) {
		return FapProperties.getLong(key);
	}

}
