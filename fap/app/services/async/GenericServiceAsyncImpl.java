package services.async;

import java.lang.reflect.Method;

import play.classloading.enhancers.EnhancedForContinuations;
import play.jobs.JobsPlugin;
import play.libs.F.Promise;
import services.async.ticketing.TicketingServiceAsyncImpl;

/***
 * Implementa los métodos necesarios para que los servicios se ejecuten de forma asíncrona.
 * Para facilitar el paso de otros servicios a ejecución asíncrona, se ha utilizado reflects
 * para la implementacion. Los métodos execute implementados reciben el objeto original del servicio
 * que va a ejecutar el método (objeto inyectado por dependencias), el nombre del método a ejecutar de forma
 * asíncrona, y los posibles parámetros que debe recibir.
 * 
 * Al utilizar esta implementación, el servicio debe devolver el objeto Promise<> de esta llamada sin haber sido 
 * manipulado, y el controlador que llame al servicio debe ejecutar su método await() sobre dicho Promise.
 * Finalmente, dada la variedad de clases de objetos devueltos, se debe castear en la implementación el objeto a
 * un Promise<> del tipo necesario.
 * 
 * Un ejemplo de uso de esta clase se encuentra en TicketingServiceAsyncImpl
 * 
 * @see TicketingServiceAsyncImpl
 * @author aletepe
 */
public class GenericServiceAsyncImpl implements EnhancedForContinuations {
	
	public Object execute(final Object object, final String methodName, final Object[] params, final Class<?>... paramTypes) {
		try  {
	        final Method method = object.getClass().getMethod(methodName, paramTypes);
	        final Promise<Object> smartFuture = new Promise<>();

	        JobsPlugin.executor.submit(new Runnable() { 
	                @Override 
	                public void run() {
	                    try {
	                        smartFuture.invoke(method.invoke(object, params)); 
	                    } catch (Throwable e) {                
	                        smartFuture.invokeWithException(e); 
	                    } 
	                } 
	            });

        	return smartFuture;
        	
	    }  catch (Exception e) { 
	        throw new RuntimeException(e); 
	    }
	}
	
	public Object execute(final Object object, final String methodName) {
		try  {
	        final Method method = object.getClass().getMethod(methodName);
	        final Promise<Object> smartFuture = new Promise<>();

	        JobsPlugin.executor.submit(new Runnable() { 
	                @Override 
	                public void run() {
	                    try {
	                        smartFuture.invoke(method.invoke(object)); 
	                    } catch (Throwable e) {                
	                        smartFuture.invokeWithException(e); 
	                    } 
	                } 
	            });

        	return smartFuture;
        	
	    }  catch (Exception e) { 
	        throw new RuntimeException(e); 
	    }
	}
}
