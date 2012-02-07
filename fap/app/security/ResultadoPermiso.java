package security;

public class ResultadoPermiso {
	
	Accion accion;
	Grafico grafico;
	
	public ResultadoPermiso(Accion accion){
		this.accion = accion;
	}
	
	public ResultadoPermiso(Grafico grafico){
		this.grafico = grafico;
	}
	
	public ResultadoPermiso(Accion accion, Grafico grafico){
		this.accion = accion;
		this.grafico = grafico;
	}
	
	public String getPrimeraAccion(){
		if (accion != null){
			if (accion.equals(Accion.Denegar)) return null;
			if (accion.equals(Accion.All)) return "editar";
			return accion.toString();
		}
		if (grafico != null)
			if (grafico.isVisibleOrGreater()) return "editar";
		return null;
	}
	
	public boolean checkAcceso(String accionStr){
		Accion consulta = Accion.parse(accionStr);
		if (consulta == null) return false;
		if (accion != null){
			if (accion.equals(Accion.Denegar)) return false;
			if (accion.equals(Accion.All)) return true;
			return consulta.equals(accion);
		}
		if (grafico != null) return grafico.isVisibleOrGreater();
		return false;
	}
	
	public boolean checkGrafico(String graficoStr){
		Grafico consulta = Grafico.parse(graficoStr);
		if (consulta == null) return false;
		if (grafico != null) return grafico.check(consulta);
		if (accion != null){
			if (accion.equals(Accion.Denegar)) return false;
			if (accion.equals(Accion.All)) return true;
			if (accion.equals(Accion.Leer)) return Grafico.Visible.check(consulta);
			else return Grafico.Editable.check(consulta);
		}
		return false;
	}
	
	
}