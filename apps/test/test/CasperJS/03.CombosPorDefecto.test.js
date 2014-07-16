var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;


var combosPorDefecto = function(test) {
    utiles.abrirUltimaSolicitud();

    casper.then(function() {
        casper.click("select#solicitud_comboTest_list");
        casper.click(x('//*[@id="solicitud_comboTest_list"]/option[contains(text(),"B")][2]'));
    });
    casper.then(function() {
        casper.click("select#solicitud_comboTest_listNumber");
        casper.click(x('//*[@id="solicitud_comboTest_listNumber"]/option[contains(text(),"2")]'));
    });

    casper.then(function() {
        casper.click("select#solicitud_comboTest_listSinDuplicados");
        casper.click(x('//*[@id="solicitud_comboTest_listSinDuplicados"]/option[contains(text(),"B")]'));
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