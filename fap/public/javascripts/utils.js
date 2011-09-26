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
