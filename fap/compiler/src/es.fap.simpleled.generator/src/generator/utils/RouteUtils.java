package generator.utils;

public class RouteUtils {
	public String method;
	public String url;
	public String action;
	
	public RouteUtils(String method, String url, String action) {
		this.method = method;
		this.url = url;
		this.action = action;
	}

	public static RouteUtils to(String method, String url, String action){
		return new RouteUtils(method, url, action);
	}
	
	@Override
	public String toString() {
		return method + " " + url + " " + action;
	}
	
}
