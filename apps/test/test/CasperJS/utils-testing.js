var require = patchRequire(require);
var x = require('casper').selectXPath;
var TRAVIS = require('system').env['TRAVIS'];
var pwd = require('system').env['PWD'];
var JENKINS = (pwd && (pwd.indexOf("jenkins") != -1));



exports.login = function(casper, test) {
    test = test || casper.test
    casper.then(function() {
            casper.open('http://localhost:9009/login');
        });
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
            casper.click(x("//a[text()[contains(.,'" + role + "')]]"));
        }
    });
    casper.then(function() {
       casper.test.assertSelectorHasText('li.dropdown a.dropdown-toggle',role);
       casper.echo("Usando rol " + role + ".");
    });
}


exports.nuevaSolicitud = function() {
    casper.thenOpen("Principal/solicitudes", function() {
        casper.echo("Creando nueva solicitud...");
        casper.click(x('//span[text()[contains(.,"Nuevo")]]'));
    });
}

exports.abrirUltimaSolicitud = function() {
    casper.thenOpen("Principal/solicitudes", function() {
        if (casper.exists("tr.x-grid-row:last-child")){
            casper.click("tr.x-grid-row:last-child");
            casper.echo("Abriendo última solicitud...")
            casper.thenClick(x('//span[text()[contains(.,"Editar")]]'), function() {
                casper.then(function() {
                    casper.test.assertTitle("Combos");
                });
            });
        } else {
            exports.nuevaSolicitud();
        }
    });
}


exports.clickEnGuardar = function(casperRecibido) {
    casper = casperRecibido || casper;
    casper.then(function() {
        casper.waitForSelector("input.btn[value='Guardar']", function() {
            casper.click("input.btn[value='Guardar']");
        });
    });
}

