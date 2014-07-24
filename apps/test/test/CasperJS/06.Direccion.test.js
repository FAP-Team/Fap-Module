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

    casper.thenEvaluate(function setTipo() {
        $("#solicitud_direccionTest_direccion_tipo").val("canaria");
        $("#solicitud_direccionTest_direccion_tipo").change();
    });
    casper.then(function() {
        casper.test.assertEval(function(){
            return $("#solicitud_direccionTest_direccion_tipo").val() === "canaria";
        });
    });


    casper.thenEvaluate(function setProvincia() {
        $("#solicitud_direccionTest_direccion_provinciaIsla").val("_38");
        $("#solicitud_direccionTest_direccion_provinciaIsla").change();
    });
    casper.then(function() {
        casper.test.assertEval(function checkProvincia(){
            return $("#solicitud_direccionTest_direccion_provinciaIsla").val() === "_38";
        });
    });

    casper.waitForSelector(x('//option[text()="Tenerife"]'));
    casper.thenEvaluate(function setIsla() {
        $("#solicitud_direccionTest_direccion_isla").val("_384");
        $("#solicitud_direccionTest_direccion_isla").change();
    });
    casper.then(function() {
        casper.test.assertEval(function checkIsla(){
            return $("#solicitud_direccionTest_direccion_isla").val() === "_384";
        });
    });

    casper.waitForSelector(x('//option[text()="Santa Úrsula"]'));
    casper.thenEvaluate(function setMunicipio() {
        $("#solicitud_direccionTest_direccion_municipioIsla").val("_380393");
        $("#solicitud_direccionTest_direccion_municipioIsla").change();
    });
    casper.then(function() {
        casper.test.assertEval(function checkMunicipio(){
            return $("#solicitud_direccionTest_direccion_municipioIsla").val() === "_380393";
        });
    });

    casper.then(function() {
        casper.fillSelectors("form#DireccioneseditarForm", {
            'input[name="solicitud.direccionTest.direccion.codigoPostal"]' : '38390',
            'input[name="solicitud.direccionTest.direccion.calle"]' : 'Los Cedros',
            'input[name="solicitud.direccionTest.direccion.numero"]' : '14',
            'input[name="solicitud.direccionTest.direccion.otros"]' : 'bj'
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

    utiles.clickEnGuardar(casper);
    utiles.assertPaginaGuardada(casper);
}


utiles.casperBegin("Direccion", direccion);