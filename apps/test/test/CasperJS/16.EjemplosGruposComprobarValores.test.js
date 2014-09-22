var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var tablasSimplesComprobarValores = function(test) {
    utiles.changeRole(casper, "Usuario");
    utiles.abrirUltimaSolicitud();
    utiles.abrirEnlace("Grupos","EjemplosdeGrupos");

    casper.then(function() {
        casper.test.assertFieldCSS("#ifTexto", "PruebaFAP");
    })
}

utiles.casperBegin("Comprobar valores tablas simples:", tablasSimplesComprobarValores);