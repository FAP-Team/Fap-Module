var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var combosSobreescritos = function(test) {
    utiles.changeRole(casper, "Usuario");
    utiles.abrirUltimaSolicitud();
    utiles.abrirEnlace("Combos sobreescritos");
    utiles.seleccionarElemCombo ("#lista", "c");
    utiles.seleccionarElemCombo ("#listaLong", "3");
    utiles.seleccionarElemCombo ("#listaMultiple", ["a","b","c"]);
    utiles.seleccionarElemCombo ("#listaMultipleLong", ["1","3"]);
    utiles.seleccionarElemCombo ("#wsjson", "2");
    utiles.seleccionarElemCombo ("#wsxml", "1");

    casper.then(function() {
        casper.test.assertFieldCSS("#lista", "c");
        casper.test.assertFieldCSS("#listaLong", "3");
        casper.test.assertFieldCSS("#listaMultiple", ["a","b","c"]);
        casper.test.assertFieldCSS("#listaMultipleLong", ["1","3"]);
        casper.test.assertFieldCSS("#wsjson", "2");
        casper.test.assertFieldCSS("#wsxml", "1");

    });
}

utiles.casperBegin("Comprobar valores combos sobreescritos:", combosSobreescritos);