exports.assertPaginaGuardada = function(casperRecibido) {
    var selector = 'div.alert.alert-success';
    casper = casperRecibido || casper;
    casper.then(function() {
        casper.waitForSelector(selector);
    })
    casper.then(function() {
        casper.test.assertSelectorHasText(selector,'Página editada correctamente');
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


exports.captura = function (nombreImagen) {
    var ruta = "img/";
    var extension = ".png";
    casper.then(function() {
        casper.capture(ruta + nombreImagen + extension);
    });
}


exports.rellenarNuevaSolicitud = function() {
    exports.login(casper);
    exports.changeRole(casper,"Usuario");
    exports.nuevaSolicitud();
    exports.rellenarFormularioSolicitud();
    exports.subirDocumentacionSolicitud();
    exports.guardarPCEconomicos();
};


exports.prepararParaFirmarSolicitudActual = function () {
    exports.echo("Preparando para firmar...");
    exports.abrirEnlace("Presentacion","Presentación de la Solicitud");
    var selector = "input[type='submit'][value='Preparar para firmar']";
    casper.then(function() {
        casper.test.assertExists(selector);
    });
    exports.clickEnSelector(selector);
    casper.then(function() {
        if (casper.exists(x("//div[contains(@class,'alert-error')]//li[contains(text(),'No se pudo preparar para Firmar')]"))) {
            exports.configurarGestorDocumental();
            exports.clickEnSelector(selector);
        }
    });
    casper.then(function() {
        casper.test.assertExists("input[type=submit][value='Presentar solicitud'].btn");
    })
};

exports.presentarSolicitudActual = function () {
    exports.echo("Presentando la solicitud...");
    var selectorTextoFirmar = x('//p[text()[contains(.,"Firmar y Registrar")]][1]');
    exports.abrirEnlace("Presentacion","Presentación de la Solicitud");
    casper.then(function() {
        if (!casper.exists(selectorTextoFirmar)) {
           exports.prepararParaFirmarSolicitudActual();
        }
    });
    exports.clickEnSelector("input[type=submit][value='Presentar solicitud'].btn");
    exports.esperarPorSelector(selectorTextoFirmar);
    exports.clickEnSelector("input[type=submit][value='Firmar y registrar'].btn");
    exports.esperarPorSelector(x('//p[text()[contains(.,"Descargar el recibo")]]'));
};


exports.subirDocumentacionSolicitud = function() {
    exports.echo("Subiendo documentos ...");
    var file = "res/pdf-file.pdf";
    var folder = "/test/CasperJS/";
    var rutaFichero = "";
    if (JENKINS) {
        exports.echo("PWD = " + pwd);
        rutaFichero = pwd + folder;
    } else if (TRAVIS) {
        rutaFichero = require('system').env['TRAVIS_BUILD_DIR'] + "/apps/test" + folder;
    } else {
        rutaFichero = "./";
    }
    exports.abrirEnlace("Documentación FAP", "Documentación");
    exports.rellenarFormularioNuevoDocumento(
        "fs://aportacionsolicitud/v01",
        "La Descripción del documento",
        rutaFichero + file);
};

exports.rellenarFormularioSolicitud = function() {
    exports.echo("Rellenando formulario de solicitud...");
    exports.abrirEnlace("S. Normal", "Solicitante");
    exports.rellenaCombo("#personaSolicitante2Combo", "fisica");
    exports.rellenaCombo("#personaSolicitante2Fisica_nip_tipo", "nif");
    exports.rellenaFormulario("#SolicitanteeditarForm", {
       "#personaSolicitante2Fisica_nip_valor" : "11111111H",
       "#personaSolicitante2Fisica_nombre" : "NombreFAP",
       "#personaSolicitante2Fisica_primerApellido" : "ApellidoFAP"
    });
    exports.rellenaCombo("#personaSolicitante2Direccion_tipo", "canaria");
    exports.rellenaCombo("#personaSolicitante2Direccion_provinciaIsla", "_38");
    exports.rellenaCombo("#personaSolicitante2Direccion_isla", "_384", "#personaSolicitante2Direccion_isla option[value='_381']");
    exports.rellenaCombo("#personaSolicitante2Direccion_municipioIsla", "_380393", "#personaSolicitante2Direccion_municipioIsla option[value='_380393']");
    exports.rellenaFormulario("#SolicitanteeditarForm", {
        "#personaSolicitante2Direccion_codigoPostal" : "12345",
        "#personaSolicitante2Direccion_calle" : "Avenida FAP",
        "#personaSolicitante2Direccion_numero" : "5"
    });
    exports.clickEnGuardar(casper);
    exports.assertPaginaGuardada(casper);

};

exports.rellenarFormularioNuevoDocumento = function(tipo, descripcion, fichero) {
    var _tipo = tipo || "fs://aportacionsolicitud/v01";
    var _desc = descripcion || "Descripción del documento";
    var _file = fichero || "res/pdf-file.pdf";
    exports.echo("Rellenando documento " + _tipo + ": \""+ _desc + "\" con fichero " + _file );
    exports.clickEnSelector(x('//span[text()[contains(.,"Nuevo")]]'));
    exports.rellenaCombo("select#documento_tipo.combo", _tipo);
    exports.rellenaFormulario("#DocumentosFAPcrearForm", {
        "#fileAportacion_descripcion" : _desc,
        "#fileAportacion" : _file
    });
    exports.clickEnGuardar();
};

exports.guardarPCEconomicos = function() {
    exports.echo("Guardando PCEconomicos...");
    exports.abrirEnlace("PCEconómicos", "Conceptos económicos");
    casper.then(function() {
        if(existeMensajeErrorBaremacion()) {
            exports.echo("La baremación está desactivada")
            exports.activarBaremacion(casper.getCurrentUrl());
        } else {
            exports.echo("La baremación esta activada");
        }
    });
    exports.clickEnGuardar();
    exports.assertPaginaGuardada();
};


exports.activarBaremacion = function(paginaPrevia) {
    casper.then(function() {
        exports.echo("Activando Baremación...")
        var usuarioActual = exports.getUsuarioActual();
        exports.changeRole(casper, "Administrador");
        exports.abrirUrl("http://localhost:9009/Administracion/activarbaremacion", function () {
            casper.click("input[type='submit'][value='Cargar Tipo Evaluación']");
        });
        if (paginaPrevia) {
            exports.abrirUrl(paginaPrevia);
        }
        exports.changeRole(casper, usuarioActual);
    });
};

exports.configurarGestorDocumental = function() {
    exports.echo("Configurando el Gestor Documental...");
    casper.then(function() {
        var urlActual;
        var usuarioActual = exports.getUsuarioActual();
        casper.then(function() {
            urlActual =  casper.getCurrentUrl();
        });
        exports.changeRole(casper, "Administrador");
        casper.then(function () {
            casper.open("http://localhost:9009/Administracion/aed");
        });
        casper.then(function () {
            casper.click("input[type='submit'][value='Configurar gestor documental']");
        });
        casper.then(function () {
            casper.click("input[type='submit'][value='Actualizar trámites']");
        });
        exports.changeRole(casper, usuarioActual);
        casper.then(function() {
            casper.open(urlActual);
        });
    });
    exports.echo("Gestor documental configurado.")
};


exports.rellenaFormulario = function(selector, campos) {
    casper.then(function() {
        casper.fillSelectors(selector, campos);
    })
}


exports.abrirUrl = function(url, callback) {
    casper.thenOpen(url, callback)
}

exports.rellenaCombo = function (sel, val, esperarPorElemento) {
    if (esperarPorElemento) {
        casper.waitForSelector(esperarPorElemento);
    }
    casper.then(function() {
        casper.evaluate(function(selector, valor) {
            $(selector).val(valor).change();
        },sel,val)
    })
};

exports.echo = function(mensaje) {
    casper.then(function() {
        casper.echo(mensaje);
    })
};

exports.getUsuarioActual = function() {
    var usuario;
    usuario = casper.evaluate(function() {
        return $("li.dropdown a.dropdown-toggle").text().trim();
    });
    return usuario;
}

function existeMensajeErrorBaremacion() {
    var selector = x("//div[contains(@class,'alert-error')]//li[contains(text(),'no están disponibles')]");
    return casper.exists(selector);
}

exports.clickEnSelector = function(selector) {
    casper.then(function() {
        casper.click(selector);
    });
};

exports.esperarPorSelector = function(selector) {
    casper.waitForSelector(selector);
};

exports.seleccionarElemCombo = function (sel, val) {
	casper.waitForSelector(sel);
	casper.then(function() {
        casper.evaluate(function(selector, valor) {
       		$(selector).val(valor).change();
        },sel,val);
    });
};
