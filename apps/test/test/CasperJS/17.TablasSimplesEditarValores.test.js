var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;


var nombre = "NombreFAP";
var apellido = "ApellidoFAP";
var nombreModificado = "NombreMofidicado";
var apellidoModificado = "ApellidoMofidicado";


function esperarPorPopup() {
    casper.waitForSelector("#tablaDeNombres_nombre");
    casper.waitForSelector("#tablaDeNombres_apellido");
    casper.waitForSelector("a#Guardar_id_popupNombre_popup");
}

function clickEditarFila(fila) {
    var selector = x("//div[@id='tablaNombres-grid']//td/div[contains(.,'" + fila + "')]");
    casper.waitForSelector(selector);
    casper.thenClick(selector);
    casper.thenClick(x('//span[text()="Editar"]'));
}

function rellenarPopupCon(nombre, apellido) {
    casper.fillSelectors("#popupNombreeditarForm", {
        "#tablaDeNombres_nombre": nombre,
        "#tablaDeNombres_apellido": apellido
    }, false);
    casper.thenEvaluate(function(){
        $("#Guardar_id_popupNombre_popup").click();
    });
}

var tablasSimples = function(test) {
    utiles.changeRole(casper, "Usuario");

    utiles.abrirUltimaSolicitud();

    utiles.abrirEnlace("Tablas Simples","TablasSimples");

    casper.then(function() {
        clickEditarFila(nombre);
    });

    casper.then(function() {
        esperarPorPopup();
    });
    casper.then(function(){
        rellenarPopupCon(nombreModificado, apellidoModificado);
    });

    casper.then(function() {
        clickEditarFila(nombreModificado);
    });

    casper.then(function() {
        esperarPorPopup();
    });

    casper.then(function(){
        rellenarPopupCon(nombre, apellido);
        casper.waitForSelector(x("//div[@id='tablaNombres-grid']//td/div[contains(.,'"+nombre+"')]"));
    });


}

utiles.casperBegin("Editar valores Tablas Simples:", tablasSimples);