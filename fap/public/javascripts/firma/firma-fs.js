Firma._getCertificados = function(){
	var certificados = [];
	certificados.push(new Certificado('11111111H', 'Luke Skywalker'));
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
	//Firma la url en vez del contenido del fichero
    return Firma._firmarTexto(url, certificado);
}