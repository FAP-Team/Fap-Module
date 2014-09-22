var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;

var direccion = function(test) {
    utiles.changeRole(casper, "Usuario");
    utiles.abrirUltimaSolicitud();


    casper.then(function() {
        casper.click(x('//*[text()="Direccion"]'));
    });

    casper.then(function() {
        casper.test.assertTitle("Direcciones");
    });

    casper.then(function() {
        casper.test.assertEval(function comprobarTipo(){
            return $("#solicitud_direccionTest_direccion_tipo").val() === "canaria";
        });
    });

    casper.then(function() {
        casper.test.assertEval(function comprobarProvincia(){
            return $("#solicitud_direccionTest_direccion_provinciaIsla").val() === "_38";
        });
    });

    casper.then(function() {
        casper.test.assertEval(function comprobarIsla(){
            return $("#solicitud_direccionTest_direccion_isla").val() === "_384";
        });
    });

    casper.then(function() {
        casper.test.assertEval(function comprobarMunicipio(){
            return $("#solicitud_direccionTest_direccion_municipioIsla").val() === "_380393";
        });
    });

    casper.then(function() {
        casper.test.assertEval(function(){
            return $('input[name="solicitud.direccionTest.direccion.codigoPostal"]').val() === "38390";
        });
        casper.test.assertEval(function(){
            return $('input[name="solicitud.direccionTest.direccion.calle"]').val() === "Los Cedros";
        });
        casper.test.assertEval(function(){
                    return $('input[name="solicitud.direccionTest.direccion.numero"]').val() === "14";
        });
        casper.test.assertEval(function(){
            return $('input[name="solicitud.direccionTest.direccion.otros"]').val() === "bj";
        });
    });

}


utiles.casperBegin("Direccion comprobar valores", direccion);