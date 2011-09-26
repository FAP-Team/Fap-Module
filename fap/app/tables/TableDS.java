package tables;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TableDS {
	private List<List<Object>> aaData;
	
	public TableDS(List<Object[]> list){
		aaData = new LinkedList<List<Object>>();
		for(Object[] arr : list){
			aaData.add(new LinkedList<Object>(Arrays.asList(arr)));
		}
	}
	
	private String prepareButton(String styleclass){
		return "<div class=\"table-button " + styleclass + " \"></div>";
	}
	
	public TableDS acciones(boolean leer, boolean editar, boolean borrar){
		for(List<Object> l : aaData){
			String out = "";
			if(leer)
				out += prepareButton("table-button-leer");
			if(editar)
				out += prepareButton("table-button-editar");
			if(borrar)
				out += prepareButton("table-button-borrar");
			
			
			if(l != null)
				l.add(out);
		}
		return this;
	}
	
}