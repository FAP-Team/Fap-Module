var utiles = require('./utils-testing.js');

var haceLogin = function(test) {
    casper.then(function() {
        test.assertTitle('Solicitudes');
    });

}

utiles.casperBegin("Login", haceLogin);