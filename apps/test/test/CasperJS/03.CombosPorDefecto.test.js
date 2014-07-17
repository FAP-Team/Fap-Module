var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;


var combosPorDefecto = function(test) {
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


    utiles.clickEnGuardar(casper);
    utiles.assertPaginaGuardada(casper);
    casper.then(function() {
        casper.capture("img/combos-defecto.png");
    });
}

utiles.casperBegin("combosPorDefecto", combosPorDefecto);