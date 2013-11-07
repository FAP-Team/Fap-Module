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
	popupWait_openWithMessage("Espere mientras se realiza la acción solicitada...");
}

function popupWait_openWithMessage(mensaje) {
	$("body").append("<div id=\"popupWait_popup\" class=\"modal hide fade in\">"+
					 "<div class=\"modal-header\">"+
						"<button class=\"close\" data-dismiss=\"modal\">×</button>"+
    					"<h3>En proceso ...</h3>"+
					 "</div>"+
					 "<div class=\"modal-body\">"+
					 	"<div class='text'>"+mensaje+"</div>"+
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
	$popup.modal("hide");
}

function popupWarning_open(warningText, functionButton) {
	var typeLocal = "btn-danger";
	var cancelType = "btn-secondary";
	var acceptTextButton = "Aceptar";
	var cancelTextButton = "Cancelar";
	var cancelFunction = "$('#popupWarning_popup').modal('hide');location.reload();";
	var onClickAccept = "onclick=\""+functionButton+";\"";
	var onClickCancel = "onclick=\""+cancelFunction+";\"";
	
	$("body").append("<div id=\"popupWarning_popup\" class=\"modal hide fade in\">"+
					 "<div class=\"modal-header\">"+
						"<button class=\"close\" data-dismiss=\"modal\">×</button>"+
    					"<h3>Aviso</h3>"+
					 "</div>"+
					 "<div class=\"modal-body\">"+
					 	"<div class='text'>"+ warningText +"</div>"+
					 "</div>"+
					 "<div class=\"modal-footer\">"+
					 	"<a href=\"#\" id=\""+acceptTextButton+"_id\""+onClickAccept+" class=\"btn "+typeLocal+"\" data-loading-text=\"Enviando...\">"+ acceptTextButton +"</a>"+
					 	"<a href=\"#\" id=\""+cancelTextButton+"_id\""+onClickCancel+" class=\"btn "+cancelType+"\" data-loading-text=\"Enviando...\">"+ cancelTextButton +"</a>"+
					 "</div>"+
					 "</div>");

	$popup = $("#popupWarning_popup");
	$popup.modal( {show : true, backdrop: "static"} );
}

function popupWarning_close() {
	$popup = $("#popupWarning_popup");	
	$popup.modal("hide");
}

function replaceId(url, entidad, id) {
	return url.replace(entidad, id).replace(new RegExp("amp;", 'g'), "");
}

function replaceRedirigirAnterior(url, urlRedirigir) {
	return url.replace("redirigir=anterior", "redirigir=" + urlRedirigir);
}

function replaceAmpersand(url) {
	return url.replace(new RegExp("amp;", 'g'), "");
}

/**
 * Añade los botones especificados en el map, con su función. Se le añade el botón cancelar también
 * @param popup
 */
function popupButtons (popup, buttons, type, cancelButton, enable, recargarAlCancelar) {
	for (var button in buttons) {
		popupAddButton (popup, button, buttons[button], type, enable);
	}
	if (cancelButton) {
		// Añadimos el botón de cancelar
		if (recargarAlCancelar)
			popupAddButton (popup, "Cancelar", "$('#"+popup+"').modal('hide'); location.reload();", "btn-secondary", true);
		else
			popupAddButton (popup, "Cancelar", "$('#"+popup+"').modal('hide'); ", "btn-secondary", true);
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
		typeLocal = 'btn-danger';
	$popup.find('.modal-footer').append("<a href=\"#\" id=\""+textButton+"_id_"+popup+"\""+onClick+disabled+" class=\"btn "+typeLocal+"\" data-loading-text=\"Enviando...\">"+textButton+"</a>");
}

function popupTitle (popup, title) {
	var $popup = $('#' + popup);
	$popup.find('.modal-header').html("<button class=\"close\" data-dismiss=\"modal\">×</button><h3>"+title+"</h3>");
}

function popupAddWait (popup) {
	var $popup = $('#' + popup);
	$popup.html("<div class=\"modal-header\"><button class=\"close\" data-dismiss=\"modal\">×</button></div><div id=\""+popup+"modal_body\" class=\"modal-body\"><div class=\"animation_content\"><div id=\"facebookG\"><div id=\"block_1\" class=\"facebook_blockG\"></div><div id=\"blockG_2\" class=\"facebook_blockG\"></div><div id=\"blockG_3\" class=\"facebook_blockG\"></div></div></div></div>");
}

function popupMessages (popup, idMessages) {
	var $popup = $('#' + popup);
	$popup.find('.modal-body').append("<div id=\""+idMessages+"\"></div>");
}