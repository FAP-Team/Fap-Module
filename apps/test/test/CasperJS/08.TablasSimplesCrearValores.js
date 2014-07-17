var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var tablasSimples = function(test) {
    utiles.changeRole(casper, "Usuario");
    utiles.abrirUltimaSolicitud();

    utiles.abrirEnlace("Tablas Simples","TablasSimples");
    casper.then(function() {
        casper.click(x('//span[text()="Nuevo"]'));

    });
    casper.then(function() {
        casper.waitForSelector("#tablaDeNombres_nombre");
        casper.waitForSelector("#tablaDeNombres_apellido");
    });
    casper.then(function(){
        casper.fillSelectors("#popupNombrecrearForm", {
            "#tablaDeNombres_nombre" : "NombreFAP",
            "#tablaDeNombres_apellido" : "ApellidoFAP"
        });
    });

    utiles.clickEnGuardar(casper);
    utiles.assertPaginaGuardada(casper);
}

utiles.casperBegin("Tablas Simples:", tablasSimples);