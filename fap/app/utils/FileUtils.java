package utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;

import messages.Messages;

import org.apache.cxf.jaxrs.ext.multipart.InputStreamDataSource;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

//import com.lowagie.text.Document;
//import com.lowagie.text.Paragraph;
//import com.lowagie.text.pdf.*;
//import com.lowagie.text.pdf.parser.PdfTextExtractor;

import play.vfs.VirtualFile;

public class FileUtils {
    
	private static Logger log = Logger.getLogger(FileUtils.class);
	static final String cambioPagina = "----------";
	
	public static List<VirtualFile> findByExtensionRecursively(VirtualFile folder, String[] extensions){
		Collection<File> files = org.apache.commons.io.FileUtils.listFiles(folder.getRealFile(), extensions , true);
		
		
		if(files == null || files.size() == 0)
			return null; // No se encontró ningún archivo con esa extension
		
		List<VirtualFile> res = new ArrayList<VirtualFile>();
		for(File file : files){
			res.add(VirtualFile.open(file));
		}
		return res;
	}
	
	public static File join(String ... subpaths){
		if (subpaths.length == 0){
			return null;
		}
		String path = subpaths[0];
		for (int i = 1; i < subpaths.length; i++){
			path = new File(path, subpaths[i]).getAbsolutePath();
		}
		return new File(path);
	}
	
	public static List<File> getFilesRecursively(String path) {
        File folder = new File(path);
		if(!folder.exists() || !folder.isDirectory())
			return null;
		
		ArrayList<File> list = new ArrayList<File>();
		getFilesRecursively(folder, list);
		return list;
    }
	
	private static void getFilesRecursively(File folder, List list){
        folder.setReadOnly();
        File[] files = folder.listFiles();
        for(int j = 0; j < files.length; j++) {
            list.add(files[j]);
            if(files[j].isDirectory())
            	getFilesRecursively(files[j], list);
        }
	}
	
    public static List<File> filterByExtension(List<File> list, String extension){
    	if(list == null) return null;
    	if(extension == null) return list;
    	
    	ArrayList<File> result = new ArrayList<File>();
    	for(File f : list){
    		if(extension.equals(getExtension(f.getName()))){
    			result.add(f);
    		}
    	}
    	return result;
    }
    
    public static String getExtension(String file){
    	log.debug("GetExtension " + file);
    	String extension = null;
    	int dotPos = file.lastIndexOf(".");
        if(dotPos > 0) 
        	extension = file.substring(dotPos + 1);
        log.debug("GetExtension " + file + " Extension = " + extension);
        return extension;
    }
    
    public static List<String> absolutePaths(List<File> files){
    	ArrayList<String> result = new ArrayList<String>();
    	for(File f : files){
    		result.add(f.getAbsolutePath());
    	}
    	return result;
    }
    
    public static List<String> relativePath(List<File> files){
    	ArrayList<String> result = new ArrayList<String>();
    	for(File f : files){
    		VirtualFile vf = VirtualFile.open(f);
    		result.add(vf.relativePath());
    	}
    	return result;
    }
    
	public static byte[] getContentUrl(String filename) throws Exception {
		//java.util.Properties systemSettings = System.getProperties();
		
		ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
		int tam, leido;
		tam = leido = 0;

		//Habilita el proxy
//		if(getProxy().isEnabled()){
//			log.info(getProxy());
			System.getProperties().put("http.proxySet","true");
			System.getProperties().put("http.proxyPort","3128");
			System.getProperties().put("http.proxyHost","proxy.gobiernodecanarias.net");
//		}
		//Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.gobiernodecanarias.net", 3128));
		
		System.out.println("Vamos a conectar");
		URL url = new URL(filename);
		URLConnection connection = url.openConnection();
		
		System.out.println("Conectado");
		
	    tam = connection.getContentLength();
        System.out.println("Tamaño: "+tam);
        InputStream in = connection.getInputStream();
	    //InputStream in = url.openStream();
	        
	    byte[] buf = new byte[512];
	    int len;
	    while (true) {
	    	len = in.read(buf);
            if (len == -1) {
                break;
            }
            tmpOut.write(buf, 0, len);
            leido+= len;
        }
        tmpOut.close();
        System.out.println("Leido: "+leido);
		return tam == leido? tmpOut.toByteArray(): null;
	}
	
	public static byte[] getContentFromStream(InputStream inpStream) throws IOException{
		ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
		int tam, leido;
		leido = 0;
	        
	    byte[] buf = new byte[512];
	    int len;
	    while (true) {
	    	len = inpStream.read(buf);
            if (len == -1) {
                break;
            }
            tmpOut.write(buf, 0, len);
            leido+= len;
        }
        tmpOut.close();
		return tmpOut.toByteArray();
	
	}
    
//	public static String ConvertPDFToString(String uri) throws Exception{
//		PdfReader reader = new PdfReader(uri);
//		String buffer = "";
//		for (int i = 0; i < reader.getNumberOfPages(); i++){ //Pdf varias páginas - Ojo al parsear
//			PdfDictionary dictionary = reader.getPageN(1);
//			PRIndirectReference reference = null;
//			reference = (PRIndirectReference)dictionary.get(PdfName.CONTENTS);
//			PRStream stream = (PRStream) PdfReader.getPdfObject(reference);
//			byte[] bytes = PdfReader.getStreamBytes(stream);
//			PRTokeniser tokenizer = new PRTokeniser(bytes);
//			while (tokenizer.nextToken()) {
//				if (tokenizer.getTokenType() == PRTokeniser.TK_STRING) {
//					buffer+=tokenizer.getStringValue()+"\n";
//				}
//			}
//		    buffer += cambioPagina;
//		}
//		return buffer;
//	}
}