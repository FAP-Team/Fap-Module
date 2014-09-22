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
        casper.fillSelectors("#CombosOverwriteeditarForm", {
            "#lista" : "c",
            "#listaLong" : "3",
            "#listaMultiple" : ["a","b","c"],
            "#listaMultipleLong" : ["1","3"],
            "#wsjson" : "2",
            "#wsxml" : "1"
        });
    });

    utiles.clickEnGuardar(casper);
    utiles.assertPaginaGuardada(casper);
}


utiles.casperBegin("Combos sobreescritos", combosSobreescritos);