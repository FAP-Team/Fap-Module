package controllers;

import java.util.ArrayList;
import java.util.List;

import tags.ComboItem;

import controllers.gen.DireccionesCopiaControllerGen;

public class DireccionesCopiaController extends DireccionesCopiaControllerGen {

	public static List<ComboItem> comboProvincia() {
       List<ComboItem> result = new ArrayList<ComboItem>();
       //Añadir los elementos a la lista
       result.add(new ComboItem("_35", "Palmas, Las"));
       result.add(new ComboItem("_38", "Santa Cruz de Tenerife"));
       return result;
	}
}
