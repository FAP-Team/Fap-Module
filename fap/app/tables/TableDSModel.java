package tables;

import java.util.ArrayList;
import java.util.List;

public class TableDSModel {
	List<Object[]> list;
	int filas;
	
	public TableDSModel(int filas) {
		this.filas = filas;
		this.list = new ArrayList<Object[]>(filas);
	}
	
	public void put(Object... parameters){
		list.add(parameters);
	}
	
	public TableDS getDs(){
		return new TableDS(list);
	}
	
}
