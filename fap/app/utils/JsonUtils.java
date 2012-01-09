package utils;

import java.util.List;

import models.TipoCriterio;
import models.TipoEvaluacion;

import com.google.gson.Gson;

import play.vfs.VirtualFile;

public class JsonUtils {

	/**
	 * Carga un fichero json y lo mapea a la clase especificada
	 * @param parser Objeto que realiza el parser
	 * @param path Ruta del fichero
	 * @param clazz Clase a la que se va a mapear
	 * @return
	 */
	public static <T> T loadObjectFromJsonFile(Gson parser, String path, Class<T> clazz){
		VirtualFile vf = VirtualFile.fromRelativePath(path);
		return parser.fromJson(vf.contentAsString(), clazz);
	}
	
	/**
	 * Carga un fichero json y lo mapea a la clase especificada
	 * @param path Ruta del fichero
	 * @param clazz Clase a la que se va a mapear
	 * @return
	 */
	public static <T> T loadObjectFromJsonFile(String path, Class<T> clazz){
		return loadObjectFromJsonFile(new Gson(), path, clazz);
	}
}
