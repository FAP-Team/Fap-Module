package models;

import play.db.jpa.Model;


public class FapModel extends Model {

	public FapModel() {
		// Esto se encarga de llamar al Init, de la clase A que tiene un extends de esta clase (FapModel). Si no tiene la clase A un Init, pues llama al Init que se encuentra en esta clase FapModel.
		// Por lo que una clase que extienda de esta, y tenga un Init, siempre se va a ejecutar al inicio de todo (en el new, o donde sea), por lo que hay que tener cuidado con los declarar métodos Init en clases que extiendan de FapModel.
		init();
	}

	public void init() {
		postInit();
	}

	public void postInit(){
		
	}

}
