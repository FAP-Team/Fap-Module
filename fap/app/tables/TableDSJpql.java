package tables;

import java.util.List;

import javax.persistence.Query;

import play.db.helper.JpaHelper;

public class TableDSJpql {

	private String q;
	
	
	public TableDSJpql(String q){
		this.q = q;
	}

	public TableDS getDs() {
		Query query = JpaHelper.execute(q);
		List list = query.getResultList();
		TableDS ds = new TableDS(list);
		return ds;
	}
	
}
