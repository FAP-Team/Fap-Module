Firma._getCertificados = function(){
	var certificados = [];
	certificados.push(new Certificado('1', 'Luke Skywalker'));
	certificados.push(new Certificado('2', 'Darth Vader'));
	return certificados;
}

Firma._firmarTexto = function(texto, certificado){
	if(texto == null)
		return null;
	return Base64.encode(addCertInfo(texto, certificado));
}

var addCertInfo = function(data, certificado){
	return certificado.nombre + "#" + certificado.clave + "#" + data;
}

Firma._firmarDocumento = function(url, certificado){
	var that = this;
	var data;
    $.ajax({
            async: false,
            url: 'doop.php',
            type: 'GET',
            success: function(resp) {
            	that.data = resp;
            }
    });
    if(data == null)
    	return null;
    return Firma._firmarTexto(data);
}