package controllers;

import java.io.ByteArrayInputStream;
import java.util.*;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import play.Logger;
import play.Play;
import play.db.jpa.GenericModel.JPAQuery;
import play.libs.IO;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.libs.XML;
import play.libs.XPath;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.utils.NoOpEntityResolver;
import properties.FapProperties;

import models.ComboTest;
import models.ComboTestRef;

import tags.ComboItem;

import controllers.gen.CombosOverwriteControllerGen;

public class CombosOverwriteController extends CombosOverwriteControllerGen {

	public static List<ComboItem> lista() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		result.add(new ComboItem("a", "A"));
		result.add(new ComboItem("b", "B"));
		result.add(new ComboItem("c", "C"));
		return result;
	}

	public static List<ComboItem> listaLong() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		result.add(new ComboItem(1L, "Uno"));
		result.add(new ComboItem(2L, "Dos"));
		result.add(new ComboItem(3L, "Tres"));
		return result;
	}

	public static List<ComboItem> listaMultiple(){
		List<ComboItem> result = new ArrayList<ComboItem>();
		result.add(new ComboItem("a", "Uno"));
		result.add(new ComboItem("b", "Dos"));
		result.add(new ComboItem("c", "Tres"));
		return result;
	}
	
	public static List<ComboItem> listaMultipleLong(){
		List<ComboItem> result = new ArrayList<ComboItem>();
		result.add(new ComboItem(1L, "Uno"));
		result.add(new ComboItem(2L, "Dos"));
		result.add(new ComboItem(3L, "Tres"));
		return result;
	}
	
	public static List<ComboItem> referencia() {
		List<ComboItem> result = new ArrayList<ComboItem>();

		// Filtra los elementos que tienen A
		List<ComboTestRef> comboTests = ComboTestRef.find("nombre like ?",
				"%a%").fetch();
		for (ComboTestRef c : comboTests) {
			result.add(new ComboItem(c.id, c.nombre));
		}

		return result;
	}

	public static List<ComboItem> referenciaMultiple() {
		List<ComboItem> result = new ArrayList<ComboItem>();

		// Filtra los elementos que tienen A
		List<ComboTestRef> comboTests = ComboTestRef.find("nombre like ?",
				"%a%").fetch();
		for (ComboTestRef c : comboTests) {
			result.add(new ComboItem(c.id, c.nombre));
		}

		return result;
	}

	public static List<ComboItem> wsjson() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		JsonObject root = null;
		
		if (Play.mode == Play.mode.DEV) {
			Response response = FunctionalTest.GET("/api/json");
			String content = FunctionalTest.getContent(response);
			String json = content;
			try {
				root = new JsonParser().parse(json).getAsJsonObject();
			} catch (Exception e) {
			}
		} else {
			// Modo producción
			String endPoint = "/api/json";
			String applicationPath = (FapProperties.get("http.path") != null) ? FapProperties.get("http.path") : "";
			String url = "http://"+request.host+applicationPath+endPoint;
			HttpResponse wsResponse = WS.url(url).get();
			root = wsResponse.getJson().getAsJsonObject();
		}
		
		JsonArray list = root.get("list").getAsJsonArray();
		Iterator<JsonElement> iterator = list.iterator();
		while (iterator.hasNext()) {
			JsonObject e = iterator.next().getAsJsonObject();
			result.add(new ComboItem(e.get("id").getAsLong(), e.get("text")
					.getAsString()));
		}

		return result;
	}

	public static List<ComboItem> wsxml() {
		List<ComboItem> result = new ArrayList<ComboItem>();

		Document xml = null;
		if (Play.mode == Play.mode.DEV) {
			try {
				Response response = FunctionalTest.GET("/api/xml");
				String content = FunctionalTest.getContent(response);
				xml = XML.getDocument(content);
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// Modo producción
			String endPoint = "/api/xml";
			String applicationPath = (FapProperties.get("http.path") != null) ? FapProperties.get("http.path") : "";
			String url = "http://"+request.host+applicationPath+endPoint;
			HttpResponse wsResponse = WS.url(url).get();
			xml = wsResponse.getXml();
		}
		
		for(Node node : XPath.selectNodes("ws.WSEmulatorResult/list/ws.WSEmulatorResultListItem", xml)){
			Long id = Long.parseLong(XPath.selectText("id", node));
			String text = XPath.selectText("text", node);
			result.add(new ComboItem(id, text));
		}
		 
		return result;
	}	
	
}
