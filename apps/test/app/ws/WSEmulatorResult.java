package ws;

import java.util.ArrayList;
import java.util.List;

public class WSEmulatorResult {
	public String text;
	public List<WSEmulatorResultListItem> list;

	public static WSEmulatorResult test() {
		WSEmulatorResult e = new WSEmulatorResult();
		e.text = "WebService test content";
		e.list = new ArrayList<WSEmulatorResultListItem>();
		e.list.add(new WSEmulatorResultListItem(1L, "Opcion 1"));
		e.list.add(new WSEmulatorResultListItem(2L, "Opcion 2"));
		e.list.add(new WSEmulatorResultListItem(3L, "Opcion 3"));
		return e;
	}

}
