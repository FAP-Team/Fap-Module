tinyMCEPopup.requireLangPack();

var GuardarComoDialog = {
	init : function() {  
	},
     
	insert : function(nombrePlantilla) {
		  // Insert the contents from the input into the document
         var nombrePlantilla = document.forms[0].nombrePlantilla.value;
         var regex = new RegExp("^([a-zA-Z0-9_/-]+)$"); 
         if(!regex.test(nombrePlantilla)) {
            alert("El nombre de la plantilla únicamente puede contener números, letras o guiones y la barra /. Prohibidos espacios en blanco");
         }
         else {
              nombrePlantilla = nombrePlantilla + ".html";
              var descripcionPlantilla = document.forms[0].descripcionPlantilla.value;
              //console.log("descripcionPlantilla: " + descripcionPlantilla );
              var datos = tinyMCE.editors[0].getContent();
              
              $.getJSON('/plantillasdoccontroller/comprobarNombrePlantillaUnico', {'nombrePlantilla': nombrePlantilla}, function(data) { 
	               $.each(data, function(index,value) {
	                     if (value.duplicado === "true") {
                            alert('¡Error! Ya existe una plantilla con ese nombre.'); 
                         }
                         else {
                              $.post("/plantillasdoccontroller/guardarPlantilla", {'idPlantilla': 'nuevo', 'nombrePlantilla': nombrePlantilla, 'descripcionPlantilla': descripcionPlantilla, 'contenido' : datos}, function(idNuevaPlantilla) {
                                        tinyMCEPopup.editor.execCommand('putIdPlantillaURL', idNuevaPlantilla);
                                        tinyMCEPopup.close(); 
                              }).error(function (xhr, ajaxOptions, thrownError) { 
                                        alert('Error al guardar una nueva plantilla: (' + xhr.status + ') ' + thrownError); 
                              });
                         }
	               });  
	          });

              /*
              if (compruebaNombrePlantilla === "true") {
                    alert('Error al guardar: ya existe una plantilla con ese nombre'); 
              }
              else {
                  $.post("/plantillasdoccontroller/guardarPlantilla", {'idPlantilla': 'nuevo', 'nombrePlantilla': nombrePlantilla, 'descripcionPlantilla': descripcionPlantilla, 'contenido' : datos}, function(idNuevaPlantilla) {
                            tinyMCEPopup.editor.execCommand('putIdPlantillaURL', idNuevaPlantilla);
                            tinyMCEPopup.close(); 
                  }).error(function (xhr, ajaxOptions, thrownError) { 
                            alert('Error al guardar una nueva plantilla: (' + xhr.status + ') ' + thrownError); 
                  });
              }
              */
         }
	},
};

tinyMCEPopup.onInit.add(GuardarComoDialog.init, GuardarComoDialog);
