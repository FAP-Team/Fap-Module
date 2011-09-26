package tags;

import java.lang.reflect.Field;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import validation.ValueFromTable;

public class ReflectionUtilsTest {

	@Test
	public void accesoFieldRecursivo() {
		Field field = ReflectionUtils.getFieldRecursively(Solicitud.class, "solicitud.solicitante.tipo");
		Assert.assertNotNull(field);
	}
	
	@Test
	public void accesoValueRecursivo() {
		Solicitud s = new Solicitud();
		s.solicitante = new Solicitante();
		s.solicitante.tipo = "fisica";
		
		Object o = ReflectionUtils.getValueRecursively(s, "solicitud.solicitante.tipo");
		Assert.assertEquals("fisica", o);
	}
	
	@Test
	public void accesoACamposDelTipo(){
		List<Field> fields = ReflectionUtils.getFieldsOfType(Solicitud.class, "solicitud.solicitante");
		System.out.println(fields);
		Assert.assertEquals(3, fields.size());

		Assert.assertEquals("tipo", fields.get(0).getName());
		Assert.assertEquals(String.class, fields.get(0).getType());
		
		Assert.assertEquals("fisica", fields.get(1).getName());
		Assert.assertEquals("juridica", fields.get(2).getName());
	}
	
	public void getTipoDeLista(){
		Class clase = ReflectionUtils.getListClass(Solicitud.class, "solicitud.lista");
		Assert.assertEquals(String.class, clase);
	}
	
}
