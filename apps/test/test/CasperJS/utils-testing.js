var require = patchRequire(require);
var x = require('casper').selectXPath;


exports.login = function(casper, test) {
    casper.then(function() {
        casper.fillSelectors('form#authen-form', {
            'input#login-username' : 'admin',
            'input#login-password' : 'a'
         },
        true);
    });
    casper.then(function() {
       casper.waitFor(function() {
            return "Solicitudes" === casper.getTitle();
       });
    });
}

exports.casperBegin = function(titulo, funcionTest) {
    casper.test.begin(titulo, {
        setUp: function(test) {
            casper.start('http://localhost:9009/login', function() {
            test.assertTitle("test");
            });
            casper.page.injectJs("../../public/javascripts/jquery-1.5.2.min.js");
            casper.viewport(1024, 768);
            exports.login(casper, test);
        },
        test: function(test) { funcionTest(test); }
    });

    casper.run(function() {
        casper.test.done();
    });
}

exports.changeRole = function(casper, role) {
    casper.then(function() {
        if(!casper.exists(x("//li[@class='dropdown']/*[text()[contains(.,'"+role+"')]]"))) {
            casper.click("a.dropdown-toggle");
            casper.waitForSelector("ul.dropdown-menu li form")
            casper.captureSelector("img/menu.png","ul.dropdown-menu li form");
            casper.click(x("//a[text()[contains(.,'" + role + "')]]"));
        }
    });
    casper.then(function() {
       casper.capture("img/cambio-de-rol-a-"+role+".png");
       casper.test.assertSelectorHasText('li.dropdown a.dropdown-toggle',role);
    });
}


exports.nuevaSolicitud = function() {
    casper.thenOpen("Principal/solicitudes", function() {
        casper.click(x('//span[text()[contains(.,"Nuevo")]]'));
    });
}

exports.abrirUltimaSolicitud = function() {
    casper.thenOpen("Principal/solicitudes", function() {
        casper.capture("img/nueva-solicitud.png");
        if (casper.exists("tr.x-grid-row:last-child")){
            casper.click("tr.x-grid-row:last-child");
            casper.thenClick(x('//span[text()[contains(.,"Editar")]]'), function() {
                casper.test.assertTitle("Combos");
            });
        } else {
            nuevaSolicitud();
        }
    });
}


exports.clickEnGuardar = function(casper) {
    casper.then(function() {
        casper.click("input.btn[value='Guardar']");
    });
}

exports.assertPaginaGuardada = function(casper) {
    casper.then(function() {
        casper.test.assertSelectorHasText('div.alert.alert-success','Página editada correctamente');
    });
}

exports.abrirEnlace = function(enlace, titulo) {
    casper.then(function() {
        casper.click(x('//a[text()="'+enlace+'"]'));
    });

    if (titulo) {
        casper.then(function() {
            casper.test.assertTitle(titulo);
        });
    }
}