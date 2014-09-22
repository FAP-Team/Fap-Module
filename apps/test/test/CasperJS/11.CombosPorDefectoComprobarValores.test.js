var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var combosDefecto = function(test) {
    utiles.changeRole(casper, "Usuario");
    utiles.abrirUltimaSolicitud();

    casper.then(function() {
        casper.test.assertFieldCSS("#solicitud_comboTest_list", "b");
        casper.test.assertFieldCSS("#solicitud_comboTest_listMultiple", ["b1","d2"]);
    });
}

utiles.casperBegin("Comprobar valores combos por defecto:", combosDefecto);