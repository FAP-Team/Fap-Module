function createAndSubmitDynamicForm(action, params, $div){
	try{
		var insertion="<form id='dynamicPostForm' method='POST' action='"+action+"'>";
		
		for (var i in params) {
			var keyValue = params[i].split('=');
			var name = keyValue[0];
			var value = keyValue[1];
			if (value.match("^{w+}$")){
				var fieldName = value.substr(1, value.length-2);
				value = $F(fieldName);
			}
			insertion += "<input type='hidden' name='"+name+"' value='"+value+"'/>";
		};
		insertion += "</form>";

		$div.append(insertion);
		$('#dynamicPostForm').submit();
		$('#dynamicPostForm').remove();
	}catch(e){
		alert("Error"+e);
	}
}

/**
 * Cambia el attributo <b>name</b> de los elementos descendientes del ancestro que se especifica,
 * para que no se realice el binding en el controlador.
 * @param idGrupo id del elemento ancestro
 */
function hideNameFromGrupo (idGrupo) {
	var $node = $('#'+idGrupo);
	$node.contents ().each (function processNodes () {
    	var $node = $(this);
    	if ($node.attr("name")) {
    	    var found = /fakeNAME/.test($node.attr("name"));
    	    if (!found)	
    			$node.attr("name", "fakeNAME"+$node.attr("name"));
    	}
        $(this).contents ().each (processNodes);
	});
}

/**
 * Establece de nuevo el attributo <b>name</b> de los elementos descendientes del ancestro que se especifica,
 * para que se realice el binding.
 * @param idGrupo id del elemento ancestro
 */
function showNameFromGrupo (idGrupo) {
	var $node = $('#'+idGrupo);
	$node.contents ().each (function processNodes () {
    	var $node = $(this);
    	if ($node.attr("name")) {
    		var myId = $node.attr("name");
    		myId = myId.replace("fakeNAME", "");
    		$node.attr("name", myId);
    	}
        $(this).contents ().each (processNodes);
	});
}

/**
 * Oculta el elemento y modifica el "name" si se especifica para que se envíe en el formulario, pero no lo "reciba" el controlador
 * @param element
 */
function _hide (id, sendHiddenFields) {
	var el = Ext.get(id);
	el.setVisibilityMode(Ext.Element.OFFSETS);
	el.hide();
	if (!sendHiddenFields)
		hideNameFromGrupo(id);
}

/**
 * Muestra el elemento y modifica el "name" si se especifica para que se envíe en el formulario, pero no lo "reciba" el controlador
 * @param element
 */
function _show (id, sendHiddenFields) {
	var el = Ext.get(id).show();
	
	if (!sendHiddenFields)
		showNameFromGrupo(id);
}