tinyMCEPopup.requireLangPack();

var InsertarEntidadFAPDialog = {
	init : function() {    
	    // Seteamos en la variable plantillaEnEditor del editor, el id de la plantilla actual (si lo hubiera)
	    tinyMCEPopup.editor.execCommand('existeIdPlantillaURL');
	},
    
    insertLista : function(contenido, entidad, alias, aliasAtributo, entidadAtributo, guardarAlias) {  
        var idPlantilla = tinyMCEPopup.editor.plantillaEnEditor;
        if (guardarAlias) {   
            // Guardamos los alias en base de datos (el alias de la entidad principial y el del atributo)
            // Ejemplo: #{list items:aportacion.documentos, as:"documentos25"} 
            //          El alias de la entidad principal es "aportacion"
            //          El alias de la entidad del atributo es "documentos"
            //          "documentos25" es el alias del bucle #{list}
            var jsonAlias = '{ "' + alias +  '" : "' + entidad + '" , "' + aliasAtributo + '" : "' + entidadAtributo + '" }';
            $.post(tinyMCE.settings.httpPath + '/plantillasdoccontroller/insertarAliasEntidad', {'idPlantilla' : idPlantilla, 'jsonAlias' : jsonAlias}, function() { ; });
         }
         else {
            var jsonAlias = '{ "' + aliasAtributo + '" : "' + entidadAtributo + '" }';
            $.post(tinyMCE.settings.httpPath + '/plantillasdoccontroller/insertarAliasEntidad', {'idPlantilla' : idPlantilla, 'jsonAlias' : jsonAlias}, function() { ; });
         }

		tinyMCEPopup.editor.execCommand('mceInsertContent', false, contenido);
		tinyMCEPopup.close();
	},
    
	insert : function(contenido, alias, entidad, guardarAlias) {
	    if (guardarAlias) {             // Guardamos el alias en base de datos
            var idPlantilla = tinyMCEPopup.editor.plantillaEnEditor;      
            var jsonAlias = "{ " + alias +  " : " + entidad + " }";
            $.post(tinyMCE.settings.httpPath + '/plantillasdoccontroller/insertarAliasEntidad', {'idPlantilla' : idPlantilla, 'jsonAlias' : jsonAlias }, function() { ; });
        }
        
        // Insertamos en el editor la ruta del atributo a sustituir
        contenido = '\\${' + contenido + '}'; 
		tinyMCEPopup.editor.execCommand('mceInsertContent', false, contenido);
		tinyMCEPopup.close();
	},
};

tinyMCEPopup.onInit.add(InsertarEntidadFAPDialog.init, InsertarEntidadFAPDialog);
