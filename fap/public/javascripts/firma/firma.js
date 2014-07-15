Firma = function(){
	
}

Certificado = function(clave, nombre){
	this.clave = clave;
	this.nombre = nombre;
}

Firma.getCertificados = function(){
	if(this.certificados == null){
		this.certificados = this._getCertificados();
	}
	return this.certificados;
}

Firma.listarCertificados = function(combo, opciones){
	var certificados = Firma.getCertificados();
	certificados2combo(combo, certificados);
}

var certificados2combo = function(combo, certificados){
	var options = '';
	$.each(certificados, function(index, certificado){
		options += '<option value="' + certificado.clave + '">' + certificado.nombre + '</option>';
	});
	$(combo).html(options);
}

Firma.firmarTexto = function(elCertificado, elTexto, elFirma, opciones){
	if(opciones == null) opciones = {};
	var mensajes = opciones.mensajes != null? opciones.mensajes : new Mensajes();
    certificadoSeleccionado = null;
	if(elCertificado) {
        var certificadoSeleccionado = getSelectedCert(elCertificado);
        if(certificadoSeleccionado == null){
            mensajes.error('No hay seleccionado ningún certificado');
        }
    }
    var texto = $(elTexto).val();
    var $firma = $(elFirma);

    var firma = Firma._firmarTexto(texto, certificadoSeleccionado);
    $firma.val(firma);

	return firma;
}
	
var getSelectedCert = function(elCombo){
	var value = $(elCombo).val();
	var selected = null;
	
	var certificados = Firma.getCertificados();
	for(var i = 0; i < certificados.length; i++){
		if(value == certificados[i].clave){
			selected = certificados[i];
			break;
		}
	}	
	return selected;
}

Firma.firmarDocumento = function(elCertificado, url, elFirma, opciones){
	var firma = null;
	if(opciones == null) opciones = {};
	var mensajes = opciones.mensajes != null? opciones.mensajes : new Mensajes();
	
	if(elCertificado) {
        var certificadoSeleccionado = getSelectedCert(elCertificado);
        if(certificadoSeleccionado == null){
            mensajes.error('No hay seleccionado ningún certificado');
        }
    }
    var $firma = $(elFirma);

    firma = Firma._firmarDocumento(url, certificadoSeleccionado);
    $.when(firma).done(function(valorFirma){
    	$firma.val(valorFirma.firma ? valorFirma.firma : valorFirma);
        $firma.change()
    });
	return firma;
}

Firma.firmarVariosDocumentos = function(elCertificado, urls, elFirma, opciones, errores) {
	var firmas = null;
	if(opciones == null) opciones = {};
	if (errores == null) errores = {};
	var mensajes = opciones.mensajes != null? opciones.mensajes : new Mensajes();
	var certificadoSeleccionado = getSelectedCert(elCertificado);
	firmas = Firma._firmarVariosDocumentos(urls, certificadoSeleccionado, errores);
	return firmas;
}