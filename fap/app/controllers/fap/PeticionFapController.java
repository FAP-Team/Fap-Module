package controllers.fap;

import java.util.ArrayList;
import java.util.List;

import enumerado.fap.gen.ListaCesionesEnum;
import enumerado.fap.gen.ListaEstadosEnum;
import peticionCesion.PeticionAEAT;
import peticionCesion.PeticionATC;
import peticionCesion.PeticionBase;
import peticionCesion.PeticionINSSA008;
import peticionCesion.PeticionINSSR001;
import models.SolicitudGenerica;
import models.TableKeyValue;


public class PeticionFapController extends InvokeClassController{
	public static PeticionBase getPeticionObject (String tipo){	
		if (tipo.equals(ListaCesionesEnum.inssR001.name()))
			return new PeticionINSSR001();
		if (tipo.equals(ListaCesionesEnum.inssA008.name()))
			return new PeticionINSSA008();
		if (tipo.equals(ListaCesionesEnum.atc.name()))
			return new PeticionATC();
		if (tipo.equals(ListaCesionesEnum.aeat.name()))
			return new PeticionAEAT();
		return null;
	}
	
	public static java.util.List<String> getTiposCesiones(){
		java.util.List<TableKeyValue> rows = TableKeyValue.find("select tableKeyValue from TableKeyValue tableKeyValue where table=?", "listaCesiones").fetch();
		java.util.List<String> tipos = new ArrayList<String>();
		for (TableKeyValue tableKeyValue : rows) {
			tipos.add(tableKeyValue.key);
		}
		return tipos;

	}
	
}
