package generator.utils;

public class Route {
	public String method;
	public String url;
	public String action;
	
	public Route(String method, String url, String action) {
		this.method = method;
		this.url = url;
		this.action = action;
	}

	public static Route to(String method, String url, String action){
		return new Route(method, url, action);
	}
	
	@Override
	public String toString() {
		return method + " " + url + " " + action;
	}
	
}
