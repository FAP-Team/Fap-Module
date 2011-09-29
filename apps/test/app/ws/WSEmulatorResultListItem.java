package ws;

import java.util.ArrayList;
import java.util.List;

public class WSEmulatorResultListItem {
	private Long id;
	private String text;
	
	public WSEmulatorResultListItem(Long id, String text) {
		this.id = id;
		this.text = text;
	}
	
	static List test(){
		List<WSEmulatorResultListItem> results = new ArrayList<WSEmulatorResultListItem>();
		results.add(new WSEmulatorResultListItem(1L, "Opcion 1"));
		results.add(new WSEmulatorResultListItem(2L, "Opcion 2"));
		results.add(new WSEmulatorResultListItem(3L, "Opcion 3"));
		return results;
	}
}
