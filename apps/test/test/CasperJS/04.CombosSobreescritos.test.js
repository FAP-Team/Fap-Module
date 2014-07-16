var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var combosSobreescritos = function(test) {
    utiles.abrirUltimaSolicitud();
    
    casper.then(function() {
        casper.click(x('//*[text()="Combos sobreescritos"]'));
    });

    casper.then(function() {
        casper.capture("img/combosSobreescritos.png");
        casper.test.assertTitle("CombosOverwrite");
    });

    casper.then(function() {
        casper.click("select#lista");
        casper.click(x('//*[@id="lista"]/option[contains(text(),"C")]'));
    });

    casper.then(function() {
        casper.click("select#listaLong");
        casper.click(x('//*[@id="listaLong"]/option[.="Tres"]'));
    });

    casper.then(function() {
        casper.click("select#listaMultiple");
        casper.click(x('//*[@id="listaMultiple"]/option[.="Dos"]'));
        casper.click(x('//*[@id="listaMultiple"]/option[.="Uno"]'));
        casper.click(x('//*[@id="listaMultiple"]/option[.="Tres"]'));
    });


    utiles.clickEnGuardar(casper);
    utiles.assertPaginaGuardada(casper);

    casper.then(function() {
        casper.capture("img/combos-sobreescritos-despues-test.png");
    });
}


utiles.casperBegin("Combos sobreescritos", combosSobreescritos);