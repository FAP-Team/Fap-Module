package services;

import org.junit.BeforeClass;

import services.filesystem.FileSystemFirmaServiceImpl;
import services.filesystem.FileSystemRegistroService;

public class FileSystemRegistroServiceTest extends RegistroServiceTest {

    @BeforeClass
    public static void beforeClass(){
        firmaService = new FileSystemFirmaServiceImpl();
        registroService = new FileSystemRegistroService(gestorDocumentalService);
    }
    
}
