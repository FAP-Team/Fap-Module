var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var tablasSimplesComprobarValores = function(test) {
    utiles.changeRole(casper, "Usuario");
    utiles.abrirUltimaSolicitud();
    utiles.abrirEnlace("Tablas Simples", "TablasSimples");

    casper.then(function abrirFila() {
        casper.click(x("//div[@id='tablaNombres-grid']//td/div[contains(.,'NombreFAP')]"));
        casper.thenClick(x("//span[contains(.,'Ver')]"));
    });

    casper.then(function esperarPorPopup() {
        casper.waitForSelector("#tablaDeNombres_nombre");
        casper.waitForSelector("#tablaDeNombres_apellido");
    });

    casper.then(function() {
        casper.test.assertFieldCSS("#tablaDeNombres_nombre", "NombreFAP");
        casper.test.assertFieldCSS("#tablaDeNombres_apellido", "ApellidoFAP");
    })
}

utiles.casperBegin("Comprobar valores tablas simples:", tablasSimplesComprobarValores);