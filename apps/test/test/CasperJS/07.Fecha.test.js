var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var fechaTest = function(test) {
    utiles.changeRole(casper, "Usuario");
    utiles.abrirUltimaSolicitud();

    utiles.abrirEnlace("Fecha","Fechas");

    casper.then(function() {
        casper.click("#solicitud_fechas_fechaRequerida");
        casper.sendKeys("#solicitud_fechas_fechaRequerida", "20/08/2012");
        casper.click("#solicitud_fechas_fecha");
        casper.sendKeys("#solicitud_fechas_fecha", "20/08/2012");
    });

    utiles.clickEnGuardar(casper);
    utiles.assertPaginaGuardada(casper);
}

utiles.casperBegin("Fecha:", fechaTest);