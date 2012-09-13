tinyMCEPopup.requireLangPack();

var ToPDFDialog = {
	preInit : function() {
		var url = tinyMCEPopup.getParam("template_external_list_url");

		if (url != null)
			document.write('<sc'+'ript language="javascript" type="text/javascript" src="' + tinyMCEPopup.editor.documentBaseURI.toAbsolute(url) + '"></sc'+'ript>');
	},

	init : function() {
		var ed = tinyMCEPopup.editor, tsrc, sel, x, u;

 		tsrc = ed.getParam("template_templates", false);
 		sel = document.getElementById('tpath');

		// Setup external template list
		if (!tsrc && typeof(tinyMCETemplateList) != 'undefined') {
			for (x=0, tsrc = []; x<tinyMCETemplateList.length; x++)
				tsrc.push({title : tinyMCETemplateList[x][0], src : tinyMCETemplateList[x][1], description : tinyMCETemplateList[x][2]});
		}

		for (x=0; x<tsrc.length; x++)
			sel.options[sel.options.length] = new Option(tsrc[x].title, tinyMCEPopup.editor.documentBaseURI.toAbsolute(tsrc[x].src));

		this.resize();
		this.tsrc = tsrc;
	},

	resize : function() {
		var w, h, e;

		if (!self.innerWidth) {
			w = document.body.clientWidth - 50;
			h = document.body.clientHeight - 160;
		} else {
			w = self.innerWidth - 50;
			h = self.innerHeight - 170;
		}

		e = document.getElementById('templatesrc');

		if (e) {
			e.style.height = Math.abs(h) + 'px';
			e.style.width = Math.abs(w - 5) + 'px';
		}
	},

	loadCSSFiles : function(d) {
		var ed = tinyMCEPopup.editor;

		tinymce.each(ed.getParam("content_css", '').split(','), function(u) {
			d.write('<link href="' + ed.documentBaseURI.toAbsolute(u) + '" rel="stylesheet" type="text/css" />');
		});
	},

	selectTemplate : function(u, ti, header, descripcion) {
	    var d, x, tsrc = this.tsrc;
	    if ( descripcion === "null")
                descripcion = "";
	    if (header === 'header') {
    		d = window.frames['templatesrc_header'].document; 
		    document.getElementById('tmpldesc_header').innerHTML = descripcion || '';
    	}
        else if (header === 'footer') {
            d = window.frames['templatesrc_footer'].document;
            document.getElementById('tmpldesc_footer').innerHTML = descripcion || '';
        }
        	    
		if (!u)
			return;

		d.body.innerHTML = this.templateHTML = this.getFileContents(u);
/*
		for (x=0; x<tsrc.length; x++) {
			if (tsrc[x].title == ti)
				document.getElementById('tmpldesc').innerHTML = tsrc[x].description || '';
		}
*/				
	},

 	insert : function() {
        var indiceHeader = document.forms[0].header.selectedIndex 
		var indiceFooter = document.forms[0].footer.selectedIndex 
		
		var headerValue = document.forms[0].header.options[indiceHeader].value
		var footerValue = document.forms[0].footer.options[indiceFooter].value
		var nombrePlantillaHeader = document.forms[0].header.options[indiceHeader].text
		var nombrePlantillaFooter = document.forms[0].footer.options[indiceFooter].text
		var sustituirEntidades = false;
		if(document.forms[0].sustituirentidades.checked)
		    sustituirEntidades = true;
		    
		var idHeader = headerValue.split("|")[0];
		var idFooter = footerValue.split("|")[0];
		var datos = tinyMCE.editors[0].getContent();  

	    tinyMCE.editors[0].execCommand('existeIdPlantillaURL');
        var idPlantilla = tinyMCEPopup.editor.plantillaEnEditor;
        if (idPlantilla == false)
             idPlantilla = null;
     
        var msgCabeceraPie = ""
        if ( (headerValue === "") && (footerValue === "") )
            msgCabeceraPie = "(sin cabecera ni pie)";
        else if ( (headerValue !== "") && (footerValue !== "") )
            msgCabeceraPie = "con la cabecera <strong>" + nombrePlantillaHeader + "</strong> y el pie <strong>" + nombrePlantillaFooter + "</strong>";
        else if (headerValue !== "")   
            msgCabeceraPie = "con la cabecera <strong>" + nombrePlantillaHeader + "</strong> y sin pie";
        else    // footerValue !== ""
            msgCabeceraPie = "con el pie <strong>" + nombrePlantillaFooter + "</strong> y sin cabecera";
        
        var divGeneracionPDF = '<div id="pdf"><div id="aviso"><p>Generando el pdf ' + msgCabeceraPie + '.</p> <p>Esta acción puede tardar unos instantes...</p></div><img id="loading" src="img/loading.gif" /><p id="warning">¡¡Recuerde que generar el pdf no implica <strong>guardar los cambios</strong> en la plantilla!!</p></div>';        
        $("#frm").after(divGeneracionPDF);
        $("#frm").remove();
        
        $.post("/plantillasdoccontroller/html2pdf", {'contenido' : datos, 'idPlantilla': idPlantilla, 'idHeader' : idHeader, 'idFooter' : idFooter, 'sustituirEntidades': sustituirEntidades}, function(rutaFicheroPDF) {
                $('#loading').remove();
                var enlace = '<div id="descarga"><a class="enlacepdf" href="' + rutaFicheroPDF + '"><img id="imgpdf" src="img/pdf_editor_icono.png" /><br />Descargar PDF</a></div>';
                var cerrar = '<div class="mceActionPanel"><input type="button" id="cancel" name="cancel" value="Cerrar" onclick="tinyMCEPopup.close();" /></div>'
                $(enlace).insertAfter('#aviso');
                $(cerrar).insertAfter('#warning');
        }).error(function (xhr, ajaxOptions, thrownError) { 
                alert('Error al generar el pdf: (' + xhr.status + ') ' + thrownError); 
        });
       
	    /*
	    $.post("/plantillasdoccontroller/guardarHeaderFooter", {'idHeader' : idHeader, 'idFooter' : idFooter, 'idPlantilla' : tinyMCEPopup.editor.plantillaEnEditor}, function() {
             ;
        }).error(function (xhr, ajaxOptions, thrownError) { 
              alert('Error al guardar la plantilla: (' + xhr.status + ') ' + thrownError); 
        }); 
        */
		//tinyMCEPopup.close();
	},

	getFileContents : function(u) {
		var x, d, t = 'text/plain';

		function g(s) {
			x = 0;

			try {
				x = new ActiveXObject(s);
			} catch (s) {
			}

			return x;
		};

		x = window.ActiveXObject ? g('Msxml2.XMLHTTP') || g('Microsoft.XMLHTTP') : new XMLHttpRequest();

		// Synchronous AJAX load file
		x.overrideMimeType && x.overrideMimeType(t);
		x.open("GET", u, false);
		
		x.send(null);

		return x.responseText;
	}
};

ToPDFDialog.preInit();
tinyMCEPopup.onInit.add(ToPDFDialog.init, ToPDFDialog);
