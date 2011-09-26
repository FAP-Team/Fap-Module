/**
* 
* Abre un popup
 * @param popup 				Id del popup.
 * @param url   				Url del contenido que se va a cargar.
 * @param action   				Accion que se va a llevar a cabo.
 * @param options.idHijo			Id de la entidad seleccionada en la tabla. (Si el popup se abre desde una tabla)
 * @param options.idPadre			Id de la entidad seleccionada en la tabla. (Si el popup se abre desde una tabla)
 * @param options.idSolicitud	Id de una solicitud
 * @param options.campo			campo mostrado en la tabla
 */
function popup_open(popup, url, action, options, callback) {
	$("body").append("<div id='" + popup + "' class='popup'></div>");
	var $popup = $("#" + popup);
	
	var optionsContent = {position: "center", width: "650", height: "auto"};
	var optionsLoading = {position: "center", width: "150", height: "150", title: "Cargando..."};
	
	$popup.dialog({ 	
		autoOpen: false,
		modal: true,
		resizable: false,
		close:  function(event, ui){
			$popup.remove();
		}
	});
			
	var cargado = false;
	var loadingShowed = false;
	
	if(callback != null)
		$popup.data('tabla', callback);
	
	$.get(url, options, function(data){
		cargado = true;
		if(typeof(data) == 'string'){
			$popup.html(data);
		}else{
			//Si el contenido viene el json hubo un error
			if(!data.success){
				$popup.dialog( "option", "title", 'Error' );
				var msg = new Mensajes("#" + popup);
				msg.error(data.message);
			}
		}

		$popup.dialog("option", optionsContent);		
		if(loadingShowed){
			$popup.removeClass("loading-popup");
		}else{
			$popup.dialog("open");
		}
		
	});
	
	//En el caso de que la petición esté tardando muestra el elemento de cargando
	setTimeout(function(){
		if(!cargado){
			loadingShowed = true;
			$popup.html("");
			$popup.addClass("loading-popup");
			$popup.dialog("option", optionsLoading);
			$popup.dialog("open");
		}
	}, 400);
}

function popupWait_open() {
	$("body").append("" +
			"<div id='popupWait' class='wait-popup'>" +
				"<div class='img'></div>" +
				"<div class='text'>Espere mientras se realiza la acción solicitada...</div>"+
				"<div class='adv'>Esta acción puede tardar varios minutos</div>"+
				"<div class='rec'>Por favor, no pulse ninguna tecla mientras se realiza la operación</div>"+
			"</div>");
	$popup = $("#popupWait.wait-popup");
	
	var optionsLoading = {position: "center", width: "350", height: "180", title: "Cargando..."};
	
	$popup.dialog({ 	
		autoOpen: false,
		modal: true,
		resizable: false,
		close:  function(event, ui){
			$popup.remove();
		}
	});
			
	$popup.dialog("option", optionsLoading);
	$popup.dialog("open");
}

function popupWait_close() {
	$popup = $("#popupWait.wait-popup");	
	$popup.dialog("close");
}
