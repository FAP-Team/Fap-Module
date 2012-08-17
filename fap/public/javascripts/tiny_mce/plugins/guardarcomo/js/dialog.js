tinyMCEPopup.requireLangPack();

var GuardarComoDialog = {
	init : function() {  
	},
     
	insert : function(nombrePlantilla) {
		  // Insert the contents from the input into the document
          var nombrePlantilla = document.forms[0].nombrePlantilla.value;
          var datos = tinyMCE.editors[0].getContent();
          
          $.post("/plantillasdoccontroller/guardarPlantilla", {'idPlantilla': 'nuevo', 'nombrePlantilla': nombrePlantilla, 'contenido' : datos}, function(idNuevaPlantilla) {
                    tinyMCEPopup.editor.execCommand('putIdPlantillaURL', idNuevaPlantilla);
                    tinyMCEPopup.close(); 
          }).error(function (xhr, ajaxOptions, thrownError) { 
                    alert('Error al guardar una nueva plantilla: (' + xhr.status + ') ' + thrownError); 
          });
	},
};

tinyMCEPopup.onInit.add(GuardarComoDialog.init, GuardarComoDialog);
