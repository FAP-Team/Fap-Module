package services;

import java.net.URL;

import javax.xml.ws.soap.MTOMFeature;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import platino.FirmaClient;
import properties.MapPropertyPlaceholder;
import properties.PropertyPlaceholder;

import aed.AedClient;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import es.gobcan.eadmon.aed.ws.Aed;
import es.gobcan.eadmon.aed.ws.AedPortType;
import es.gobcan.platino.servicios.sfst.FirmaService;

public class AedServiceTest {
	static Injector injector;
	static AedService aedService;
	static boolean hasConnection = false;
	
//	@BeforeClass
//	public static void configure() throws Exception {
//		injector = Guice.createInjector(new AbstractModule(){
//			protected void configure() {
//				MapPropertyPlaceholder p = new MapPropertyPlaceholder();
//				p.put("fap.aed.url", "http://fap-devel.etsii.ull.es/ws/aed/");
//				p.put("fap.proxy.enable", "false");
//				
//				bind(AedService.class).to(AedServiceImpl.class);
//				bind(PropertyPlaceholder.class).toInstance(p);
//			}
//		});
//		aedService = injector.getInstance(AedService.class);
//		
//		//Comprueba si tiene conección con el AED para poder realizar las pruebas
//		hasConnection = aedService.getVersion() != null;
//	}
	
	@Test
	public void init(){
		//URL url = Aed.class.getClassLoader().getResource("aed/aed.wsdl");
		URL wsdlURL = FirmaClient.class.getClassLoader().getResource(
				"wsdl/firma-pre.wsdl");
		new FirmaService(wsdlURL).getFirmaService();
	}
	
//	@Test
//	public void getVersion() throws Exception {
//		String version = aedService.getVersion();
//		Assert.assertNotNull(version);
//	}
	
}
