var utiles = require('./utils-testing.js');
var x = require('casper').selectXPath;


var nombre = "NombreFAP";
var apellido = "ApellidoFAP";
var botonBorrarPopup = "#Borrar_id_popupNombre_popup";


var selectorFila = x("//div[@id='tablaNombres-grid']//td/div[contains(.,'" + nombre + "')]");
function esperarPorPopup() {
    casper.waitForSelector("#tablaDeNombres_nombre");
    casper.waitForSelector("#tablaDeNombres_apellido");
    casper.waitForSelector("a#Guardar_id_popupNombre_popup");
}

function rellenarPopupCrearCon(nombre, apellido) {
    casper.fillSelectors("#popupNombrecrearForm", {
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
        casper.waitForSelector(selectorFila);
        casper.thenClick(selectorFila);
        casper.thenClick(x('//span[contains(.,"Borrar")]'));
    });

    casper.then(function(){
        casper.waitUntilVisible(botonBorrarPopup);
        casper.waitUntilVisible("#tablaDeNombres_nombre");
    });

    casper.thenEvaluate(function(){
        $("#Borrar_id_popupNombre_popup").click();
    });

    casper.then(function() {
        casper.waitWhileSelector(selectorFila);
    });

    casper.thenClick(x('//span[text()="Nuevo"]'));

    casper.then(function() {
        esperarPorPopup();
    });

    casper.then(function(){
        rellenarPopupCrearCon(nombre, apellido);
        casper.waitForSelector(x("//div[@id='tablaNombres-grid']//td/div[contains(.,'"+nombre+"')]"));
    });
}

utiles.casperBegin("Borrar valores Tablas Simples:", tablasSimples);