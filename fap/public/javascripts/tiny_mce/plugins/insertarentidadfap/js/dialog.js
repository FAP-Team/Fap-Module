tinyMCEPopup.requireLangPack();

var InsertarEntidadFAPDialog = {
	init : function() {
	},
    
	insert : function(contenido) {
		// Insert the contents from the input into the document
        contenido = '\\${' + contenido.charAt(0).toLowerCase() + contenido.slice(1) + '}';      // minúscula primera letra de la entidad
		tinyMCEPopup.editor.execCommand('mceInsertContent', false, contenido);
		tinyMCEPopup.close();
	},
};

tinyMCEPopup.onInit.add(InsertarEntidadFAPDialog.init, InsertarEntidadFAPDialog);
