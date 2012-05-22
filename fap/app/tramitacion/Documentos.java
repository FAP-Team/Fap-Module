package tramitacion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;

import messages.Messages;
import models.Documento;
import play.mvc.Util;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;

public class Documentos {
	
	@Inject
    private static GestorDocumentalService gestorDocumentalService;

    public static Documento getDocumento(Long idDocumento){
        Documento documento = null;
        if(idDocumento == null){
            Messages.fatal("Falta parámetro idDocumento");
        }else{
            documento = Documento.find("select documento from Documento documento where documento.id=?", idDocumento).first();
            if(documento == null){
                Messages.fatal("Error al recuperar Documento");
            }
        }
        return documento;
    }
	
	/**
	 * 
	 * @param obj Instancia del objeto que contiene la colección de Documentos
	 * @param fieldName Nombre de la propiedad que referencia a los Documentos
	 */
	public static void borrarDocumentos(Object obj, String fieldName) {
		Class<?> clase = obj.getClass();

		try {
			// El objeto pasado contiene el nombre del campo?
			Field field = clase.getField(fieldName);

			// El objeto pasado implementa el método save?
			Method methodSave = clase.getMethod("save");

			// Existe el método getter
			String methodName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
			Method getMethod = clase.getMethod("get" + methodName);

			Type type = field.getGenericType();
			if (type instanceof ParameterizedType) {
				Type concreteType[] = ((ParameterizedType) type).getActualTypeArguments();
				if (concreteType.length > 0) {
					if (concreteType[0].equals(Documento.class)) {
						List<Documento> documentos = (List<Documento>) getMethod.invoke(obj);
						while (!documentos.isEmpty()) {
							Documento dbDocumento = getDocumento(documentos.get(0).id);
							
							documentos.remove(0);
							methodSave.invoke(obj);
							
							try {
								if (dbDocumento != null) {
									if (dbDocumento.uri != null && !dbDocumento.uri.isEmpty()) {
										// Esta llamada borra el objeto documento y lo elimina del aed
										gestorDocumentalService.deleteDocumento(dbDocumento);
									}
									else {
										dbDocumento.delete();
									}
								}
							}
							catch (GestorDocumentalServiceException ex1) {
								if (dbDocumento != null) {
									dbDocumento.delete();
								}
								play.Logger.info("Error borrando los documento temporales desde el aed");
							}
						}
					}
				}
			}
		}
		catch (Exception ex1) {
			ex1.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param obj Instancia del objeto que contiene el Documento
	 * @param fieldName Nombre de la propiedad que referencia al Documento
	 */
	public static void borrarDocumento(Object obj, String fieldName) {
		Class<?> clase = obj.getClass();
		
		try {
			
			// El objeto pasado implementa el método save?
			Method methodSave = clase.getMethod("save");
		
			// Existe el método getter
			String methodName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
			Method getMethod = clase.getMethod("get" + methodName);
			Object fldDocumento = getMethod.invoke(obj);
			
			// Existe el método setter
			Method setMethod = clase.getMethod("set" + methodName, new Class[] { Documento.class });

			if (fldDocumento != null && Documento.class.isInstance(fldDocumento)) {
				Documento dbDocumento = getDocumento(((Documento) fldDocumento).getId());
				
				setMethod.invoke(obj, new Object[] { null });
				methodSave.invoke(obj);
				
				try {
					if (dbDocumento != null) {
						if (dbDocumento.uri != null && !dbDocumento.uri.isEmpty()) {
							// Esta llamada borra el objeto documento y lo elimina del aed
							gestorDocumentalService.deleteDocumento(dbDocumento);
						}
						else {
							dbDocumento.delete();
						}
					}
				}
				catch (GestorDocumentalServiceException ex1) {
					if (dbDocumento != null) {
						dbDocumento.delete();
					}
				} catch (Exception ex2) {}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

