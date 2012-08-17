tinyMCEPopup.requireLangPack();

var InsertarEntidadFAPDialog = {
	init : function() {
	},
    
	insert : function(contenido) {
		// Insert the contents from the input into the document
        contenido = "&" + contenido + "&";
		tinyMCEPopup.editor.execCommand('mceInsertContent', false, contenido);
		tinyMCEPopup.close();
	},
};

tinyMCEPopup.onInit.add(InsertarEntidadFAPDialog.init, InsertarEntidadFAPDialog);
