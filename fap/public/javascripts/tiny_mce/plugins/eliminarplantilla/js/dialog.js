tinyMCEPopup.requireLangPack();

var EliminarPlantillaDialog = {
	init : function() {  
	},
     
    eliminar : function() {
          // Insert the contents from the input into the document
		  var indice = document.forms[0].selectplantilla.selectedIndex;
		  var idPlantilla = document.forms[0].selectplantilla.options[indice].value;
		  
		  tinyMCEPopup.editor.setContent(''); 
          
          $.post("/plantillasdoccontroller/eliminarPlantilla", {'idPlantilla': idPlantilla}, function() {
                    tinyMCEPopup.execCommand('putIdPlantillaURL', '');      // borramos el anchor
                    tinyMCEPopup.close(); 
          }).error(function (xhr, ajaxOptions, thrownError) { 
                    alert('Error al eliminar la plantilla: (' + xhr.status + ') ' + thrownError); 
          });
	},
};

tinyMCEPopup.onInit.add(EliminarPlantillaDialog.init, EliminarPlantillaDialog);
