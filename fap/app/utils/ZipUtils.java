package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.*;

import play.Play;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;

public class ZipUtils {

	public static boolean comprimirEnZip (String[] nombresAComprimir, String nombreComprimido, File ruta) {
		return comprimirEnZip(nombresAComprimir, nombreComprimido, ruta.getAbsolutePath());
	}
	
	public static boolean comprimirEnZip (String[] nombresAComprimir, String nombreComprimido, String rootPath) {
		// Ficheros a incluir en el archivo ZIP
		String[] filenames = nombresAComprimir;

		// Buffer para ir leyendo los ficheros a comprimir
		byte[] buf = new byte[1024];

		try {
		    // El fichero ZIP resultante
		    String outFilename = nombreComprimido;
		    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(rootPath+outFilename));

		    // Comprimimos los ficheros
		    for (int i=0; i<filenames.length; i++) {
		        FileInputStream in = new FileInputStream(rootPath+filenames[i]);

		        // Añadimos el fichero al ZIP
		        out.putNextEntry(new ZipEntry(rootPath+filenames[i]));

		        // Empezamos a transferir el contenido del fichero al ZIP
		        int len;
		        while ((len = in.read(buf)) > 0) {
		            out.write(buf, 0, len);
		        }

		        // Completamos esa entrada del ZIP, para empezar una nueva, si hubiese mas ficheros
		        out.closeEntry();
		        in.close();
		    }

		    // Cerramos el fichero comprimido, todo listo
		    out.close();
		    return true;
		} catch (IOException e) {
			return false;
		}
	}
	public static boolean descomprimirEnZip (String nombreADescomprimir, String nombreDescomprimido) {
		try {
			// Abrimos el fichero ZIP
			String inFilename = nombreADescomprimir;
			ZipInputStream in = new ZipInputStream(new FileInputStream(inFilename));
			
			// Necesario para autoasignar el unico fichero comprimido para leer
			ZipEntry entry = in.getNextEntry();
			
			// Abrimos el fichero de salida
			String outFilename = nombreDescomprimido;
			
			OutputStream out = new FileOutputStream(outFilename);

			// Empezamos a transferir del fichero comprimido al descomprimido
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			// Cerramos los ficheros
			out.close();
			in.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
