package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeticionModificacion {
	public Map<String, Long> idSimples;
	public String campoPagina;
	public List<ValorCampoModificado> valoresModificado;
	public List<ValorCampoModificado> valoresCreados;
	public List<ValorCampoModificado> valoresBorrados;
	
	public class ValorCampoModificado {
		public String nombreCampo;
		public List<String> valoresAntiguos;
		public List<String> valoresNuevos;
		
		public ValorCampoModificado(){
			nombreCampo="";
			valoresAntiguos=new ArrayList<String>();
			valoresNuevos=new ArrayList<String>();
		}
	}
	
	public PeticionModificacion(){
		idSimples = new HashMap<String, Long>();
		campoPagina="";
		valoresModificado = new ArrayList<ValorCampoModificado>();
		valoresCreados = new ArrayList<ValorCampoModificado>();
		valoresBorrados = new ArrayList<ValorCampoModificado>();
	}
	
	public void setValorModificado(String nombreCampo, List<String> valoresAntiguos, List<String> valoresNuevos){
		ValorCampoModificado valor = new ValorCampoModificado();
		valor.nombreCampo=nombreCampo;
		valor.valoresAntiguos.addAll(valoresAntiguos);
		valor.valoresNuevos.addAll(valoresNuevos);
		this.valoresModificado.add(valor);
	}
	
	public void setValorCreado(String nombreCampo, List<String> valoresAntiguos, List<String> valoresNuevos){
		ValorCampoModificado valor = new ValorCampoModificado();
		valor.nombreCampo=nombreCampo;
		valor.valoresAntiguos.addAll(valoresAntiguos);
		valor.valoresNuevos.addAll(valoresNuevos);
		this.valoresCreados.add(valor);
	}
	
	public void setValorBorrado(String nombreCampo, List<String> valoresAntiguos, List<String> valoresNuevos){
		ValorCampoModificado valor = new ValorCampoModificado();
		valor.nombreCampo=nombreCampo;
		valor.valoresAntiguos.addAll(valoresAntiguos);
		valor.valoresNuevos.addAll(valoresNuevos);
		this.valoresBorrados.add(valor);
	}
	
	public boolean isEmpty(){
		if ((!valoresCreados.isEmpty()) || (!valoresModificado.isEmpty()) || (!valoresBorrados.isEmpty())){
			return false;
		}
		return true;
	}
}
