var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var presentarSolicitud = function(test) {
    utiles.rellenarNuevaSolicitud();
    utiles.prepararParaFirmarSolicitudActual();
    utiles.presentarSolicitudActual();
}

utiles.casperBegin("Rellenar Solicitud", presentarSolicitud);