var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var selectorNuevo = x("//div[@id='comboTestRef']//button/span[contains(.,'Nuevo')]");


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

    casper.then(function() {
        casper.fillSelectors("#ComboseditarForm", {
        "#solicitud_comboTest_list" : "b",
        "select#solicitud_comboTest_listNumber" : "_2",
        "select#solicitud_comboTest_listSinDuplicados" : "b"
        });
    });
    casper.then(function() {
        casper.click("ul.chzn-choices li.search-field");
        casper.click(x('//li[contains(text(),"B")][2]'));
        casper.click("ul.chzn-choices li.search-field");
        casper.click(x('//li[contains(text(),"D")][3]'));
    });
    utiles.captura("despues rellenar combos");
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

    utiles.changeRole(casper, "Usuario");
    utiles.clickEnGuardar(casper);
    utiles.assertPaginaGuardada(casper);
    utiles.captura("Despues de guardar")
}

utiles.casperBegin("combosPorDefecto", combosPorDefecto);