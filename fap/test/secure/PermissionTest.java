package secure;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import models.*;

import org.junit.Assert;
import org.junit.Test;


import play.test.UnitTest;

public class PermissionTest extends UnitTest {

//	@Test
//	public void permiso1(){
//		Convocatoria convocatoria = Convocatoria.get();
//		convocatoria.estado = "iniciada";
//		convocatoria.save();
//		
//		Map<String, Long> ids = new HashMap<String, Long>();
//		Map<String, Object> vars = new HashMap<String, Object>();
//		boolean permiso1 = PermissionGen.permiso1("leer", ids, vars);
//		Assert.assertTrue(permiso1);
//		
//		convocatoria.estado = "otro estado";
//		convocatoria.save();
//		
//		permiso1 = PermissionGen.permiso1("leer", ids, vars);
//		Assert.assertFalse(permiso1);
//		
//		convocatoria.estado = "borrador";
//		convocatoria.save();
//		
//		permiso1 = PermissionGen.permiso1("leer", ids, vars);
//		Assert.assertTrue(permiso1);
//	}
//	
//	@Test
//	public void permiso2(){
//		Map<String, Long> ids = new HashMap<String, Long>();
//		Map<String, Object> vars = new HashMap<String, Object>();
//		
//		Agente agente = new Agente();
//		Rol rolAdmin = new Rol("administrador");
//		Rol rolUsuario = new Rol("usuario");
//		rolAdmin.save();
//		agente.roles.add(rolAdmin);
//		agente.roles.add(rolUsuario);
//		agente.rolActivo = rolAdmin;
//		agente.save();
//		
//		Secure.setAgente(agente);
//		boolean permiso2 = PermissionGen.permiso2("leer", ids, vars);
//		Assert.assertTrue(permiso2);
//		
//		agente.rolActivo = rolUsuario;
//		agente.save();
//		
//		permiso2 = PermissionGen.permiso2("leer", ids, vars);
//		Assert.assertFalse(true);
//	}
}
