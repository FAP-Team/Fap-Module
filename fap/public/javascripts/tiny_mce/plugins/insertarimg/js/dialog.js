tinyMCEPopup.requireLangPack();

var InsertarImgDialog = {
	init : function() {
	},
     
    uploadandinsert : function(rutaImagen) {
		tinyMCEPopup.editor.execCommand('mceInsertContent', false, '<img src="' + rutaImagen +'" />');
		tinyMCEPopup.close();
	},
     
	insert : function() {
		// Insert the contents from the input into the document
		tinyMCEPopup.editor.execCommand('mceInsertContent', false, '<img src="' + document.forms[0].imgtag.src +'" />');
		tinyMCEPopup.close();
	},
};

tinyMCEPopup.onInit.add(InsertarImgDialog.init, InsertarImgDialog);
