package properties;

public interface PropertyPlaceholder {
	
	public String get(String key);
	
	public Boolean getBoolean(String key);
	
	public int getInt(String key);
	
	public long getLong(String key);
	
}
