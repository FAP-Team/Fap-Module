var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var xpathBorrador = '//*[text()="Borrador"]';

function abrirEditarSolicitud() {
    casper.click(x(xpathBorrador));
    casper.thenClick(x('//span[text()[contains(.,"Editar")]]'), function() {
        casper.test.assertTitle("Combos");
    });
}

var editarSolicitud = function(test) {
    utiles.changeRole(casper, "Usuario");
    casper.then(function() {
        casper.test.assertTitle("Solicitudes");
    });

    casper.then(function() {
        if(!casper.exists(x(xpathBorrador))) {
            utiles.nuevaSolicitud();
        }
        abrirEditarSolicitud();
    });

}

utiles.casperBegin("Editar Solicitud:", editarSolicitud);