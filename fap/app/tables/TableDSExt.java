package tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.Query;

import org.apache.commons.collections.map.HashedMap;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import play.Logger;
import play.db.helper.JpaHelper;


/**
 * Resultado para los JSON de las tablas
 */
public class TableDSExt {
	public long total;
	public List<Object> rows;
	
	
	/**
	 *  Se usa para consultas de campos específicos
	 * 
	 *  Ejemplo de uso:
	 * 
	 *  String select = "id, username, email, rolActivo.nombre";
	 *	Query q = JpaHelper.execute("Select " + select + " from Agente");
	 *	renderJSON(new TableDSExt(select, q.getResultList()));
	 * 
	 * @param fields Nombre de los campos que se consultaron
	 * @param rows   Resultado de la consulta
	 */
	public TableDSExt(String fields, List<Object[]> rows) {
		init(fields, rows);
	}
	
	public TableDSExt(Long id, String lista, List<String> fields) {
		Preconditions.checkNotNull(lista, "lista");
		if (lista.split("\\.").length > 1) {
			//Preconditions.checkNotNull(id, "id");
		}
		Preconditions.checkArgument(fields != null & fields.size() > 0, "campos");
		
		String raiz = lista.split("\\.")[0];
		String raizClass = raiz.substring(0, 1).toUpperCase() + raiz.substring(1);
				
		String campoHijo = fields.get(0).split("\\.")[0];
		String select = Joiner.on(",").join(fields);
		
		String qstr = "Select " + select + " from " + raizClass + " " + raiz + " INNER JOIN " + lista + " " + campoHijo + " where " + raiz + ".id=?";
		Query q = JpaHelper.execute(qstr, id);
		init(Joiner.on(",").join(fields), q.getResultList());
	}
	
	private void init(String fields, List<Object[]> rows) {
		Preconditions.checkNotNull(fields);
		Preconditions.checkNotNull(rows);
		
		//Elimina los . de los identificadores
		fields = fields.replaceAll("\\.", "_");
		
		this.total = rows.size();
		this.rows = new LinkedList<Object>();
				
		Iterable<String> fieldsList = Splitter.on(',').trimResults().split(fields);
		
		for(Object[] o : rows){
			TreeMap<String, Object> map = Maps.newTreeMap();
			
			Iterator<String> iterator = fieldsList.iterator();
			int i = 0;
			while(iterator.hasNext()){
				map.put(iterator.next(), o[i]);
				i++;
			}
			
			this.rows.add(map);
		}
	}
	
	/**
	 * Se usa para consultar de objetos enteros
	 * 
	 * Ejemplo de uso:
	 * 	List<Agente> agentes = Agente.findAll();
	 *  renderJSON(new TableDSExt(agentes));
	 * 
	 * @param rows
	 */
	public TableDSExt(List<?> rows){
		this.total = rows.size();
		this.rows = (List<Object>)rows;
	}	
	
}
