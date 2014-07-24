var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var fechaComprobarValores = function(test) {
    utiles.changeRole(casper, "Usuario");
    utiles.abrirUltimaSolicitud();
    utiles.abrirEnlace("Fecha");

    casper.then(function() {
        casper.test.assertFieldCSS("#solicitud_fechas_fechaRequerida", "20/08/2012");
        casper.test.assertFieldCSS("#solicitud_fechas_fecha", "20/08/2012");
    });
}

utiles.casperBegin("Comprobar valores fecha:", fechaComprobarValores);