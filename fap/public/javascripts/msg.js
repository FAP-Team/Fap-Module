/**
 * Clase que maneja cómo se muestran los mensajes por pantalla
 */
function Mensajes(renderTo){	
	var MSG_ERROR_CLASS = 'box-error';
	var MSG_FATAL_CLASS = 'box-fatal';
	var MSG_INFO_CLASS = 'box-info';
	var MSG_DEBUG_CLASS = 'debugBoxMessage';
	var MSG_WARN_CLASS = 'box-warn';
	var MSG_OK_CLASS = 'box-ok';
	var MSG_LOADING_CLASS = "box-loading";
	var MSG_COMMON_CLASS = 'box'; //Clase que se le añade a todos los divs, para poder seleccionarlos todos y limpiarlos
	
	if(renderTo == null) return null;
	
	return {
		id : renderTo,
		
		clear: function(){
			$(this.id).html("");

		},
		
		show: function(msg, styleclass){
			if(msg == null || $.trim(msg).length == 0) return null;
			if(this.id != null){
				$('<div class="' + styleclass + ' ' + MSG_COMMON_CLASS+'">' + msg +'</div>').hide().appendTo(this.id).fadeIn();
			}
			
			if(typeof console  != "undefined"){
				console.log(this.id);
				console.log('[' + styleclass + ']:' + msg);
			}
		},
	
		info: function(msg){
			this.show(msg, MSG_INFO_CLASS);
		},
		
		error: function(msg){
			this.show(msg, MSG_ERROR_CLASS);
		},
		
		fatal: function(msg){
			this.show(msg, MSG_FATAL_CLASS);
		},

		warn: function(msg){
			this.show(msg, MSG_WARN_CLASS);
		},
	
		ok: function(msg){
			this.show(msg, MSG_OK_CLASS);
		},
	
		debug: function(msg){
			this.show(msg, MSG_DEBUG_CLASS);
		},
		
		loading: function(msg){
			this.show(msg, MSG_LOADING_CLASS);
		}
	}
}