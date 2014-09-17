var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var rellenarSolicitud = function(test) {
    utiles.rellenarNuevaSolicitud();
}

utiles.casperBegin("Rellenar Solicitud", rellenarSolicitud);