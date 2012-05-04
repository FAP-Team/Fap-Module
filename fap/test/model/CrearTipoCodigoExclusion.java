package model;

import models.TipoCodigoExclusion;

import org.junit.Assert;
import org.junit.Test;
import play.test.UnitTest;

public class CrearTipoCodigoExclusion extends UnitTest {
	
	@Test
	public void crearTipoCodigoExclusion(){
        TipoCodigoExclusion tce = new TipoCodigoExclusion();
        tce.codigo="Paco";
        tce.descripcion=null;
        tce.descripcionCorta="Cortita";
        tce.save();
	}
	
}
