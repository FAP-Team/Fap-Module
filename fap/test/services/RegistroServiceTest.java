package services;

import static org.junit.Assume.assumeTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;

import javax.inject.Inject;

import models.Documento;
import models.ExpedientePlatino;
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


@InjectSupport
public class RegistroServiceTest extends UnitTest {

	@Inject
	static RegistroService registroService;

	@Inject
	static FirmaService firmaService;
	
	@Inject
	static AedService aedService;
	
	@Inject
	static GestorDocumentalService gestorDocumentalService;
	
	@Before
	public void before(){
		assumeTrue(registroService.hasConnection());
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
		
		//Documento a registrar
		//TODO falta añadir firmas
		
		File f = documentoARegistrar();
		
		Documento documento = uploadTestDocumento(f);
		assertNotNull(documento.uri);
		play.Logger.info("Documento temporal subido al aed");
		
		
		//Firma el documento
		byte[] content = IO.readContent(f);
		String firmaDocumento = firmaService.firmarContentSignature(content);
		play.Logger.info("Documento firmado");
		
		Firmante firmante = new Firmante(solicitante, "unico");
		firmante.fechaFirma = new DateTime(2003, 1, 1, 12, 15);
		aedService.agregarFirma(documento.uri, firmante, firmaDocumento);
		play.Logger.info("Firma añadida al documento");
		
		String firmaDocumento2 = firmaService.firmarContentSignature(content);
		play.Logger.info("Documento firmado");
		
		aedService.agregarFirma(documento.uri, firmante, firmaDocumento2);
		play.Logger.info("Firma2 añadida al documento");
		
		
		//Expediente platino
		ExpedientePlatino expedientePlatino = new ExpedientePlatino();
		gestorDocumentalService.crearExpediente(expedientePlatino);
		assertTrue(expedientePlatino.creado);
		assertNotNull(expedientePlatino.uri);
		play.Logger.info("Expediente de platino creado");
		
		//Datos de registro
		DatosRegistro datosRegistro = registroService.getDatosRegistro(solicitante, documento, expedientePlatino);
		play.Logger.info("Calculados datos de registro");
		
		//Datos de registro en formato que se pueden firmar
		String datosAFirmar = registroService.obtenerDatosAFirmarRegisto(datosRegistro);
		Assert.assertNotNull(datosAFirmar);
		
		//Firma los datos de registro
		String firma = firmaService.firmarPKCS7(datosAFirmar.getBytes("iso-8859-1"));
		Assert.assertNotNull(firma);
		play.Logger.info("Datos de registro firmados");
		
		//Comprueba que la firma fue correcta
		Boolean valida = firmaService.verificarPKCS7(datosAFirmar, firma);
		Assert.assertTrue(valida);
		play.Logger.info("Firma de los datos de registro válida");
		
		//Registra
		JustificanteRegistro justificanteRegistro = registroService.registroDeEntrada(datosAFirmar, firma);
		assertNotNull(justificanteRegistro);
		play.Logger.info("Justificante recuperado");
		
		
		File justificanteTmp = File.createTempFile("justificante_", ".pdf", Play.tmpDir);
		IO.write(justificanteRegistro.getReciboPdf().getInputStream(), justificanteTmp);
	}
	
	
	private Documento uploadTestDocumento(File file) throws Exception {
		Documento d = new Documento();
		d.tipo = FapProperties.get("fap.aed.tiposdocumentos.base");
		d.descripcion = "prueba";

		aedService.saveDocumentoTemporal(d, file);
		return d;
	}

	private File documentoARegistrar() throws Exception {
		VirtualFile vf = Play.getVirtualFile("/test/services/registro.pdf");
		if(!vf.exists())
			throw new FileNotFoundException();
		
		return vf.getRealFile();
	}
	
}
