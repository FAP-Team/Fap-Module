var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var valoresDefecto = function(test) {
    utiles.abrirUltimaSolicitud();

    casper.then(function() {
        casper.click(x('//*[text()="Valores por defecto"]'));
    });

    casper.then(function() {
        casper.capture("img/valoresPorDefecto.png");
        casper.test.assertTitle("ValoresPorDefecto");
    });

    casper.then(function() {
        casper.test.assertFieldCSS('#mString', 'string');
        casper.test.assertFieldCSS('#mLong', '2');
        casper.test.assertFieldCSS('#mInteger', '4');
        casper.test.assertFieldCSS('#mBoolean', true);
        casper.test.assertFieldCSS('#mDouble', '2,345');
        casper.test.assertFieldCSS('#mLongText', 'texto largooooo largiiisimo');
        casper.test.assertFieldCSS('#mLongText2', 'texto largooooo largiiisimo');
        casper.test.assertFieldCSS('#mEmbeddable', 'pepe');
    });


}


utiles.casperBegin("Comprobar Valores por Defecto", valoresDefecto);