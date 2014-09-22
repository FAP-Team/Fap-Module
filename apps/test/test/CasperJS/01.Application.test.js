var utiles = require('./utils-testing.js');

var paginaSinErrores = function(test) {
    casper.thenOpen('Principal/solicitudes', function(){
        test.assertTitle("Solicitudes");
        test.assertNot(casper.getTitle().match("Application error"));
    });
}

utiles.casperBegin("Pagina sin errores", paginaSinErrores);