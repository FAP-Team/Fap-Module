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

    utiles.changeRole(casper, "Administrador");

    crearReferencia("Referencia1");
    crearReferencia("Referencia2");
    crearReferencia("Referencia3");

    casper.thenClick(x("//div[@id='comboTestRef']//button/span[contains(.,'Nuevo')]"));

    casper.then(function(){
        casper.test.assertElementCount(x('//div[@id="comboTestRef"]//tr[contains(@class,"x-grid-row")]'),3);
    })

    utiles.changeRole(casper, "Usuario");
    utiles.clickEnGuardar(casper);
    utiles.assertPaginaGuardada(casper);
}

utiles.casperBegin("combosPorDefecto", combosPorDefecto);