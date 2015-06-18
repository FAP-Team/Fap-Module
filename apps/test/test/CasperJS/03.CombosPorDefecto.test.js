var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var selectorNuevo = x("//div[@id='comboTestRef']//button/span[contains(.,'Nuevo')]");
var selectorBorrar = x("//div[@id='comboTestRef']//button/span[contains(.,'Borrar')]");

function crearReferencia(nombreReferencia) {
    casper.then(function () {
        casper.waitForSelector(selectorNuevo);
    });

    casper.thenClick(selectorNuevo);

    casper.then(function () {
        casper.waitUntilVisible("#comboTestRef_nombre");
        casper.waitUntilVisible("#Guardar_id_ComboTestRef_popup");
    });

    casper.then(function () {
        casper.fillSelectors("#ComboTestRefcrearForm", {
            "#comboTestRef_nombre": nombreReferencia
        }, false);
    });

    casper.thenEvaluate(function () {
        $("#Guardar_id_ComboTestRef_popup").click();
    });

    casper.then(function () {
        casper.waitWhileSelector("#Guardar_id_ComboTestRef_popup");
    })
}

var combosPorDefecto = function(test) {
    var nReferencias = 3;
    utiles.changeRole(casper,"Usuario");
    utiles.abrirUltimaSolicitud();

   	casper.waitForSelector("#ComboseditarForm");
   		
    casper.then(function() {
        casper.fillSelectors("#ComboseditarForm", {
        "#solicitud_comboTest_list" : "b",
        "select#solicitud_comboTest_listNumber" : "_2",
        "select#solicitud_comboTest_listSinDuplicados" : "b"
        });
    });

   	casper.waitForSelector("ul.chosen-choices li.search-field");
   		
    casper.then(function() {
        casper.click("ul.chosen-choices li.search-field");
        casper.click(x('//li[contains(text(),"B")][2]'));
        casper.click("ul.chosen-choices li.search-field");
        casper.click(x('//li[contains(text(),"D")][3]'));
    });

    utiles.clickEnGuardar(casper);
    utiles.assertPaginaGuardada(casper);
    
    utiles.changeRole(casper, "Administrador");

    for(var i = 0; i < nReferencias; i++ ) {
        crearReferencia("Referencia" + i);
    }

    var selectorFila = "div#comboTestRef tr.x-grid-row";
    casper.then(function() {
        casper.waitFor(function() {
            return casper.evaluate(function(selector, nRefs) {
                return $(selector).length >= nRefs;
            }, selectorFila, nReferencias)
        })
    })

    casper.then(function(){
        casper.test.assertElementCount(selectorFila,nReferencias);
    })
    
    
    //Se borran las referencias creadas con anterioridad
	for(var i = 0; i < nReferencias; i++ ) {
		var selectorFilaBorrar = x("//div[@id='comboTestRef']//table//tr/td[contains(.,'Referencia')]");
	    casper.then(function () {
	    	casper.waitForSelector(selectorFilaBorrar);
	    });
	   
	    casper.thenClick(selectorFilaBorrar); 
 		 
 		casper.waitForSelector(selectorBorrar);
		casper.thenClick(selectorBorrar);
		
   		casper.then(function () {
	       	casper.waitUntilVisible("#Borrar_id_ComboTestRef_popup");
	   	});    
		
	   	casper.thenEvaluate(function () {
	       	$("#Borrar_id_ComboTestRef_popup").click();
	   	}); 
    		
	   	casper.then(function () {
	       	casper.waitWhileSelector("#Borrar_id_ComboTestRef_popup");
	   	})
	   	
	   	casper.wait(5000, function() {
       		
   		});

	 } 

    utiles.changeRole(casper, "Usuario");
    utiles.clickEnGuardar(casper);

    utiles.assertPaginaGuardada(casper);

}

utiles.casperBegin("combosPorDefecto", combosPorDefecto);