var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var ejemplosGrupos = function(test) {
    utiles.changeRole(casper, "Usuario");
    utiles.abrirUltimaSolicitud();

    utiles.abrirEnlace("Grupos","EjemplosdeGrupos");
    casper.then(function() {
        casper.sendKeys("#ifTexto","PruebaFAP");
    });


    utiles.clickEnGuardar(casper);
    utiles.assertPaginaGuardada(casper);
}

utiles.casperBegin("Ejemplos Grupos:", ejemplosGrupos);