package generator.utils

import java.io.File;

import org.apache.log4j.Logger;

public class FileUtils {
	
	def static String target = "";
	
	private static Logger logger = Logger.getLogger("FileUtil")
	
	def static routes = [
		MODEL: 'app/models/',
		VIEW  : "app/views/gen/",
		CONTROLLER_GEN : "app/controllers/gen/",
		CONTROLLER : "app/controllers/",
		CONTROLLER_GEN_POPUP : "app/controllers/gen/popups/",
		CONTROLLER_POPUP : "app/controllers/popups/",
		CONF  : "conf/",
		CONF_APPLICATION : "conf/application.conf",
		CONF_ROUTES : "conf/routes",
		LIST : 'app/listas/gen/',
		ENUM : 'app/enumerado/gen/',
		ENUM_FAP: 'app/enumerado/fap/gen/',
		MENU_GEN : 'app/views/gen/menu/',
		PERMISSION: 'app/secure/',
		PERMISSION_GEN: 'app/secure/gen/',
		JSON_DOCUMENTATION: 'compiler/src/es.fap.simpleled.ui/src/es/fap/simpleled/ui/documentation/json/',
		FAP_DOCUMENTATION: 'documentation/manual/',
		APP_CONFIG : 'app/config/'
	]
	
	public static final HashSet<String> overwrittenFiles = new HashSet<String>();
	
	public static final String REGION_SEPARATOR_START = "# === FAPGENERATED ==="
	public static final String REGION_SEPARATOR_END   = "# === END FAPGENERATED ==="

	public static final String REGION_MANUAL = "MANUAL REGION";

	public static final String REGION_IMPORT = "IMPORT REGION";

	public static String getRoute(String routeName){
		if(routes.containsKey(routeName))
			return target + routes[routeName]
		throw new RuntimeException("La ruta ${routeName} no existe");	
	}
	
	/**
	 * Escribe a fichero si el archivo no existe
	 * @param filepath Ruta
	 * @param content  Contenido
	 */
	public static void write(String filepath, String content){
		String fullPath = filepath;
		File f = new File(fullPath);
		if(!f.exists()){
			createFolders(fullPath);
			logger.info("Escribiendo en ${filepath}")
			f.write(content, "UTF-8");
		}else{
			//logger.info("El fichero ${filepath} ya existe, no se sobreescribe");
		}

	}
	
	public static void write(String path, String file, String content){
		write(path + file, content);
	}
	
	/**
	 * Sobreescribe en el fichero
	 * @param filepath Ruta
	 * @param content  Contenido
	 */
	public static void overwrite(String filepath, String content){
		String fullPath = filepath;
		File f = new File(fullPath);
		overwrittenFiles.add(f.getAbsolutePath());
		if(f.exists()){
			if (fileIsEqual(filepath, content)){
				return;	
			}
			f.delete();
		}
		write(filepath, content)
	}
	
	public static void overwrite(String path, String file, String content){
		overwrite(path + file, content);
	}
	
	private static void createFolders(String path){
		File f = new File(path);
		File folder = new File(f.parent);
		folder.mkdirs();
	}
	
	private static boolean fileIsEqual(String filepath, String content){
		File f = new File(filepath);
		if(!f.exists()){
			return false;
		}
		String fileContent = f.getText();
		return content.equals(fileContent);		
	}
	
