package services;

import org.junit.BeforeClass;
import org.junit.Test;

import play.test.UnitTest;

import services.filesystem.FileSystemRegistroLibroResolucionesServiceImpl;
import services.registrolibroresoluciones.RegistroLibroResolucionesServiceImpl;


public class FileSystemRegistroLibroResolucionesServiceTest extends UnitTest {
    
	protected static FileSystemRegistroLibroResolucionesServiceImpl resolucion;
	
	@Test
    public void infoInyeccion(){
    	resolucion = new FileSystemRegistroLibroResolucionesServiceImpl();
    	resolucion.mostrarInfoInyeccion();
    }

}
