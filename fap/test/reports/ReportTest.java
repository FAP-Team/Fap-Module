package reports;

import play.Play;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.util.*;
import java.io.*;

import org.junit.Test;

public class ReportTest extends UnitTest {
	
	@Test
	public void renderTmpFile() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("name", "fap");
		File f = new Report("/test/reports/reportTest.html").renderTmpFile(args);
		//Comprueba que el fichero existe
		assertTrue(f.exists());
		//Comprueba que el fichero se cree en la carpeta temporal de la aplicación
		assertEquals(Play.tmpDir.getAbsolutePath(), f.getParentFile().getAbsolutePath());
	}
	
}
