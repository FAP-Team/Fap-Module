package utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import models.TipoCriterio;
import models.TipoEvaluacion;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
	
	public static <T> T loadObjectFromJsonFile(Gson parser, String path, Type type){
		VirtualFile vf = VirtualFile.fromRelativePath(path);
		return parser.fromJson(vf.contentAsString(), type);
	}
	
	public static <T> T loadObjectFromJsonFile(String path, Type type){
		return loadObjectFromJsonFile(new Gson(), path, type);
	}
	
}
