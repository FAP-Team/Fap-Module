tinyMCEPopup.requireLangPack();

var ToPDFDialog = {
	init : function() {
          var datos = tinyMCE.editors[0].getContent();  
          
          $.post("/plantillasdoccontroller/html2pdf", {contenido : datos}, function(rutaFicheroPDF) {
                $('#loading').remove();
                var enlace = "<p id=\"descarga\"><a href=\"" + rutaFicheroPDF + "\">Descargar PDF</a></p>";
                $(enlace).insertAfter('#aviso');
          }).error(function (xhr, ajaxOptions, thrownError) { 
                    alert('Error al generar el pdf: (' + xhr.status + ') ' + thrownError); 
          });
	},
     
	insert : function() {
	},
};

tinyMCEPopup.onInit.add(ToPDFDialog.init, ToPDFDialog);
