tinyMCEPopup.requireLangPack();

var HeaderFooterDialog = {
	preInit : function() {
	    // Comprobamos que hay alguna plantilla actual "seteada" en el editor 
	    // (ver si hay un ancla con el id de la plantilla en la url)      
        tinyMCE.editors[0].execCommand('existeIdPlantillaURL');
        if (tinyMCEPopup.editor.plantillaEnEditor == false) {
            alert("¡Error! Debe guardar la plantilla actual para elegir su cabecera y su pie.");
            tinyMCEPopup.close();
        }

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

	selectTemplate : function(u, ti, header) {
	    var d, x, tsrc = this.tsrc;
	    if (header === 'header')
    		d = window.frames['templatesrc_header'].document; 
        else if (header === 'footer')
            d = window.frames['templatesrc_footer'].document;
            
		if (!u)
			return;

		d.body.innerHTML = this.templateHTML = this.getFileContents(u);

		for (x=0; x<tsrc.length; x++) {
			if (tsrc[x].title == ti)
				document.getElementById('tmpldesc').innerHTML = tsrc[x].description || '';
		}
	},

 	insert : function() {
        var indice = document.forms[0].header.selectedIndex 
		var header_value = document.forms[0].header.options[indice].value
		indice = document.forms[0].footer.selectedIndex 
		var footer_value = document.forms[0].footer.options[indice].value
		var idHeader = header_value.split(",")[0];
		var idFooter = footer_value.split(",")[0];
        
        tinyMCE.editors[0].execCommand('existeIdPlantillaURL');
        if (tinyMCEPopup.editor.plantillaEnEditor == false) {
            alert("¡Error! Debe guardar la plantilla actual para elegir su cabecera y su pie.");
            tinyMCEPopup.close();
        } 
	    
	    $.post("/plantillasdoccontroller/guardarHeaderFooter", {'idHeader' : idHeader, 'idFooter' : idFooter, 'idPlantilla' : tinyMCEPopup.editor.plantillaEnEditor}, function() {
             ;
        }).error(function (xhr, ajaxOptions, thrownError) { 
              alert('Error al guardar la plantilla: (' + xhr.status + ') ' + thrownError); 
        }); 
        
		tinyMCEPopup.close();
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

HeaderFooterDialog.preInit();
tinyMCEPopup.onInit.add(HeaderFooterDialog.init, HeaderFooterDialog);
