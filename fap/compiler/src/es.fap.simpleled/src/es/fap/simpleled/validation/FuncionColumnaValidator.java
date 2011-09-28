package es.fap.simpleled.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.CampoAtributos;
import es.fap.simpleled.led.Columna;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.LedFactory;
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.Model;
import es.fap.simpleled.led.impl.LedFactoryImpl;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class FuncionColumnaValidator {

	public static void checkFuncionColumna(Columna columna, LedJavaValidator validator) {
		if (columna.getFuncion() == null){
			return;
		}
		Pattern funcionSinEntidadPattern = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher matcher = funcionSinEntidadPattern.matcher(columna.getFuncion());
		while (matcher.find()){
			String campo = matcher.group(1).trim();
			if (create(campo, columna) == null){
				validator.myError("El campo \"" + campo + "\" no es valido.", LedPackage.Literals.COLUMNA__FUNCION);
			}
		}
	}
	
	private static Entity buscarEntidad(String entidad, EObject obj){
		List<Entity> entidades = getEntidades(obj);
		Entity resultado = null;
		for (Entity e: entidades){
			if (e.getName().equals(entidad)){
				resultado = e;
				break;
			}
		}
		return resultado;
	}
	
	private static List<Entity> getEntidades(EObject obj){
		ArrayList<Entity> entidades = new ArrayList<Entity>();
		for (Resource r: obj.eResource().getResourceSet().getResources()){
			if (r.getContents().get(0) instanceof Model){
				Model model = (Model) r.getContents().get(0);
				for (Entity entidad: model.getEntidades()){
					entidades.add(entidad);
				}
			}
		}
		return entidades;
	}
	
	public static Campo create(String campoStr, EObject obj){
		String[] splitted = campoStr.split("\\.");
		if (splitted.length == 0){
			return null;
		}
		if (splitted.length == 1){
			if (splitted[0].trim().equals("")){
				return null;
			}
		}
		String entidad = splitted[0];
		String campo2 = entidad;
		List<String> atributos = new ArrayList<String>();
		for (int i = 1; i < splitted.length; i++){
			if (splitted[i].equals("")){
				return null;
			}
			atributos.add(splitted[i]);
			campo2 += "." + splitted[i];
		}
		if (!campo2.equals(campoStr)){
			return null;
		}
		Entity entity = buscarEntidad(entidad, obj);
		if (entity == null){
			return null;
		}
		LedFactory factory = new LedFactoryImpl();
		Campo campoResult = factory.createCampo();
		campoResult.setEntidad(entity);
		if (atributos.size() == 0){
			return campoResult;
		}
		CampoAtributos attrsResult = factory.createCampoAtributos();
		campoResult.setAtributos(attrsResult);
		for (int i = 0; i < atributos.size(); i++){
			String atributo = atributos.get(i);
			if (entity == null){
				return null;
			}
			Attribute attr = LedEntidadUtils.getAttribute(entity, atributo);
			if (attr == null){
				return null;
			}
			if (attr.getType().getCompound() == null){
				entity = null;
			}
			else{
				entity = attr.getType().getCompound().getEntidad();
			}
			attrsResult.setAtributo(attr);
			if (i < atributos.size() - 1){
				attrsResult.setAtributos(factory.createCampoAtributos());
				attrsResult = attrsResult.getAtributos();
			}
		}
		return campoResult;
	}
	
	
}
