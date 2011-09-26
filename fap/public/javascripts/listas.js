var Listas = {
	//Listas cacheadas	
	listas : {},
	
	//Callbacks que deben ejecutarse
	//cuando se recupere una lista
	callbacks : {},
	
	//Comprueba si una lista está en cache y si no la carga
	load : function(nombre, callback){
		if(this.listas.hasOwnProperty(nombre)){
			//Lista cacheada
			callback(listas[nombre]);
		}else if(this.callbacks.hasOwnProperty(nombre)){
			//Hecha la petición, esperando al callback
			this.callbacks[nombre].push(callback);
		}else{
			//La lista no se ha pedido, hay que hacer la petición
			this.callbacks[nombre] = [callback];
			var that = this;
			$.get("/lista/" + nombre , function(data){
				if(data.success){
					that.listas[nombre] = data.data;
					$.each(that.callbacks[nombre], function(index, callback){
						callback(data.data);
					});
				}
			});
		}
	}
}