	public static void writeInRegion(String filepath, String content){
		File f = new File(filepath);
		
		if(!f.exists()){
			//Si no existe lo crea con los separadores
			
			String contentWithSeparators = """
${REGION_SEPARATOR_START}
${content}
${REGION_SEPARATOR_END}
			"""
			write(filepath, contentWithSeparators)
			return;
		}
		
		//Lee el fichero buscando la region y mete el contenido dentro de la region
		
		boolean inBlock = false
		boolean regionStartFound = false
		boolean regionEndFound  = false
		
		String fileContent = ""
		f.eachLine { line ->
			if(!inBlock){
				if(line.trim().equals(REGION_SEPARATOR_START)){
					inBlock = true
					regionStartFound = true
				}
				fileContent += line + "\n"
			}else{
				if(line.trim().equals(REGION_SEPARATOR_END)){
					inBlock = false
					fileContent += content + "\n" + line + "\n"
					regionEndFound = true
				}
			}
		}
		
		if(!regionStartFound) logger.error "REGION SEPARATOR START NOT FOUND AT ${filepath}"
		if(!regionEndFound)  logger.error "REGION SEPARATOR END NOT FOUND AT ${filepath}"
		
		//Si encontro bien las regiones escribe a fichero
		if(regionStartFound && regionEndFound){
			overwrite(filepath, fileContent);
		}
		
	}
	
	public static boolean deleteRecursive(File path) throws FileNotFoundException{
		if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
		boolean ret = true;
		if (path.isDirectory()){
			for (File f : path.listFiles()){
				ret = ret && FileUtils.deleteRecursive(f);
			}
		}
		return ret && path.delete();
	}
	
	public static void delete(File file){
		if (file.isDirectory()) {
			if(!file.name.equals(".svn")){
				for (File c : file.listFiles())
					delete(c);
			}
		}

		if (!file.delete())
			logger.error("Fallo al borrar ${file}")
	}
	
	
	/**
	 * Borra el contenido de una carpeta recursivamente
	 * @param path
	 */
	public static void deleteFolder(String path){
		logger.info "Borrando contenido de ${path}"
		File dir = new File(path)
		delete(dir);
	}
	
	public static void deleteFolder(String...keys){
		for(String key : keys){
			deleteFolder(getRoute(key))
		}
	}
	
	
	public static String addRegion(String filepath, String region){
		File f = new File(filepath);
		String start = "// === ${region} START ==="
		String end = "// === ${region} END ==="
		
		if(f.exists()){
			boolean inBlock = false
			boolean regionStartFound = false
			boolean regionEndFound  = false
		
			String fileContent = "\n";
			f.eachLine { line ->
				if (!regionEndFound) {
					if(!inBlock){
						if(line.trim().equals(start)){
							inBlock = true
							regionStartFound = true
							fileContent += start + "\n"
						}
					}else{
						if(line.trim().equals(end)){
							inBlock = false
							regionEndFound = true
						}
						fileContent += line + "\n"
					}
				}
			}
		
			if(!regionStartFound) logger.error "${start} NOT FOUND AT ${filepath}"
			if(!regionEndFound)  logger.error "${end} NOT FOUND AT ${filepath}"
		
			//Si encontro bien las regiones escribe a fichero
			if(regionStartFound && regionEndFound){
				return fileContent;
			}
		}
		
		return """
${start}
			
${end}
""";
			
	}

	
	
	public static boolean hasConstructRegion(String filepath,String entity, String region){
		File f = new File(filepath);
		String start = "// === ${region} START ==="
		String end = "// === ${region} END ==="
		
		if(f.exists()){
			boolean inBlock = false
			boolean regionStartFound = false
			boolean regionEndFound  = false

			boolean hasContruct = false;	
			def pattern = ~"\\s*public\\s*${entity}\\s*\\(\\s*\\).*"
			f.eachLine { line ->
				if (!regionEndFound) {
					if(!inBlock){
						if(line.trim().equals(start)){
							inBlock = true
							regionStartFound = true
						}
					}else{
						if(line.trim().equals(end)){
							inBlock = false
							regionEndFound = true
						}
						
						if (line.find(pattern)) {
							//println ("*************** $entity");
							hasContruct = true;
						}
					}
				}
			}
		
			if(!regionStartFound) logger.error "${start} NOT FOUND AT ${filepath}"
			if(!regionEndFound)  logger.error "${end} NOT FOUND AT ${filepath}"
		
			return hasContruct;
		}
		
		return """
${start}
			
${end}
""";
			
	}

}
