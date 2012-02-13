package services;

import static org.junit.Assume.assumeTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;

import javax.inject.Inject;

import models.Documento;
import models.ExpedientePlatino;
import models.Firma;
import models.Firmante;
import models.Solicitante;
import models.SolicitudGenerica;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import es.gobcan.platino.servicios.registro.JustificanteRegistro;

import platino.DatosRegistro;
import play.Play;
import play.libs.IO;
import play.modules.guice.InjectSupport;
import play.test.UnitTest;
import play.vfs.VirtualFile;
import properties.FapProperties;
import properties.PropertyPlaceholder;


@InjectSupport
public abstract class RegistroServiceTest extends UnitTest {

	protected static RegistroService registroService;

	@Inject
	protected static PropertyPlaceholder propertyPlaceholder;
	
	@Inject
	protected static FirmaService firmaService;
	
	@Inject
	protected static GestorDocumentalService gestorDocumentalService;
		
	@Before
	public void before(){
		assumeTrue(registroService.isConfigured());
	}
	
	@Test
	public void registroDeEntrada() throws Exception {
		//Solicitante
		Solicitante solicitante = new Solicitante();
		solicitante.tipo = "fisica";
		solicitante.fisica.nombre = "Luke";
		solicitante.fisica.primerApellido = "Sky";
		solicitante.fisica.segundoApellido = "Walker";
		solicitante.fisica.nip.tipo = "nif";
		solicitante.fisica.nip.valor = "78574424F";
		
		File f = documentoARegistrar();
		
		Documento documento = uploadTestDocumento(f);
		assertNotNull(documento.uri);
		
		//Firma el documento
		byte[] content = IO.readContent(f);
		String firmaDocumento = firmaService.firmarDocumento(content);
		assertNotNull(firmaDocumento);
		
		Firmante firmante = new Firmante(solicitante, "unico");
		firmante.fechaFirma = new DateTime(2003, 1, 1, 12, 15);

		models.Firma firma = new models.Firma(firmaDocumento, firmante);
		gestorDocumentalService.agregarFirma(documento, firma);
		
		String firma2Documento = firmaService.firmarDocumento(content);
		gestorDocumentalService.agregarFirma(documento, new Firma(firma2Documento, firmante));
		
		//Registra el documento
		ExpedientePlatino expediente = new ExpedientePlatino();
		models.JustificanteRegistro justificante = registroService.registrarEntrada(solicitante, documento, expediente);
		assertNotNull(justificante.getNumeroRegistro());
		assertNotNull(justificante.getFechaRegistro());
		
	    File justificanteTmp = File.createTempFile("justificante_", ".pdf", Play.tmpDir);
	    IO.write(justificante.getDocumento().getBytes(), justificanteTmp);
	}
	
	
	private Documento uploadTestDocumento(File file) throws Exception {
		Documento d = new Documento();
		d.tipo = FapProperties.get("fap.aed.tiposdocumentos.base");
		d.descripcion = "prueba";

		gestorDocumentalService.saveDocumentoTemporal(d, file);
		return d;
	}

	private File documentoARegistrar() throws Exception {
		VirtualFile vf = Play.getVirtualFile("/test/services/registro.pdf");
		if(!vf.exists())
			throw new FileNotFoundException();
		
		return vf.getRealFile();
	}

}
