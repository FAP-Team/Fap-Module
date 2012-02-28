package services;

import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import models.Firmante;
import models.Persona;
import models.PersonaFisica;
import models.PersonaJuridica;
import models.RepresentantePersonaJuridica;
import models.Solicitante;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import platino.InfoCert;
import play.modules.guice.InjectSupport;
import play.test.UnitTest;

public abstract class FirmaServiceTest extends Assert {

    private static final String TEST_FIRMA_INVALIDA = "esta no es una firma valida";
    private static final String TEST_TEXT = "test text";
    protected static FirmaService firmaService;
    protected static boolean hasConnection = false;

    @Before
    public void before() {
        assumeTrue(hasConnection);
    }

    @Test
    public void firmarTexto() throws Exception {
        assertCorrectFirmarValidarTexto(TEST_TEXT);
    }

    @Test
    public void firmarTextoConTildes() throws Exception {
        assertCorrectFirmarValidarTexto("Texto con tildes áéíóúÁÉÍÓÚ");
    }

    private void assertCorrectFirmarValidarTexto(String texto) throws Exception {
        String firma = firmaService.firmarTexto(texto.getBytes());
        assertNotNull(firma);
        boolean firmaValida = firmaService.validarFirmaTexto(texto.getBytes(), firma);
        assertTrue(firmaValida);
    }

    public void invalidFirma() throws Exception {
        boolean firmaValida = firmaService.validarFirmaTexto(TEST_TEXT.getBytes(), TEST_FIRMA_INVALIDA);
        assertFalse(firmaValida);
    }
    
    @Test(expected = NullPointerException.class)
    public void firmarTextoNull() throws Exception {
        firmaService.firmarTexto(null);
    }

    @Test(expected = NullPointerException.class)
    public void validarFirmaTextoTextoNull() throws Exception {
        firmaService.validarFirmaTexto(null, "firma");
    }

    @Test(expected = NullPointerException.class)
    public void validarFirmaTextoFirmaNull() throws Exception {
        firmaService.validarFirmaTexto("".getBytes(), null);
    }

    @Test
    public void extraerInformacion() throws Exception {
        assumeTrue(hasConnection);
        String texto = "Texto de prueba para firma";
        String firma = firmaService.firmarTexto(texto.getBytes());
        Assert.assertNotNull(firma);
        InfoCert certificado = firmaService.extraerCertificado(firma);
        Assert.assertNotNull(certificado);
        asssertValidCertificado(certificado);
    }

    public abstract void asssertValidCertificado(InfoCert certificado);

