/**
* 
* Abre un popup
 * @param popup 				Id del popup.
 * @param url   				Url del contenido que se va a cargar.
 * @param options.idHijo			Id de la entidad seleccionada en la tabla. (Si el popup se abre desde una tabla)
 * @param options.idPadre			Id de la entidad seleccionada en la tabla. (Si el popup se abre desde una tabla)
 * @param options.idSolicitud	Id de una solicitud
 * @param options.campo			campo mostrado en la tabla
 */
function popup_open(popup, url, callback) {
	$("body").append("<div id=\""+popup+"\" class=\"modal hide fade in\"></div>");

	var $popup = $("#" + popup);
	
	$('#'+popup).bind('hidden', function () {
		$('#'+popup).remove();
	});
			
	var cargado = false;
	var loadingShowed = false;
	
	if(callback != null)
		$popup.data('tabla', callback);
	
	$.get(url, function(data){
		cargado = true;
		if(typeof(data) == 'string'){
			$popup.html(data);
		}else{
			//Si el contenido viene el json hubo un error
			if(!data.success){
				//var msg = new Mensajes("#" + popup + "Messages");
				//msg.error(data.message);
			}
		}
	});
	
	// Mostramos el popup ahora
	$popup.modal( {show : true, backdrop: "static"} );
	
	//En el caso de que la petición esté tardando muestra el elemento de cargando
	setTimeout(function(){
		if(!cargado){
			loadingShowed = true;
			popupAddWait(popup);
		}
	}, 100);
}

function popupWait_open() {
	$("body").append("<div id=\"popupWait_popup\" class=\"modal hide fade in\">"+
					 "<div class=\"modal-header\">"+
					   	"<a href=\"#\" class=\"close\">×</a>"+
    					"<h3>En proceso ...</h3>"+
					 "</div>"+
					 "<div class=\"modal-body\">"+
					 	"<div class='text'>Espere mientras se realiza la acción solicitada...</div>"+
						"<div class='adv'>Esta acción puede tardar varios minutos</div>"+
						"<div class='rec'>Por favor, no pulse ninguna tecla mientras se realiza la operación</div>"+
					 "</div>"+
					 "</div>");
					 
	$popup = $("#popupWait_popup");
	
	$("#popupWait_popup").bind('hidden', function () {
		$("#popupWait_popup").remove();
	});
	
	$popup.modal( {show : true, backdrop: "static"} );
}

function popupWait_close() {
	$popup = $("#popupWait_popup");	
	$popup.dialog("close");
}

function replaceId(url, entidad, id) {
	return url.replace(entidad, id).replace(new RegExp("amp;", 'g'), "");
}

function replaceAmpersand(url) {
	return url.replace(new RegExp("amp;", 'g'), "");
}

/**
 * Añade los botones especificados en el map, con su función. Se le añade el botón cancelar también
 * @param popup
 */
function popupButtons (popup, buttons, type, cancelButton, enable) {
	if (cancelButton) {
		// Añadimos el botón de cancelar
		popupAddButton (popup, "Cancelar", "$('#"+popup+"').modal('hide');", "secondary", true);
	}
	for (var button in buttons) {
		popupAddButton (popup, button, buttons[button], type, enable);
	}
}

function popupAddButton (popup, textButton, functionButton, type, enable) {
	var disabled = " ";
	var onClick = "onclick=\""+functionButton+";\"";
	if (enable == false) {
		disabled = "disabled=true";
		onClick = "";
	}
	var $popup = $('#' + popup);
	var typeLocal = type;
	if (textButton == 'Borrar')
		typeLocal = 'danger';
	$popup.find('.modal-footer').append("<a href=\"#\" id=\""+textButton+"_id\""+onClick+disabled+" class=\"btn "+typeLocal+"\" data-loading-text=\"Enviando...\">"+textButton+"</a>");
}

function popupTitle (popup, title) {
	var $popup = $('#' + popup);
	$popup.find('.modal-header').html("<a href=\"#\" class=\"close\">×</a><h3>"+title+"</h3>");
}

function popupAddWait (popup) {
	var $popup = $('#' + popup);
	$popup.html("<div class=\"modal-header\"><a href=\"#\" class=\"close\">×</a></div><div id=\""+popup+"modal_body\" class=\"modal-body\"><div class=\"animation_content\"><div id=\"facebookG\"><div id=\"block_1\" class=\"facebook_blockG\"></div><div id=\"blockG_2\" class=\"facebook_blockG\"></div><div id=\"blockG_3\" class=\"facebook_blockG\"></div></div></div></div>");
}

function popupMessages (popup, idMessages) {
	var $popup = $('#' + popup);
	$popup.find('.modal-body').append("<div id=\""+idMessages+"\"></div>");
}