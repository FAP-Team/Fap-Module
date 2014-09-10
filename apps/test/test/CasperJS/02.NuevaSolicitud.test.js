var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var nuevaSolicitud = function(test) {
    utiles.changeRole(casper, "Usuario");

    casper.then(function() {
        this.click(x('//span[text()[contains(.,"Nuevo")]]'));
    });

    casper.then(function() {
        casper.waitForSelector(x('//title[text()[contains(.,"Combos")]][1]'))
    })

    casper.then(function() {
        test.assertTitle("Combos");
    });
}


utiles.casperBegin("Nueva Solicitud", nuevaSolicitud);