    /*
     * @Test(expected=NullPointerException.class) public void
     * calcularFirmantesNullSolicitante(){ firmaService.calcularFirmantes(null,
     * new ArrayList<Firmante>()); }
     * 
     * @Test(expected=NullPointerException.class) public void
     * calcularFirmantesNullFirmantes(){ firmaService.calcularFirmantes(null,
     * null); }
     * 
     * @Test public void calcularFirmantesPersonaFisica(){ //Solicitante persona
     * física sin representantes Solicitante solicitante = new Solicitante();
     * solicitante.tipo = "fisica"; solicitante.fisica =
     * crearPersonaFisica("1");
     * 
     * List<Firmante> firmantes = new ArrayList<Firmante>();
     * firmaService.calcularFirmantes(solicitante, firmantes);
     * 
     * assertEquals(1, firmantes.size()); Firmante firmante = firmantes.get(0);
     * assertFirmante(solicitante.fisica, firmante); assertEquals("unico",
     * firmante.cardinalidad); assertEquals("personafisica", firmante.tipo);
     * 
     * //Solicitante persona fisica con representante físico
     * solicitante.representado = true; solicitante.representante.fisica =
     * crearPersonaFisica("2"); solicitante.representante.tipo = "fisica";
     * firmaService.borrarFirmantes(firmantes);
     * firmaService.calcularFirmantes(solicitante, firmantes); assertEquals(2,
     * firmantes.size());
     * 
     * for(Firmante f : firmantes){ if(f.idvalor.equals("1")){
     * assertFirmante(solicitante.fisica, f); assertEquals("personafisica",
     * f.tipo); }else if(f.idvalor.equals("2")){
     * assertFirmante(solicitante.representante.fisica, f);
     * assertEquals("representante", f.tipo); }else{ throw new
     * IllegalStateException(); } assertEquals("unico", f.cardinalidad); }
     * 
     * //Solicitante persona fisica con representante juridico
     * solicitante.representante.tipo = "juridica";
     * solicitante.representante.fisica = null;
     * solicitante.representante.juridica = crearPersonaJuridica("2");
     * 
     * firmaService.borrarFirmantes(firmantes);
     * firmaService.calcularFirmantes(solicitante, firmantes);
     * 
     * assertEquals(2, firmantes.size()); for(Firmante f : firmantes){
     * if(f.idvalor.equals("1")){ assertFirmante(solicitante.fisica, f);
     * assertEquals("personafisica", f.tipo); }else if(f.idvalor.equals("2")){
     * assertFirmante(solicitante.representante.juridica, f);
     * assertEquals("representante", f.tipo); }else{ throw new
     * IllegalStateException(); } assertEquals("unico", f.cardinalidad); } }
     * 
     * @Test public void calcularFirmantesPersonaJuridica(){ //Sin
     * representantes Solicitante solicitante = new Solicitante();
     * solicitante.tipo = "juridica"; solicitante.juridica =
     * crearPersonaJuridica("1");
     * 
     * List<Firmante> firmantes = new ArrayList<Firmante>();
     * firmaService.calcularFirmantes(solicitante, firmantes);
     * 
     * assertEquals(1, firmantes.size()); Firmante firmante = firmantes.get(0);
     * assertFirmante(solicitante.juridica, firmante); assertEquals("unico",
     * firmante.cardinalidad); assertEquals("personajuridica", firmante.tipo);
     * 
     * //con Representantes RepresentantePersonaJuridica mancomunado =
     * crearRepresentantePersonaJuridicaFisico("mancomunado", "2");
     * RepresentantePersonaJuridica solidario =
     * crearRepresentantePersonaJuridicaFisico("solidario", "3");
     * RepresentantePersonaJuridica administradorUnico =
     * crearRepresentantePersonaJuridicaFisico("administradorUnico", "4");
     * solicitante.representantes.add(mancomunado);
     * solicitante.representantes.add(solidario);
     * solicitante.representantes.add(administradorUnico);
     * 
     * firmaService.borrarFirmantes(firmantes);
     * firmaService.calcularFirmantes(solicitante, firmantes);
     * 
     * assertEquals(4, firmantes.size()); for(Firmante f : firmantes){
     * if(f.idvalor.equals("1")){ assertFirmante(solicitante.juridica, f);
     * assertEquals("unico", f.cardinalidad); assertEquals("personajuridica",
     * f.tipo); }else if(f.idvalor.equals("2")){
     * assertFirmante(mancomunado.fisica, f); assertEquals("multiple",
     * f.cardinalidad); assertEquals("representante", f.tipo); }else
     * if(f.idvalor.equals("3")){ assertFirmante(solidario.fisica, f);
     * assertEquals("unico", f.cardinalidad); assertEquals("representante",
     * f.tipo); }else if(f.idvalor.equals("4")){
     * assertFirmante(administradorUnico.fisica, f); assertEquals("unico",
     * f.cardinalidad); assertEquals("representante", f.tipo); }else{ throw new
     * IllegalStateException(); } } }
     * 
     * @Test public void hanFirmadoTodos(){ Firmante unico1 = new
     * Firmante(crearPersonaTipoFisica("1"), "unico"); Firmante unico2 = new
     * Firmante(crearPersonaTipoFisica("2"), "unico"); Firmante multiple1 = new
     * Firmante(crearPersonaTipoFisica("3"), "multiple"); Firmante multiple2 =
     * new Firmante(crearPersonaTipoFisica("4"), "multiple"); List<Firmante>
     * firmantes = new ArrayList<Firmante>(); firmantes.add(unico1);
     * firmantes.add(unico2); firmantes.add(multiple1);
     * firmantes.add(multiple2);
     * 
     * assertFalse(firmaService.hanFirmadoTodos(firmantes));
     * 
     * multiple1.fechaFirma = new DateTime();
     * assertFalse(firmaService.hanFirmadoTodos(firmantes));
     * 
     * multiple2.fechaFirma = new DateTime();
     * assertTrue(firmaService.hanFirmadoTodos(firmantes));
     * 
     * multiple2.fechaFirma = null;
     * assertFalse(firmaService.hanFirmadoTodos(firmantes));
     * 
     * unico1.fechaFirma = new DateTime();
     * assertTrue(firmaService.hanFirmadoTodos(firmantes)); }
     * 
     * private RepresentantePersonaJuridica
     * crearRepresentantePersonaJuridicaFisico(String tipo, String id){
     * RepresentantePersonaJuridica rep = new RepresentantePersonaJuridica();
     * rep.tipo = "fisica"; rep.fisica = crearPersonaFisica(id);
     * rep.tipoRepresentacion = tipo; return rep; }
     * 
     * private PersonaFisica crearPersonaFisica(String nif){ PersonaFisica pf =
     * new PersonaFisica(); pf.nombre = "Frodo"; pf.primerApellido = "Bolsón";
     * pf.segundoApellido = "de Bolsón Cerrado"; pf.nip.tipo = "nif";
     * pf.nip.valor = nif; return pf; }
     * 
     * private PersonaJuridica crearPersonaJuridica(String cif){ PersonaJuridica
     * pj = new PersonaJuridica(); pj.entidad = "ACME"; pj.cif = cif; return pj;
     * }
     * 
     * private void assertFirmante(PersonaFisica pf, Firmante f){
     * assertEquals(pf.getNombreCompleto(), f.nombre); assertEquals(pf.nip.tipo,
     * f.idtipo); assertEquals(pf.nip.valor, f.idvalor); }
     * 
     * private void assertFirmante(PersonaJuridica pj, Firmante f){
     * assertEquals(pj.entidad, f.nombre); assertEquals("cif", f.idtipo);
     * assertEquals(pj.cif, f.idvalor); }
     * 
     * private Persona crearPersonaTipoFisica(String id){ Persona persona = new
     * Persona(); persona.tipo = "fisica"; persona.fisica =
     * crearPersonaFisica(id); return persona; }
     */
}
